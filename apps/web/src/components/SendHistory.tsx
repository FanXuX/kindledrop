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
      <div className="bg-white rounded-lg shadow-lg p-8 text-center">
        <p className="text-gray-500 text-lg">
          No submissions yet. Send a file to get started!
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200">
        <h2 className="text-2xl font-bold text-gray-900">Recent Submissions</h2>
      </div>
      <div className="divide-y divide-gray-200">
        {submissions.map((sub) => (
          <div key={sub.id} className="px-6 py-4 hover:bg-gray-50 transition">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span
                    className={`inline-block w-2 h-2 rounded-full ${
                      sub.status === "success" ? "bg-green-500" : "bg-red-500"
                    }`}
                  />
                  <h3 className="font-semibold text-gray-900">
                    {sub.fileName || "Unknown file"}
                  </h3>
                </div>
                {sub.url && (
                  <p className="text-sm text-gray-600 mb-1 break-all">
                    {sub.url}
                  </p>
                )}
                {sub.message && (
                  <p
                    className={`text-sm ${
                      sub.status === "error" ? "text-red-600" : "text-gray-600"
                    }`}
                  >
                    {sub.message}
                  </p>
                )}
              </div>
              <div className="text-right ml-4">
                <p className="text-sm font-medium text-gray-600">
                  {formatBytes(sub.bytes)}
                </p>
                <p className="text-xs text-gray-500">
                  {formatTime(sub.timestamp)}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
