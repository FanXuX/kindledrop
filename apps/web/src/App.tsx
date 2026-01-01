import { useState, useRef } from "react";
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
    <div className="container">
      <div className="header-box">
        <h1>Kindle-Drop!</h1>
      </div>

      <div
        className="comic-frame"
        ref={useRef<HTMLDivElement | null>(null)}
        onMouseMove={(e) => {
          const el = e.currentTarget as HTMLDivElement;
          const rect = el.getBoundingClientRect();
          const x = e.clientX - rect.left;
          const y = e.clientY - rect.top;
          const centerX = rect.width / 2;
          const centerY = rect.height / 2;
          const rotateX = (y - centerY) / 50;
          const rotateY = (centerX - x) / 50;
          el.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) rotate(-1deg)`;
        }}
        onMouseLeave={(e) => {
          const el = e.currentTarget as HTMLDivElement;
          el.style.transform = `perspective(1000px) rotateX(0deg) rotateY(0deg) rotate(-1deg)`;
        }}
      >
        <div className="halftone-lattice" />

        <div className="form-content">
          <SendForm onSuccess={handleSendSuccess} onError={handleSendError} />
        </div>

        <div className="monologue">
          "THE DATA MUST FLOW THROUGH THE PERFORATED GRID BEFORE THE INK DRIES!"
        </div>
      </div>

      <div className="history-panel">
        <SendHistory submissions={submissions} />
      </div>
    </div>
  );
}
