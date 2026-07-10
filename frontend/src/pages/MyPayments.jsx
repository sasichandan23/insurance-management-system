import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useToast } from "../context/ToastContext";
import Modal from "../components/Modal";
import StatusBadge from "../components/StatusBadge";

export default function MyPayments() {
  const toast = useToast();
  const [payments, setPayments] = useState([]);
  const [paying, setPaying] = useState(null);
  const [processing, setProcessing] = useState(false);

  const load = useCallback(() => {
    client.get("/payments/my").then(({ data }) => setPayments(data)).catch(() => {});
  }, []);

  useEffect(load, [load]);

  const dues = payments.filter((p) => p.status !== "PAID");
  const history = payments.filter((p) => p.status === "PAID");

  const confirmPay = async () => {
    setProcessing(true);
    try {
      const { data } = await client.post(`/payments/${paying.id}/pay`);
      toast.success(`Payment successful! Transaction: ${data.transactionRef}`);
      setPaying(null);
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div>
      <h1 className="page-title">My Payments</h1>

      <div className="panel">
        <h2>Premiums Due</h2>
        {dues.length === 0 ? (
          <p className="muted">No pending premiums. You are all caught up! 🎉</p>
        ) : (
          <table className="table">
            <thead>
              <tr><th>Policy</th><th>Plan</th><th>Due Date</th><th>Amount</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              {dues.map((p) => (
                <tr key={p.id}>
                  <td>{p.policyNumber}</td>
                  <td>{p.planName}</td>
                  <td>{p.dueDate}</td>
                  <td>₹{Number(p.amount).toLocaleString()}</td>
                  <td><StatusBadge value={p.status} /></td>
                  <td>
                    <button className="btn btn-primary btn-sm" onClick={() => setPaying(p)}>Pay Now</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="panel">
        <h2>Payment History</h2>
        {history.length === 0 ? (
          <p className="muted">No payments made yet.</p>
        ) : (
          <table className="table">
            <thead>
              <tr><th>Policy</th><th>Paid On</th><th>Amount</th><th>Transaction Ref.</th><th>Status</th></tr>
            </thead>
            <tbody>
              {history.map((p) => (
                <tr key={p.id}>
                  <td>{p.policyNumber}</td>
                  <td>{p.paidDate}</td>
                  <td>₹{Number(p.amount).toLocaleString()}</td>
                  <td className="mono">{p.transactionRef}</td>
                  <td><StatusBadge value={p.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {paying && (
        <Modal title="Confirm Payment (Simulated)" onClose={() => setPaying(null)}>
          <div className="pay-summary">
            <p>You are paying the premium for policy <strong>{paying.policyNumber}</strong>:</p>
            <div className="pay-amount">₹{Number(paying.amount).toLocaleString()}</div>
            <p className="muted">
              This is a simulated payment for demonstration — no real money is involved.
              A transaction reference will be generated automatically.
            </p>
          </div>
          <div className="modal-actions">
            <button className="btn btn-outline" onClick={() => setPaying(null)}>Cancel</button>
            <button className="btn btn-success" onClick={confirmPay} disabled={processing}>
              {processing ? "Processing..." : "Confirm & Pay"}
            </button>
          </div>
        </Modal>
      )}
    </div>
  );
}
