/** Split-screen layout for login/register: animated brand hero + form panel. */
export default function AuthLayout({ children }) {
  return (
    <div className="auth-split">
      <div className="auth-hero">
        <div className="hero-blob blob-1" />
        <div className="hero-blob blob-2" />
        <div className="hero-blob blob-3" />

        <div className="hero-content">
          <div className="hero-logo">
            <span className="hero-shield">🛡️</span>
            <span className="hero-title">InsureDesk</span>
          </div>

          <h1 className="hero-heading">
            Protect what
            <br />
            matters <span className="hero-accent">most.</span>
          </h1>
          <p className="hero-sub">
            One secure platform for all your insurance needs — buy policies,
            pay premiums, and track claims in real time.
          </p>

          <div className="hero-features">
            <div className="hero-feature" style={{ animationDelay: "0.9s" }}>
              <span className="feature-check">✓</span> Life, Health, Motor &amp; Home insurance
            </div>
            <div className="hero-feature" style={{ animationDelay: "1.1s" }}>
              <span className="feature-check">✓</span> Instant claim filing and live status tracking
            </div>
            <div className="hero-feature" style={{ animationDelay: "1.3s" }}>
              <span className="feature-check">✓</span> Dedicated agent support for every customer
            </div>
          </div>

          <div className="hero-cards">
            <div className="float-card float-1">
              <span className="float-icon">❤️</span>
              <div>
                <div className="float-label">Life Insurance</div>
                <div className="float-sub">Secure their future</div>
              </div>
            </div>
            <div className="float-card float-2">
              <span className="float-icon">🏥</span>
              <div>
                <div className="float-label">Health Cover</div>
                <div className="float-sub">Family protection</div>
              </div>
            </div>
            <div className="float-card float-3">
              <span className="float-icon">🚗</span>
              <div>
                <div className="float-label">Motor Insurance</div>
                <div className="float-sub">Drive worry-free</div>
              </div>
            </div>
            <div className="float-card float-4">
              <span className="float-icon">🏠</span>
              <div>
                <div className="float-label">Home Shield</div>
                <div className="float-sub">Guard your nest</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="auth-form-side">{children}</div>
    </div>
  );
}
