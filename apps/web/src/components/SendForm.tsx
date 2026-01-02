import { FormEvent, useEffect, useMemo, useState } from "react";
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

const STORAGE_KEY = "kindleEmail";

function safeSessionGet(key: string): string | null {
  try {
    return sessionStorage.getItem(key);
  } catch {
    return null;
  }
}

function safeSessionSet(key: string, value: string) {
  try {
    sessionStorage.setItem(key, value);
  } catch {}
}

function safeSessionRemove(key: string) {
  try {
    sessionStorage.removeItem(key);
  } catch {}
}

export default function SendForm({ onSuccess, onError }: SendFormProps) {
  const [url, setUrl] = useState("");
  const [email, setEmail] = useState("");
  const [remember, setRemember] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canClearSaved = remember && email.trim().length > 0;

  useEffect(() => {
    const saved = safeSessionGet(STORAGE_KEY);
    if (saved) {
      setEmail(saved);
      setRemember(true);
    }
  }, []);

  // Keep session storage in sync when the user toggles remember or changes email.
  // (Only saves when remember is enabled.)
  useEffect(() => {
    if (!remember) {
      safeSessionRemove(STORAGE_KEY);
      return;
    }
    if (email.trim().length > 0) {
      safeSessionSet(STORAGE_KEY, email.trim());
    }
  }, [remember, email]);

  const handleClearSaved = () => {
    safeSessionRemove(STORAGE_KEY);
    setRemember(false);
    setEmail("");
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const result = await apiClient.send({ url, email });
      onSuccess(result);
      setUrl("");

      // If not remembering, also clear email field (storage already handled by effect)
      if (!remember) setEmail("");
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

      {/* Session settings row (its own row) */}
      <div className="sendform__remember-row">
        <label className="sendform__remember-label">
          <input
            className="sendform__remember-checkbox"
            type="checkbox"
            checked={remember}
            onChange={(e) => setRemember(e.target.checked)}
          />
          <span className="sendform__remember-text">
            Save email for this session
          </span>
        </label>

        <button
          type="button"
          className="sendform__clear-btn"
          onClick={handleClearSaved}
          disabled={!canClearSaved}
          aria-disabled={!canClearSaved}
        >
          Clear saved
        </button>
      </div>

      {/* Actions row */}
      <div className="sendform__controls-row">
        <div className="sendform__actions">
          <button
            type="submit"
            id="submitBtn"
            className="sendform__submit"
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
      </div>

      {error && (
        <div className="error-box" role="alert" aria-live="polite">
          <p className="error-text">{error}</p>
        </div>
      )}
    </form>
  );
}
