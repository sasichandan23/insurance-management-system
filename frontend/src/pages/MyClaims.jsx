import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useToast } from "../context/ToastContext";
import Modal from "../components/Modal";
import StatusBadge from "../components/StatusBadge";
import FormField from "../components/FormField";

const EMPTY = { policyId: "", claimAmount: "", incidentDate: "", description: "" };

export default function MyClaims() {
  const toast = useToast();
  const [claims, setClaims] = useState([]);
  const [activePolicies, setActivePolicies] = useState([]);
  const [form, setForm] = useState(null);
  const [errors, setErrors] = useState({});

  const load = useCallback(() => {
    client.get("/claims/my").then(({ data }) => setClaims(data)).catch(() => {});
  }, []);

  useEffect(() => {
    load();
    client.get("/policies/my").then(({ data }) =>
      setActivePolicies(data.filter((p) => p.status === "ACTIVE"))
    ).catch(() => {});
  }, [load]);

  const validate = () => {
    const e = {};
    if (!form.policyId) e.policyId = "Select a policy";
    if (!form.claimAmount || Number(form.claimAmount) <= 0) e.claimAmount = "Enter a valid amount";
    if (!form.incidentDate) e.incidentDate = "Incident date is required";
    else if (new Date(form.incidentDate) > new Date()) e.incidentDate = "Cannot be in the future";
    if (!form.description || form.description.trim().length < 10)
      e.description = "Describe the incident (at least 10 characters)";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      await client.post("/claims", {
        policyId: Number(form.policyId),
        claimAmount: Number(form.claimAmount),
        incidentDate: form.incidentDate,
        description: form.description,
      });
      toast.success("Claim filed successfully");
      setForm(null);
      load();
    } catch (err) {
      const { message, fieldErrors } = apiError(err);
      setErrors(fieldErrors);
      toast.error(message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">My Claims</h1>
        <button className="btn btn-primary"
          onClick={() => { setErrors({}); setForm({ ...EMPTY }); }}
          disabled={activePolicies.length === 0}
          title={activePolicies.length === 0 ? "You need an active policy to file a claim" : ""}>
          + File a Claim
        </button>
      </div>

      {claims.length === 0 ? (
        <p className="muted">No claims filed yet.</p>
      ) : (
        <div className="panel">
          <table className="table">
            <thead>
              <tr>
                <th>Claim No.</th><th>Policy</th><th>Amount</th><th>Incident</th>
                <th>Status</th><th>Settled</th><th>Remarks</th>
              </tr>
            </thead>
            <tbody>
              {claims.map((c) => (
                <tr key={c.id}>
                  <td>{c.claimNumber}</td>
                  <td>{c.policyNumber}</td>
                  <td>₹{Number(c.claimAmount).toLocaleString()}</td>
                  <td>{c.incidentDate}</td>
                  <td><StatusBadge value={c.status} /></td>
                  <td>{c.settledAmount ? `₹${Number(c.settledAmount).toLocaleString()} on ${c.settledDate}` : "—"}</td>
                  <td>{c.reviewerRemarks || "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {form && (
        <Modal title="File a New Claim" onClose={() => setForm(null)}>
          <form onSubmit={submit}>
            <FormField label="Policy *" error={errors.policyId}>
              <select value={form.policyId} onChange={(e) => setForm({ ...form, policyId: e.target.value })}>
                <option value="">Select an active policy</option>
                {activePolicies.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.policyNumber} — {p.planName} (coverage ₹{Number(p.coverageAmount).toLocaleString()})
                  </option>
                ))}
              </select>
            </FormField>
            <FormField label="Claim Amount (₹) *" error={errors.claimAmount}>
              <input type="number" min="1" value={form.claimAmount}
                onChange={(e) => setForm({ ...form, claimAmount: e.target.value })} />
            </FormField>
            <FormField label="Incident Date *" error={errors.incidentDate}>
              <input type="date" value={form.incidentDate}
                onChange={(e) => setForm({ ...form, incidentDate: e.target.value })} />
            </FormField>
            <FormField label="Description *" error={errors.description}>
              <textarea rows="4" value={form.description} placeholder="Describe what happened..."
                onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </FormField>
            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={() => setForm(null)}>Cancel</button>
              <button className="btn btn-primary">Submit Claim</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
