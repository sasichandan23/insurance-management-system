import { createContext, useContext, useState } from "react";
import client from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("ims_user");
    return stored ? JSON.parse(stored) : null;
  });

  const saveSession = (data) => {
    const sessionUser = {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    };
    localStorage.setItem("ims_token", data.token);
    localStorage.setItem("ims_user", JSON.stringify(sessionUser));
    setUser(sessionUser);
    return sessionUser;
  };

  const login = async (email, password) => {
    const { data } = await client.post("/auth/login", { email, password });
    return saveSession(data);
  };

  const register = async (form) => {
    const { data } = await client.post("/auth/register", form);
    return saveSession(data);
  };

  const logout = () => {
    localStorage.removeItem("ims_token");
    localStorage.removeItem("ims_user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
