import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useToast } from "../context/ToastContext";
import FormField from "../components/FormField";

export default function Assignments() {
  const toast = useToast();
  const [assignments, setAssignments] = useState([]);
  const [agents, setAgents] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [form, setForm] = useState({ agentId: "", customerId: "" });

  const load = useCallback(() => {
    client.get("/assignments").then(({ data }) => setAssignments(data)).catch(() => {});
  }, []);

  useEffect(() => {
    load();
    client.get("/users/agents").then(({ data }) => setAgents(data)).catch(() => {});
    client.get("/users", { params: { role: "CUSTOMER", size: 100 } })
      .then(({ data }) => setCustomers(data.content))
      .catch(() => {});
  }, [load]);

  const assign = async (e) => {
    e.preventDefault();
    if (!form.agentId || !form.customerId) {
      toast.error("Select both an agent and a customer");
      return;
    }
    try {
      await client.put("/assignments", {
        agentId: Number(form.agentId),
        customerId: Number(form.customerId),
      });
      toast.success("Agent assigned");
      setForm({ agentId: "", customerId: "" });
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  const unassign = async (a) => {
    if (!window.confirm(`Remove ${a.agentName} from ${a.customerName}?`)) return;
    try {
      await client.delete(`/assignments/${a.id}`);
      toast.success("Assignment removed");
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  return (
    <div>
      <h1 className="page-title">Agent-Customer Assignment</h1>

      <div className="panel">
        <h2>Assign / Reassign</h2>
        <form onSubmit={assign} className="inline-form">
          <FormField label="Agent">
            <select value={form.agentId} onChange={(e) => setForm({ ...form, agentId: e.target.value })}>
              <option value="">Select agent</option>
              {agents.map((a) => <option key={a.id} value={a.id}>{a.name} ({a.email})</option>)}
            </select>
          </FormField>
          <FormField label="Customer">
            <select value={form.customerId} onChange={(e) => setForm({ ...form, customerId: e.target.value })}>
              <option value="">Select customer</option>
              {customers.map((c) => <option key={c.id} value={c.id}>{c.name} ({c.email})</option>)}
            </select>
          </FormField>
          <button className="btn btn-primary">Assign</button>
        </form>
        <p className="muted">Assigning a customer who already has an agent will reassign them.</p>
      </div>

      <div className="panel">
        <h2>Current Assignments</h2>
        {assignments.length === 0 ? (
          <p className="muted">No assignments yet.</p>
        ) : (
          <table className="table">
            <thead>
              <tr><th>Agent</th><th>Customer</th><th>Assigned On</th><th></th></tr>
            </thead>
            <tbody>
              {assignments.map((a) => (
                <tr key={a.id}>
                  <td>{a.agentName}</td>
                  <td>{a.customerName}</td>
                  <td>{new Date(a.assignedAt).toLocaleString()}</td>
                  <td>
                    <button className="btn btn-danger btn-sm" onClick={() => unassign(a)}>Remove</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
