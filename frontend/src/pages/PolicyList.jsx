import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import Modal from "../components/Modal";
import StatusBadge from "../components/StatusBadge";
import FormField from "../components/FormField";
import Pagination from "../components/Pagination";

const STATUSES = ["PENDING", "ACTIVE", "REJECTED", "EXPIRED", "CANCELLED"];

export default function PolicyList() {
  const { user } = useAuth();
  const toast = useToast();
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const [decision, setDecision] = useState(null); // { policy, approved, remarks }

  const load = useCallback(() => {
    const params = { page, size: 10 };
    if (status) params.status = status;
    client.get("/policies", { params }).then(({ data }) => setPageData(data)).catch(() => {});
  }, [page, status]);

  useEffect(load, [load]);

  const submitDecision = async (e) => {
    e.preventDefault();
    try {
      await client.put(`/policies/${decision.policy.id}/decision`, {
        approved: decision.approved,
        remarks: decision.remarks || undefined,
      });
      toast.success(decision.approved ? "Policy approved and activated" : "Policy rejected");
      setDecision(null);
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  const cancelPolicy = async (policy) => {
    if (!window.confirm(`Cancel policy ${policy.policyNumber}? This cannot be undone.`)) return;
    try {
      await client.put(`/policies/${policy.id}/cancel`);
      toast.success("Policy cancelled");
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Policies</h1>
        <select value={status} onChange={(e) => { setPage(0); setStatus(e.target.value); }}>
          <option value="">All Statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <div className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>Policy No.</th><th>Customer</th><th>Plan</th><th>Type</th>
              <th>Status</th><th>Applied</th><th></th>
            </tr>
          </thead>
          <tbody>
            {(pageData?.content || []).map((p) => (
              <tr key={p.id}>
                <td>{p.policyNumber}</td>
                <td>{p.customerName}</td>
                <td>{p.planName}</td>
                <td><StatusBadge value={p.insuranceType} /></td>
                <td><StatusBadge value={p.status} /></td>
                <td>{new Date(p.appliedAt).toLocaleDateString()}</td>
                <td className="row-actions">
                  {p.status === "PENDING" && (
                    <>
                      <button className="btn btn-success btn-sm"
                        onClick={() => setDecision({ policy: p, approved: true, remarks: "" })}>
                        Approve
                      </button>
                      <button className="btn btn-danger btn-sm"
                        onClick={() => setDecision({ policy: p, approved: false, remarks: "" })}>
                        Reject
                      </button>
                    </>
                  )}
                  {user.role === "ADMIN" && p.status === "ACTIVE" && (
                    <button className="btn btn-outline btn-sm" onClick={() => cancelPolicy(p)}>
                      Cancel Policy
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {pageData?.content?.length === 0 && <p className="muted">No policies found.</p>}
        <Pagination page={page} totalPages={pageData?.totalPages || 0} onChange={setPage} />
      </div>

      {decision && (
        <Modal
          title={`${decision.approved ? "Approve" : "Reject"} ${decision.policy.policyNumber}`}
          onClose={() => setDecision(null)}
        >
          <form onSubmit={submitDecision}>
            <p className="muted">
              {decision.policy.customerName} · {decision.policy.planName} ·
              Coverage ₹{Number(decision.policy.coverageAmount).toLocaleString()}
            </p>
            <FormField label="Remarks (optional)">
              <textarea rows="3" value={decision.remarks}
                onChange={(e) => setDecision({ ...decision, remarks: e.target.value })} />
            </FormField>
            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={() => setDecision(null)}>Back</button>
              <button className={`btn ${decision.approved ? "btn-success" : "btn-danger"}`}>
                Confirm {decision.approved ? "Approval" : "Rejection"}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
