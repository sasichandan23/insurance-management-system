import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import Modal from "../components/Modal";
import StatusBadge from "../components/StatusBadge";
import FormField from "../components/FormField";
import Pagination from "../components/Pagination";

const STATUSES = ["FILED", "UNDER_REVIEW", "APPROVED", "REJECTED", "SETTLED"];

export default function ClaimList() {
  const { user } = useAuth();
  const toast = useToast();
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState("");
  const [review, setReview] = useState(null); // { claim, status, remarks }
  const [settle, setSettle] = useState(null); // { claim, settledAmount }

  const load = useCallback(() => {
    const params = { page, size: 10 };
    if (status) params.status = status;
    client.get("/claims", { params }).then(({ data }) => setPageData(data)).catch(() => {});
  }, [page, status]);

  useEffect(load, [load]);

  const submitReview = async (e) => {
    e.preventDefault();
    try {
      await client.put(`/claims/${review.claim.id}/review`, {
        status: review.status,
        remarks: review.remarks || undefined,
      });
      toast.success(`Claim marked ${review.status.replace("_", " ")}`);
      setReview(null);
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  const submitSettle = async (e) => {
    e.preventDefault();
    try {
      await client.put(`/claims/${settle.claim.id}/settle`, {
        settledAmount: Number(settle.settledAmount),
      });
      toast.success("Claim settled");
      setSettle(null);
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Claims</h1>
        <select value={status} onChange={(e) => { setPage(0); setStatus(e.target.value); }}>
          <option value="">All Statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s.replace("_", " ")}</option>)}
        </select>
      </div>

      <div className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>Claim No.</th><th>Customer</th><th>Policy</th><th>Amount</th>
              <th>Incident</th><th>Status</th><th></th>
            </tr>
          </thead>
          <tbody>
            {(pageData?.content || []).map((c) => (
              <tr key={c.id}>
                <td>{c.claimNumber}</td>
                <td>{c.customerName}</td>
                <td>{c.policyNumber}</td>
                <td>₹{Number(c.claimAmount).toLocaleString()}</td>
                <td>{c.incidentDate}</td>
                <td><StatusBadge value={c.status} /></td>
                <td className="row-actions">
                  {["FILED", "UNDER_REVIEW"].includes(c.status) && (
                    <button className="btn btn-primary btn-sm"
                      onClick={() => setReview({ claim: c, status: "UNDER_REVIEW", remarks: "" })}>
                      Review
                    </button>
                  )}
                  {user.role === "ADMIN" && c.status === "APPROVED" && (
                    <button className="btn btn-success btn-sm"
                      onClick={() => setSettle({ claim: c, settledAmount: c.claimAmount })}>
                      Settle
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {pageData?.content?.length === 0 && <p className="muted">No claims found.</p>}
        <Pagination page={page} totalPages={pageData?.totalPages || 0} onChange={setPage} />
      </div>

      {review && (
        <Modal title={`Review ${review.claim.claimNumber}`} onClose={() => setReview(null)}>
          <form onSubmit={submitReview}>
            <p className="muted">{review.claim.customerName} claims ₹
              {Number(review.claim.claimAmount).toLocaleString()} — "{review.claim.description}"</p>
            <FormField label="Decision *">
              <select value={review.status} onChange={(e) => setReview({ ...review, status: e.target.value })}>
                <option value="UNDER_REVIEW">Mark Under Review</option>
                <option value="APPROVED">Approve</option>
                <option value="REJECTED">Reject</option>
              </select>
            </FormField>
            <FormField label="Remarks">
              <textarea rows="3" value={review.remarks}
                onChange={(e) => setReview({ ...review, remarks: e.target.value })} />
            </FormField>
            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={() => setReview(null)}>Cancel</button>
              <button className="btn btn-primary">Save Decision</button>
            </div>
          </form>
        </Modal>
      )}

      {settle && (
        <Modal title={`Settle ${settle.claim.claimNumber}`} onClose={() => setSettle(null)}>
          <form onSubmit={submitSettle}>
            <p className="muted">Claimed amount: ₹{Number(settle.claim.claimAmount).toLocaleString()}</p>
            <FormField label="Settled Amount (₹) *">
              <input type="number" min="0" max={settle.claim.claimAmount} value={settle.settledAmount}
                onChange={(e) => setSettle({ ...settle, settledAmount: e.target.value })} />
            </FormField>
            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={() => setSettle(null)}>Cancel</button>
              <button className="btn btn-success">Confirm Settlement</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
