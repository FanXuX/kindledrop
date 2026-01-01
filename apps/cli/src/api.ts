import axios from "axios";
import type { KindleDropConfig } from "./config.js";

export type SendRequest = {
  url: string;
  kindleEmail: string;
  dryRun: boolean;
  smtp?: {
    host: string;
    port: number;
    user: string;
    from: string;
    useStartTLS: boolean;
    useSSL: boolean;
    password?: string;
  };
  limits?: {
    maxBytes: number;
  };
};

export type SendResponse = {
  ok: boolean;
  resolvedUrl: string;
  fileName: string;
  bytes: number;
  message: string;
};

export async function sendToKindle(engineUrl: string, req: SendRequest): Promise<SendResponse> {
  const url = new URL("/api/send", engineUrl).toString();
  const { data } = await axios.post(url, req, { timeout: 120_000 });
  return data as SendResponse;
}

export function buildRequest(cfg: KindleDropConfig, input: {
  url: string;
  kindleEmail: string;
  dryRun: boolean;
  verbose: boolean;
}) : SendRequest {
  const smtpPass = process.env.KINDLEDROP_SMTP_PASS;

  return {
    url: input.url,
    kindleEmail: input.kindleEmail,
    dryRun: input.dryRun,
    smtp: cfg.smtp ? {
      ...cfg.smtp,
      password: smtpPass, // optional; engine also reads env fallback
    } : undefined,
    limits: cfg.limits ? { ...cfg.limits } : undefined,
  };
}
