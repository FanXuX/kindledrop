import { FormEvent, useState } from "react";
import { apiClient } from "../lib/apiClient";

interface SendFormProps {
  onSuccess: (result: {
    ok: boolean;
    resolvedUrl: string;
    fileName: string;
    bytes: number;
    message?: string;
  }) => void;
  onError: (error: string) => void;
}

export default function SendForm({ onSuccess, onError }: SendFormProps) {
  const [url, setUrl] = useState("");
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const result = await apiClient.send({ url, email });
      onSuccess(result);
      setUrl("");
      setEmail("");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Unknown error";
      setError(message);
      onError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="comic-inner">
      <div className="input-group stagger-1">
        <label htmlFor="url">GitHub File URL</label>
        <input
          id="url"
          type="url"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          placeholder="https://github.com/..."
          required
        />
      </div>

      <div className="input-group stagger-2">
        <label htmlFor="email">Kindle Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@kindle.com"
          required
        />
      </div>

      <div className="btn-container">
        <button
          type="submit"
          id="submitBtn"
          onMouseDown={() => {
            document.body.style.backgroundColor = "var(--cyan)";
          }}
          onMouseUp={() => {
            document.body.style.backgroundColor = "var(--paper)";
          }}
          disabled={loading}
        >
          {loading ? "SENDING..." : "Send to Kindle"}
        </button>
      </div>

      {error && (
        <div className="error-box">
          <p className="error-text">{error}</p>
        </div>
      )}
    </form>
  );
}
