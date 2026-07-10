import { useCallback, useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import Modal from "../components/Modal";
import StatusBadge from "../components/StatusBadge";
import FormField from "../components/FormField";
import Pagination from "../components/Pagination";

const TYPES = ["LIFE", "HEALTH", "MOTOR", "HOME"];
const FREQUENCIES = ["MONTHLY", "QUARTERLY", "YEARLY"];

const EMPTY_PLAN = {
  name: "",
  insuranceType: "LIFE",
  description: "",
  coverageAmount: "",
  premiumAmount: "",
  premiumFrequency: "MONTHLY",
  durationYears: 1,
  active: true,
};

/** Extra application details asked per insurance type. */
const DETAIL_FIELDS = {
  LIFE: [
    { key: "nomineeName", label: "Nominee Name" },
    { key: "nomineeRelation", label: "Nominee Relation" },
  ],
  HEALTH: [
    { key: "familyMembers", label: "Number of Family Members Covered" },
    { key: "preExistingConditions", label: "Pre-existing Conditions (if any)" },
  ],
  MOTOR: [
    { key: "vehicleRegistrationNumber", label: "Vehicle Registration Number" },
    { key: "vehicleModel", label: "Vehicle Make and Model" },
  ],
  HOME: [
    { key: "propertyAddress", label: "Property Address" },
    { key: "builtYear", label: "Year of Construction" },
  ],
};

export default function Plans() {
  const { user } = useAuth();
  const toast = useToast();
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);
  const [typeFilter, setTypeFilter] = useState("");
  const [editing, setEditing] = useState(null); // plan being created/edited (admin)
  const [applying, setApplying] = useState(null); // plan being applied to (customer)
  const [errors, setErrors] = useState({});

  const load = useCallback(() => {
    const params = { page, size: 12 };
    if (typeFilter) params.type = typeFilter;
    client.get("/plans", { params }).then(({ data }) => setPageData(data)).catch(() => {});
  }, [page, typeFilter]);

  useEffect(load, [load]);

  const savePlan = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      const payload = {
        ...editing,
        coverageAmount: Number(editing.coverageAmount),
        premiumAmount: Number(editing.premiumAmount),
        durationYears: Number(editing.durationYears),
      };
      if (editing.id) {
        await client.put(`/plans/${editing.id}`, payload);
        toast.success("Plan updated");
      } else {
        await client.post("/plans", payload);
        toast.success("Plan created");
      }
      setEditing(null);
      load();
    } catch (err) {
      const { message, fieldErrors } = apiError(err);
      setErrors(fieldErrors);
      toast.error(message);
    }
  };

  const submitApplication = async (e) => {
    e.preventDefault();
    try {
      await client.post("/policies", {
        planId: applying.plan.id,
        details: applying.details,
      });
      toast.success("Application submitted! Track it under My Policies.");
      setApplying(null);
    } catch (err) {
      toast.error(apiError(err).message);
    }
  };

  const setDetail = (key) => (e) =>
    setApplying({
      ...applying,
      details: { ...applying.details, [key]: e.target.value },
    });

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Insurance Plans</h1>
        <div className="page-actions">
          <select value={typeFilter} onChange={(e) => { setPage(0); setTypeFilter(e.target.value); }}>
            <option value="">All Types</option>
            {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
          {user.role === "ADMIN" && (
            <button className="btn btn-primary" onClick={() => { setErrors({}); setEditing({ ...EMPTY_PLAN }); }}>
              + New Plan
            </button>
          )}
        </div>
      </div>

      <div className="card-grid">
        {(pageData?.content || []).map((plan) => (
          <div key={plan.id} className="plan-card">
            <div className="plan-card-top">
              <StatusBadge value={plan.insuranceType} />
              {!plan.active && <StatusBadge value="INACTIVE" />}
            </div>
            <h3>{plan.name}</h3>
            <p className="muted plan-desc">{plan.description || "—"}</p>
            <div className="plan-figures">
              <div>
                <div className="figure-label">Coverage</div>
                <div className="figure-value">₹{Number(plan.coverageAmount).toLocaleString()}</div>
              </div>
              <div>
                <div className="figure-label">Premium</div>
                <div className="figure-value">
                  ₹{Number(plan.premiumAmount).toLocaleString()}
                  <span className="figure-sub">/{plan.premiumFrequency.toLowerCase()}</span>
                </div>
              </div>
              <div>
                <div className="figure-label">Duration</div>
                <div className="figure-value">{plan.durationYears} yrs</div>
              </div>
            </div>
            <div className="plan-card-actions">
              {user.role === "CUSTOMER" && plan.active && (
                <button className="btn btn-primary btn-block"
                  onClick={() => setApplying({ plan, details: {} })}>
                  Apply Now
                </button>
              )}
              {user.role === "ADMIN" && (
                <button className="btn btn-outline btn-block"
                  onClick={() => { setErrors({}); setEditing({ ...plan }); }}>
                  Edit
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
      {pageData?.content?.length === 0 && <p className="muted">No plans found.</p>}
      <Pagination page={page} totalPages={pageData?.totalPages || 0} onChange={setPage} />

      {editing && (
        <Modal title={editing.id ? "Edit Plan" : "Create Plan"} onClose={() => setEditing(null)} wide>
          <form onSubmit={savePlan}>
            <div className="form-grid">
              <FormField label="Plan Name *" error={errors.name}>
                <input value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} />
              </FormField>
              <FormField label="Insurance Type *" error={errors.insuranceType}>
                <select value={editing.insuranceType}
                  onChange={(e) => setEditing({ ...editing, insuranceType: e.target.value })}>
                  {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                </select>
              </FormField>
              <FormField label="Coverage Amount (₹) *" error={errors.coverageAmount}>
                <input type="number" min="1000" value={editing.coverageAmount}
                  onChange={(e) => setEditing({ ...editing, coverageAmount: e.target.value })} />
              </FormField>
              <FormField label="Premium Amount (₹) *" error={errors.premiumAmount}>
                <input type="number" min="1" value={editing.premiumAmount}
                  onChange={(e) => setEditing({ ...editing, premiumAmount: e.target.value })} />
              </FormField>
              <FormField label="Premium Frequency *" error={errors.premiumFrequency}>
                <select value={editing.premiumFrequency}
                  onChange={(e) => setEditing({ ...editing, premiumFrequency: e.target.value })}>
                  {FREQUENCIES.map((f) => <option key={f} value={f}>{f}</option>)}
                </select>
              </FormField>
              <FormField label="Duration (years) *" error={errors.durationYears}>
                <input type="number" min="1" max="100" value={editing.durationYears}
                  onChange={(e) => setEditing({ ...editing, durationYears: e.target.value })} />
              </FormField>
            </div>
            <FormField label="Description" error={errors.description}>
              <textarea rows="3" value={editing.description || ""}
                onChange={(e) => setEditing({ ...editing, description: e.target.value })} />
            </FormField>
            <FormField label="">
              <label className="checkbox-label">
                <input type="checkbox" checked={editing.active}
                  onChange={(e) => setEditing({ ...editing, active: e.target.checked })} />
                Active (visible to customers)
              </label>
            </FormField>
            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={() => setEditing(null)}>Cancel</button>
              <button className="btn btn-primary">{editing.id ? "Save Changes" : "Create Plan"}</button>
            </div>
          </form>
        </Modal>
      )}

      {applying && (
        <Modal title={`Apply for ${applying.plan.name}`} onClose={() => setApplying(null)}>
          <form onSubmit={submitApplication}>
            <p className="muted">
              {applying.plan.insuranceType} insurance · Coverage ₹
              {Number(applying.plan.coverageAmount).toLocaleString()} · Premium ₹
              {Number(applying.plan.premiumAmount).toLocaleString()}/
              {applying.plan.premiumFrequency.toLowerCase()}
            </p>
            {DETAIL_FIELDS[applying.plan.insuranceType].map((f) => (
              <FormField key={f.key} label={f.label}>
                <input value={applying.details[f.key] || ""} onChange={setDetail(f.key)} required />
              </FormField>
            ))}
            <div className="modal-actions">
              <button type="button" className="btn btn-outline" onClick={() => setApplying(null)}>Cancel</button>
              <button className="btn btn-primary">Submit Application</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
