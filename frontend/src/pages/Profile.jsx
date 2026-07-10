import { useEffect, useState } from "react";
import client, { apiError } from "../api/client";
import { useToast } from "../context/ToastContext";
import FormField from "../components/FormField";

export default function Profile() {
  const toast = useToast();
  const [profile, setProfile] = useState(null);
  const [profileErrors, setProfileErrors] = useState({});
  const [pwd, setPwd] = useState({ currentPassword: "", newPassword: "", confirm: "" });
  const [pwdErrors, setPwdErrors] = useState({});

  useEffect(() => {
    client.get("/users/me").then(({ data }) => setProfile(data)).catch(() => {});
  }, []);

  const saveProfile = async (e) => {
    e.preventDefault();
    setProfileErrors({});
    try {
      const { data } = await client.put("/users/me", {
        name: profile.name,
        phone: profile.phone || undefined,
        address: profile.address || undefined,
      });
      setProfile(data);
      const stored = JSON.parse(localStorage.getItem("ims_user") || "{}");
      localStorage.setItem("ims_user", JSON.stringify({ ...stored, name: data.name }));
      toast.success("Profile updated");
    } catch (err) {
      const { message, fieldErrors } = apiError(err);
      setProfileErrors(fieldErrors);
      toast.error(message);
    }
  };

  const changePassword = async (e) => {
    e.preventDefault();
    const errs = {};
    if (pwd.newPassword.length < 8) errs.newPassword = "At least 8 characters";
    if (pwd.newPassword !== pwd.confirm) errs.confirm = "Passwords do not match";
    setPwdErrors(errs);
    if (Object.keys(errs).length > 0) return;
    try {
      await client.put("/users/me/password", {
        currentPassword: pwd.currentPassword,
        newPassword: pwd.newPassword,
      });
      setPwd({ currentPassword: "", newPassword: "", confirm: "" });
      toast.success("Password changed successfully");
    } catch (err) {
      const { message, fieldErrors } = apiError(err);
      setPwdErrors(fieldErrors);
      toast.error(message);
    }
  };

  if (!profile) return <div className="page-loading">Loading profile...</div>;

  return (
    <div>
      <h1 className="page-title">My Profile</h1>
      <div className="profile-grid">
        <div className="panel">
          <h2>Account Details</h2>
          <form onSubmit={saveProfile}>
            <FormField label="Full Name" error={profileErrors.name}>
              <input value={profile.name}
                onChange={(e) => setProfile({ ...profile, name: e.target.value })} />
            </FormField>
            <FormField label="Email (cannot be changed)">
              <input value={profile.email} disabled />
            </FormField>
            <FormField label="Role">
              <input value={profile.role} disabled />
            </FormField>
            <FormField label="Phone" error={profileErrors.phone}>
              <input value={profile.phone || ""}
                onChange={(e) => setProfile({ ...profile, phone: e.target.value })} />
            </FormField>
            <FormField label="Address" error={profileErrors.address}>
              <input value={profile.address || ""}
                onChange={(e) => setProfile({ ...profile, address: e.target.value })} />
            </FormField>
            <button className="btn btn-primary">Save Changes</button>
          </form>
        </div>

        <div className="panel">
          <h2>Change Password</h2>
          <form onSubmit={changePassword}>
            <FormField label="Current Password" error={pwdErrors.currentPassword}>
              <input type="password" value={pwd.currentPassword}
                onChange={(e) => setPwd({ ...pwd, currentPassword: e.target.value })} />
            </FormField>
            <FormField label="New Password" error={pwdErrors.newPassword}>
              <input type="password" value={pwd.newPassword}
                onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })} />
            </FormField>
            <FormField label="Confirm New Password" error={pwdErrors.confirm}>
              <input type="password" value={pwd.confirm}
                onChange={(e) => setPwd({ ...pwd, confirm: e.target.value })} />
            </FormField>
            <button className="btn btn-primary">Change Password</button>
          </form>
        </div>
      </div>
    </div>
  );
}
