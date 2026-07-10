import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useToast } from "../context/ToastContext";
import Modal from "../components/Modal";
import StatusBadge from "../components/StatusBadge";
import FormField from "../components/FormField";
import Pagination from "../components/Pagination";

const EMPTY_AGENT = { name: "", email: "", password: "", phone: "" };

export default function Users() {
  const toast = useToast();
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [role, setRole] = useState("");
  const [search, setSearch] = useState("");
  const [agentForm, setAgentForm] = useState(null);
  const [errors, setErrors] = useState({});

  const load = useCallback(() => {
    const params = { page, size: 10 };
    if (role) params.role = role;
    if (search) params.search = search;
    client.get("/users", { params }).then(({ data }) => setPageData(data)).catch(() => {});
  }, [page, role, search]);

  useEffect(load, [load]);

  const toggleActive = async (u) => {
    try {
      await client.put(`/users/${u.id}/status`, { active: !u.active });
      toast.success(`${u.name} ${u.active ? "deactivated" : "activated"}`);
      load();
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  const createAgent = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      await client.post("/users/agents", agentForm);
      toast.success("Agent account created");
      setAgentForm(null);
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
        <h1 className="page-title">Users</h1>
        <div className="page-actions">
          <input
            placeholder="Search name or email..."
            value={search}
            onChange={(e) => { setPage(0); setSearch(e.target.value); }}
          />
          <select value={role} onChange={(e) => { setPage(0); setRole(e.target.value); }}>
            <option value="">All Roles</option>
            <option value="CUSTOMER">Customers</option>
            <option value="AGENT">Agents</option>
            <option value="ADMIN">Admins</option>
          </select>
          <button className="btn btn-primary" onClick={() => { setErrors({}); setAgentForm({ ...EMPTY_AGENT }); }}>
            + New Agent
          </button>
        </div>
      </div>

      <div className="panel">
        <table className="table">
          <thead>
            <tr><th>Name</th><th>Email</th><th>Role</th><th>Phone</th><th>Status</th><th>Joined</th><th></th></tr>
          </thead>
          <tbody>
            {(pageData?.content || []).map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td><StatusBadge value={u.role} /></td>
                <td>{u.phone || "—"}</td>
                <td><StatusBadge value={u.active ? "ACTIVE" : "INACTIVE"} /></td>
                <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                <td>
                  {u.role !== "ADMIN" && (
                    <button
                      className={`btn btn-sm ${u.active ? "btn-danger" : "btn-success"}`}
                      onClick={() => toggleActive(u)}
                    >
                      {u.active ? "Deactivate" : "Activate"}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {pageData?.content?.length === 0 && <p className="muted">No users found.</p>}
        <Pagination page={page} totalPages={pageData?.totalPages || 0} onChange={setPage} />
      </div>

      {agentForm && (
        <Modal title="Create Agent Account" onClose={() => setAgentForm(null)}>
          <form onSubmit={createAgent}>
            <FormField label="Full Name *" error={errors.name}>
              <input value={agentForm.name} onChange={(e) => setAgentForm({ ...agentForm, name: e.target.value })} />
            </FormField>
            <FormField label="Email *" error={errors.email}>
              <input type="email" value={agentForm.email}
                onChange={(e) => setAgentForm({ ...agentForm, email: e.target.value })} />
            </FormField>
            <FormField label="Temporary Password *" error={errors.password}>
              <input type="text" value={agentForm.password} placeholder="Min 8 characters"
                onChange={(e) => setAgentForm({ ...agentForm, password: e.target.value })} />
            </FormField>
            <FormField label="Phone" error={errors.phone}>
              <input value={agentForm.phone} placeholder="10-digit mobile number"
                onChange={(e) => setAgentForm({ ...agentForm, phone: e.target.value })} />
            </FormField>
            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={() => setAgentForm(null)}>Cancel</button>
              <button className="btn btn-primary">Create Agent</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
