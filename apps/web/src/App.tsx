import { useState } from "react";
import SendForm from "./components/SendForm";
import SendHistory from "./components/SendHistory";

export default function App() {
  const [submissions, setSubmissions] = useState<
    Array<{
      id: string;
      url: string;
      fileName: string;
      bytes: number;
      timestamp: Date;
      status: "success" | "error";
      message?: string;
    }>
  >([]);

  const handleSendSuccess = (result: {
    ok: boolean;
    resolvedUrl: string;
    fileName: string;
    bytes: number;
    message?: string;
  }) => {
    setSubmissions([
      {
        id: Date.now().toString(),
        url: result.resolvedUrl,
        fileName: result.fileName,
        bytes: result.bytes,
        timestamp: new Date(),
        status: result.ok ? "success" : "error",
        message: result.message,
      },
      ...submissions,
    ]);
  };

  const handleSendError = (error: string) => {
    setSubmissions([
      {
        id: Date.now().toString(),
        url: "",
        fileName: "",
        bytes: 0,
        timestamp: new Date(),
        status: "error",
        message: error,
      },
      ...submissions,
    ]);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="container mx-auto px-4 py-8">
        <header className="mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">KindleDrop</h1>
          <p className="text-lg text-gray-600">
            Send GitHub files directly to your Kindle
          </p>
        </header>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-1">
            <SendForm onSuccess={handleSendSuccess} onError={handleSendError} />
          </div>
          <div className="lg:col-span-2">
            <SendHistory submissions={submissions} />
          </div>
        </div>
      </div>
    </div>
  );
}
