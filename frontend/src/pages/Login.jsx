import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiError } from "../api/client";
import AuthLayout from "../components/AuthLayout";
import FormField from "../components/FormField";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const validate = () => {
    const e = {};
    if (!form.email) e.email = "Email is required";
    else if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = "Enter a valid email";
    if (!form.password) e.password = "Password is required";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setServerError("");
    if (!validate()) return;
    setLoading(true);
    try {
      await login(form.email, form.password);
      navigate("/");
    } catch (err) {
      setServerError(apiError(err).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout>
      <div className="form-card">
        <div className="form-brand-mobile">
          <span>🛡️</span> InsureDesk
        </div>
        <h2 className="form-heading">Welcome back 👋</h2>
        <p className="form-subtext">Sign in to manage your policies, claims and payments.</p>

        {serverError && <div className="alert alert-error shake">{serverError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <FormField label="Email" error={errors.email}>
            <div className="input-icon-wrap">
              <span className="input-icon">✉️</span>
              <input
                type="email"
                value={form.email}
                placeholder="you@example.com"
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </div>
          </FormField>
          <FormField label="Password" error={errors.password}>
            <div className="input-icon-wrap">
              <span className="input-icon">🔒</span>
              <input
                type={showPassword ? "text" : "password"}
                value={form.password}
                placeholder="Your password"
                onChange={(e) => setForm({ ...form, password: e.target.value })}
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
          <button className="btn btn-primary btn-block btn-glow" disabled={loading}>
            {loading ? "Signing in..." : "Sign In →"}
          </button>
        </form>

        <div className="form-divider"><span>New to InsureDesk?</span></div>
        <Link to="/register" className="btn btn-outline btn-block">
          Create a free account
        </Link>
      </div>
    </AuthLayout>
  );
}
