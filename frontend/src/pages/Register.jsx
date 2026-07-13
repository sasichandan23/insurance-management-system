import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiError } from "../api/client";
import AuthLayout from "../components/AuthLayout";
import FormField from "../components/FormField";

const EMPTY = {
  name: "",
  email: "",
  password: "",
  confirmPassword: "",
  phone: "",
  address: "",
  dob: "",
};

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const validate = () => {
    const e = {};
    if (!form.name || form.name.trim().length < 2) e.name = "Name must be at least 2 characters";
    if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = "Enter a valid email";
    if (form.password.length < 8) e.password = "Password must be at least 8 characters";
    if (form.password !== form.confirmPassword) e.confirmPassword = "Passwords do not match";
    if (form.phone && !/^[0-9]{10}$/.test(form.phone)) e.phone = "Phone must be a 10-digit number";
    if (form.dob && new Date(form.dob) >= new Date()) e.dob = "Date of birth must be in the past";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setServerError("");
    if (!validate()) return;
    setLoading(true);
    try {
      const payload = { ...form };
      delete payload.confirmPassword;
      if (!payload.dob) delete payload.dob;
      await register(payload);
      navigate("/");
    } catch (err) {
      const { message, fieldErrors } = apiError(err);
      setServerError(message);
      setErrors(fieldErrors);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="form-card form-card-wide">
        <div className="form-brand-mobile">
          <span>🛡️</span> InsureDesk
        </div>
        <h2 className="form-heading">Create your account 🚀</h2>
        <p className="form-subtext">
          Join InsureDesk and protect what matters in just a few clicks.
        </p>

        {serverError && <div className="alert alert-error shake">{serverError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-grid">
            <FormField label="Full Name *" error={errors.name}>
              <input value={form.name} onChange={set("name")} placeholder="Your full name" />
            </FormField>
            <FormField label="Email *" error={errors.email}>
              <input type="email" value={form.email} onChange={set("email")} placeholder="you@example.com" />
            </FormField>
            <FormField label="Password *" error={errors.password}>
              <div className="input-icon-wrap no-left-icon">
                <input
                  type={showPassword ? "text" : "password"}
                  value={form.password}
                  onChange={set("password")}
                  placeholder="Min 8 characters"
                />
                <button
                  type="button"
                  className="input-eye"
                  title={showPassword ? "Hide password" : "Show password"}
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? "🙈" : "👁️"}
                </button>
              </div>
            </FormField>
            <FormField label="Confirm Password *" error={errors.confirmPassword}>
              <input
                type={showPassword ? "text" : "password"}
                value={form.confirmPassword}
                onChange={set("confirmPassword")}
                placeholder="Repeat password"
              />
            </FormField>
            <FormField label="Phone" error={errors.phone}>
              <input value={form.phone} onChange={set("phone")} placeholder="10-digit mobile number" />
            </FormField>
            <FormField label="Date of Birth" error={errors.dob}>
              <input type="date" value={form.dob} onChange={set("dob")} />
            </FormField>
          </div>
          <FormField label="Address" error={errors.address}>
            <input value={form.address} onChange={set("address")} placeholder="Your address" />
          </FormField>
          <button className="btn btn-primary btn-block btn-glow" disabled={loading}>
            {loading ? "Creating account..." : "Create Account →"}
          </button>
        </form>

        <div className="form-divider"><span>Already registered?</span></div>
        <Link to="/login" className="btn btn-outline btn-block">
          Sign in instead
        </Link>
      </div>
    </AuthLayout>
  );
}
