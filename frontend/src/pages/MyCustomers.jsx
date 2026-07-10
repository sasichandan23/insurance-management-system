import { useEffect, useState } from "react";
import client from "../api/client";

export default function MyCustomers() {
  const [assignments, setAssignments] = useState([]);

  useEffect(() => {
    client.get("/assignments/my-customers").then(({ data }) => setAssignments(data)).catch(() => {});
  }, []);

  return (
    <div>
      <h1 className="page-title">My Customers</h1>
      {assignments.length === 0 ? (
        <p className="muted">No customers assigned to you yet.</p>
      ) : (
        <div className="panel">
          <table className="table">
            <thead>
              <tr><th>Customer</th><th>Assigned On</th></tr>
            </thead>
            <tbody>
              {assignments.map((a) => (
                <tr key={a.id}>
                  <td>{a.customerName}</td>
                  <td>{new Date(a.assignedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="muted">
            Their policy applications appear under <strong>Policies</strong> and their claims under{" "}
            <strong>Claims</strong>.
          </p>
        </div>
      )}
    </div>
  );
}
