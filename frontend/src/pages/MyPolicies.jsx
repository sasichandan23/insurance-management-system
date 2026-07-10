import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useToast } from "../context/ToastContext";
import Modal from "../components/Modal";
import StatusBadge from "../components/StatusBadge";

export default function MyPolicies() {
  const toast = useToast();
  const [policies, setPolicies] = useState([]);
  const [selected, setSelected] = useState(null);

  const load = useCallback(() => {
    client.get("/policies/my").then(({ data }) => setPolicies(data)).catch(() => {});
  }, []);

  useEffect(load, [load]);

  const requestCancel = async (policy) => {
    if (!window.confirm(`Request cancellation of policy ${policy.policyNumber}?`)) return;
    try {
      const { data } = await client.post(`/policies/${policy.id}/cancel-request`);
      toast.success(data.message);
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  return (
    <div>
      <h1 className="page-title">My Policies</h1>
      {policies.length === 0 ? (
        <p className="muted">You have no policies yet. Browse plans to apply for one.</p>
      ) : (
        <div className="panel">
          <table className="table">
            <thead>
              <tr>
                <th>Policy No.</th><th>Plan</th><th>Type</th><th>Coverage</th>
                <th>Status</th><th>Valid Till</th><th></th>
              </tr>
            </thead>
            <tbody>
              {policies.map((p) => (
                <tr key={p.id}>
                  <td>{p.policyNumber}</td>
                  <td>{p.planName}</td>
                  <td><StatusBadge value={p.insuranceType} /></td>
                  <td>₹{Number(p.coverageAmount).toLocaleString()}</td>
                  <td><StatusBadge value={p.status} /></td>
                  <td>{p.endDate || "—"}</td>
                  <td className="row-actions">
                    <button className="btn btn-outline btn-sm" onClick={() => setSelected(p)}>View</button>
                    {p.status === "ACTIVE" && (
                      <button className="btn btn-danger btn-sm" onClick={() => requestCancel(p)}>
                        Request Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selected && (
        <Modal title={`Policy ${selected.policyNumber}`} onClose={() => setSelected(null)}>
          <dl className="detail-list">
            <dt>Plan</dt><dd>{selected.planName} (<StatusBadge value={selected.insuranceType} />)</dd>
            <dt>Status</dt><dd><StatusBadge value={selected.status} /></dd>
            <dt>Coverage</dt><dd>₹{Number(selected.coverageAmount).toLocaleString()}</dd>
            <dt>Premium</dt>
            <dd>₹{Number(selected.premiumAmount).toLocaleString()} / {selected.premiumFrequency?.toLowerCase()}</dd>
            <dt>Start Date</dt><dd>{selected.startDate || "—"}</dd>
            <dt>End Date</dt><dd>{selected.endDate || "—"}</dd>
            <dt>Applied On</dt><dd>{new Date(selected.appliedAt).toLocaleString()}</dd>
            {selected.remarks && (<><dt>Remarks</dt><dd>{selected.remarks}</dd></>)}
            {selected.details && Object.entries(selected.details).map(([k, v]) => (
              <div key={k} className="detail-pair">
                <dt>{k.replace(/([A-Z])/g, " $1").replace(/^./, (c) => c.toUpperCase())}</dt>
                <dd>{String(v)}</dd>
              </div>
            ))}
          </dl>
        </Modal>
      )}
    </div>
  );
}
