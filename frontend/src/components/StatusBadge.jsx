const COLORS = {
  PENDING: "badge-yellow",
  ACTIVE: "badge-green",
  APPROVED: "badge-green",
  PAID: "badge-green",
  SETTLED: "badge-blue",
  UNDER_REVIEW: "badge-blue",
  FILED: "badge-yellow",
  DUE: "badge-yellow",
  REJECTED: "badge-red",
  OVERDUE: "badge-red",
  CANCELLED: "badge-gray",
  EXPIRED: "badge-gray",
  INACTIVE: "badge-gray",
  LIFE: "badge-purple",
  HEALTH: "badge-teal",
  MOTOR: "badge-blue",
  HOME: "badge-orange",
  ADMIN: "badge-purple",
  AGENT: "badge-blue",
  CUSTOMER: "badge-teal",
};

export default function StatusBadge({ value }) {
  if (!value) return null;
  return (
    <span className={`badge ${COLORS[value] || "badge-gray"}`}>
      {String(value).replace("_", " ")}
    </span>
  );
}
