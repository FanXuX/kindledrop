import fs from "node:fs";
import path from "node:path";
import os from "node:os";
import { z } from "zod";

const ConfigSchema = z.object({
  engineUrl: z.string().url().default("http://localhost:8080"),
  kindleEmail: z.string().email().optional(),
  smtp: z.object({
    host: z.string(),
    port: z.number().int().positive().default(587),
    user: z.string(),
    from: z.string().email(),
    useStartTLS: z.boolean().default(true),
    useSSL: z.boolean().default(false),
  }).optional(),
  limits: z.object({
    maxBytes: z.number().int().positive().default(30 * 1024 * 1024),
  }).optional(),
});

export type KindleDropConfig = z.infer<typeof ConfigSchema>;

export function configPath() {
  return path.join(os.homedir(), ".kindledrop", "config.json");
}

export function loadConfig(): KindleDropConfig {
  const fp = configPath();
  if (!fs.existsSync(fp)) {
    return { engineUrl: "http://localhost:8080" };
  }
  const raw = fs.readFileSync(fp, "utf8");
  const parsed = JSON.parse(raw);
  return ConfigSchema.parse(parsed);
}
