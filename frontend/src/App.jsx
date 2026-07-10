import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import Layout from "./components/Layout";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Plans from "./pages/Plans";
import MyPolicies from "./pages/MyPolicies";
import PolicyList from "./pages/PolicyList";
import MyClaims from "./pages/MyClaims";
import ClaimList from "./pages/ClaimList";
import MyPayments from "./pages/MyPayments";
import Users from "./pages/Users";
import Assignments from "./pages/Assignments";
import MyCustomers from "./pages/MyCustomers";
import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/plans" element={<Plans />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/profile" element={<Profile />} />

          {/* Customer */}
          <Route element={<ProtectedRoute roles={["CUSTOMER"]} />}>
            <Route path="/my-policies" element={<MyPolicies />} />
            <Route path="/my-claims" element={<MyClaims />} />
            <Route path="/my-payments" element={<MyPayments />} />
          </Route>

          {/* Agent + Admin */}
          <Route element={<ProtectedRoute roles={["ADMIN", "AGENT"]} />}>
            <Route path="/policies" element={<PolicyList />} />
            <Route path="/claims" element={<ClaimList />} />
          </Route>

          {/* Agent */}
          <Route element={<ProtectedRoute roles={["AGENT"]} />}>
            <Route path="/my-customers" element={<MyCustomers />} />
          </Route>

          {/* Admin */}
          <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
            <Route path="/users" element={<Users />} />
            <Route path="/assignments" element={<Assignments />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
