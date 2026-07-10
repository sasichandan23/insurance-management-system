import { useEffect, useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import client from "../api/client";

const NAV = {
  ADMIN: [
    { to: "/", label: "Dashboard", icon: "📊" },
    { to: "/plans", label: "Plans", icon: "📋" },
    { to: "/policies", label: "Policies", icon: "📄" },
    { to: "/claims", label: "Claims", icon: "🧾" },
    { to: "/users", label: "Users", icon: "👥" },
    { to: "/assignments", label: "Agent Assignment", icon: "🤝" },
  ],
  AGENT: [
    { to: "/", label: "Dashboard", icon: "📊" },
    { to: "/plans", label: "Plans", icon: "📋" },
    { to: "/policies", label: "Policies", icon: "📄" },
    { to: "/claims", label: "Claims", icon: "🧾" },
    { to: "/my-customers", label: "My Customers", icon: "👥" },
  ],
  CUSTOMER: [
    { to: "/", label: "Dashboard", icon: "📊" },
    { to: "/plans", label: "Browse Plans", icon: "📋" },
    { to: "/my-policies", label: "My Policies", icon: "📄" },
    { to: "/my-claims", label: "My Claims", icon: "🧾" },
    { to: "/my-payments", label: "My Payments", icon: "💳" },
  ],
};

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [unread, setUnread] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    client
      .get("/notifications/unread-count")
      .then(({ data }) => setUnread(data.count))
      .catch(() => {});
  }, [location]);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const links = NAV[user.role] || [];

  return (
    <div className="app-shell">
      <aside className={`sidebar ${menuOpen ? "open" : ""}`}>
        <div className="sidebar-brand">
          <span className="brand-icon">🛡️</span>
          <span>InsureDesk</span>
        </div>
        <div className="sidebar-role">{user.role} PORTAL</div>
        <nav>
          {links.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
              onClick={() => setMenuOpen(false)}
            >
              <span className="nav-icon">{item.icon}</span> {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <button className="hamburger" onClick={() => setMenuOpen(!menuOpen)}>
            ☰
          </button>
          <div className="topbar-spacer" />
          <NavLink to="/notifications" className="bell" title="Notifications">
            🔔{unread > 0 && <span className="bell-badge">{unread}</span>}
          </NavLink>
          <NavLink to="/profile" className="topbar-user" title="My profile">
            <span className="avatar">{user.name.charAt(0).toUpperCase()}</span>
            <span className="topbar-name">{user.name}</span>
          </NavLink>
          <button className="btn btn-outline btn-sm" onClick={handleLogout}>
            Logout
          </button>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
