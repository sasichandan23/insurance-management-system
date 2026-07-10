import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import client from "../api/client";
import { useAuth } from "../context/AuthContext";
import StatusBadge from "../components/StatusBadge";

function StatCard({ label, value, icon, accent }) {
  return (
    <div className={`stat-card accent-${accent || "blue"}`}>
      <div className="stat-icon">{icon}</div>
      <div>
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
      </div>
    </div>
  );
}

export default function Dashboard() {
  const { user } = useAuth();
  const [data, setData] = useState(null);

  useEffect(() => {
    client.get("/dashboard/summary").then(({ data }) => setData(data)).catch(() => {});
  }, []);

  if (!data) return <div className="page-loading">Loading dashboard...</div>;

  return (
    <div>
      <h1 className="page-title">Welcome back, {user.name.split(" ")[0]} 👋</h1>

      {user.role === "ADMIN" && (
        <>
          <div className="stat-grid">
            <StatCard label="Customers" value={data.totalCustomers} icon="👥" accent="blue" />
            <StatCard label="Agents" value={data.totalAgents} icon="🤝" accent="teal" />
            <StatCard label="Active Policies" value={data.activePolicies} icon="📄" accent="green" />
            <StatCard label="Pending Approvals" value={data.pendingPolicies} icon="⏳" accent="yellow" />
            <StatCard label="Premium Collected" value={`₹${Number(data.totalPremiumCollected).toLocaleString()}`} icon="💰" accent="purple" />
          </div>
          <div className="panel">
            <h2>Claims by Status</h2>
            <div className="claims-status-row">
              {Object.entries(data.claimsByStatus || {}).map(([status, count]) => (
                <div key={status} className="claims-status-item">
                  <StatusBadge value={status} />
                  <span className="claims-count">{count}</span>
                </div>
              ))}
            </div>
            <Link to="/claims" className="btn btn-outline btn-sm">Go to claims →</Link>
          </div>
        </>
      )}

      {user.role === "AGENT" && (
        <>
          <div className="stat-grid">
            <StatCard label="My Customers" value={data.assignedCustomers} icon="👥" accent="blue" />
            <StatCard label="Pending Policies" value={data.pendingPolicies} icon="⏳" accent="yellow" />
            <StatCard label="Active Policies" value={data.activePolicies} icon="📄" accent="green" />
            <StatCard label="Claims to Review" value={data.claimsAwaitingReview} icon="🧾" accent="red" />
          </div>
          <div className="panel">
            <h2>Recent Payments from My Customers</h2>
            {(data.recentPayments || []).length === 0 ? (
              <p className="muted">No payments yet.</p>
            ) : (
              <table className="table">
                <thead>
                  <tr><th>Customer</th><th>Policy</th><th>Amount</th><th>Paid On</th></tr>
                </thead>
                <tbody>
                  {data.recentPayments.map((p) => (
                    <tr key={p.id}>
                      <td>{p.customerName}</td>
                      <td>{p.policyNumber}</td>
                      <td>₹{Number(p.amount).toLocaleString()}</td>
                      <td>{p.paidDate}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}

      {user.role === "CUSTOMER" && (
        <>
          <div className="stat-grid">
            <StatCard label="Total Policies" value={data.totalPolicies} icon="📄" accent="blue" />
            <StatCard label="Active Policies" value={data.activePolicies} icon="✅" accent="green" />
            <StatCard label="Pending Applications" value={data.pendingPolicies} icon="⏳" accent="yellow" />
            <StatCard label="Total Claims" value={data.totalClaims} icon="🧾" accent="purple" />
          </div>
          <div className="panel">
            <h2>Next Premium Due</h2>
            {data.nextPremiumDue ? (
              <div className="due-banner">
                <div>
                  <strong>₹{Number(data.nextPremiumDue.amount).toLocaleString()}</strong>{" "}
                  for policy <strong>{data.nextPremiumDue.policyNumber}</strong> ({data.nextPremiumDue.planName})
                  <div className="muted">Due date: {data.nextPremiumDue.dueDate}{" "}
                    <StatusBadge value={data.nextPremiumDue.status} />
                  </div>
                </div>
                <Link to="/my-payments" className="btn btn-primary">Pay Now</Link>
              </div>
            ) : (
              <p className="muted">No premiums due. You are all caught up! 🎉</p>
            )}
          </div>
          <div className="panel">
            <h2>Quick Actions</h2>
            <div className="quick-actions">
              <Link to="/plans" className="btn btn-outline">Browse Plans</Link>
              <Link to="/my-claims" className="btn btn-outline">File a Claim</Link>
              <Link to="/my-policies" className="btn btn-outline">View My Policies</Link>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
