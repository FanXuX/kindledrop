// src/components/SendHistory.tsx
interface Submission {
  id: string;
  url: string;
  fileName: string;
  bytes: number;
  timestamp: Date;
  status: "success" | "error";
  message?: string;
}

interface SendHistoryProps {
  submissions: Submission[];
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
}

function formatTime(date: Date): string {
  return new Intl.DateTimeFormat("en-US", {
    hour: "numeric",
    minute: "2-digit",
    second: "2-digit",
  }).format(date);
}

export default function SendHistory({ submissions }: SendHistoryProps) {
  if (submissions.length === 0) {
    return (
      <div className="history-empty">
        <p>No submissions yet. Send a file to get started!</p>
      </div>
    );
  }

  return (
    <div className="history-card">
      <div className="history-header">
        <h2>Recent Submissions</h2>
      </div>

      <div className="history-list" role="list" aria-label="Recent submissions">
        {submissions.map((sub) => (
          <div key={sub.id} className="history-item" role="listitem">
            <div className="history-row">
              <div className="history-left">
                <div className="history-title-row">
                  <span
                    className={`status-dot ${
                      sub.status === "success"
                        ? "status-success"
                        : "status-error"
                    }`}
                    aria-hidden="true"
                  />
                  <span className="history-title">
                    {sub.fileName || "Unknown file"}
                  </span>
                </div>

                {sub.url && <div className="history-url">{sub.url}</div>}

                {sub.message && (
                  <div className="history-message">{sub.message}</div>
                )}
              </div>

              <div className="history-meta">
                <div>{formatBytes(sub.bytes)}</div>
                <div className="history-time">{formatTime(sub.timestamp)}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
