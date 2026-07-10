import axios from "axios";

export const API_BASE = "http://localhost:8081/api";

const client = axios.create({ baseURL: API_BASE });

// Attach the JWT to every request
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("ims_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401 (expired/invalid token) force re-login
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url || "";
    if (status === 401 && !url.includes("/auth/")) {
      localStorage.removeItem("ims_token");
      localStorage.removeItem("ims_user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

/** Extracts a readable message (and field errors) from an API error. */
export function apiError(error) {
  const data = error.response?.data;
  return {
    message: data?.message || error.message || "Something went wrong",
    fieldErrors: data?.fieldErrors || {},
  };
}

export default client;
