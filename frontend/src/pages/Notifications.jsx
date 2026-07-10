import { useCallback, useEffect, useState } from "react";
import client from "../api/client";
import Pagination from "../components/Pagination";

export default function Notifications() {
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(0);

  const load = useCallback(() => {
    client.get("/notifications", { params: { page, size: 10 } })
      .then(({ data }) => setPageData(data))
      .catch(() => {});
  }, [page]);

  useEffect(load, [load]);

  const markRead = async (n) => {
    if (n.read) return;
    await client.put(`/notifications/${n.id}/read`).catch(() => {});
    load();
  };

  const markAll = async () => {
    await client.put("/notifications/read-all").catch(() => {});
    load();
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Notifications</h1>
        <button className="btn btn-outline" onClick={markAll}>Mark all as read</button>
      </div>
      <div className="notification-list">
        {(pageData?.content || []).map((n) => (
          <div
            key={n.id}
            className={`notification-item ${n.read ? "" : "unread"}`}
            onClick={() => markRead(n)}
          >
            <div className="notification-title">
              {!n.read && <span className="dot" />} {n.title}
            </div>
            <div className="notification-message">{n.message}</div>
            <div className="notification-time">{new Date(n.createdAt).toLocaleString()}</div>
          </div>
        ))}
        {pageData?.content?.length === 0 && <p className="muted">No notifications yet.</p>}
      </div>
      <Pagination page={page} totalPages={pageData?.totalPages || 0} onChange={setPage} />
    </div>
  );
}
