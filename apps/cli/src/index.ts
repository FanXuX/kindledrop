#!/usr/bin/env node
import { Command } from "commander";
import ora from "ora";
import { loadConfig } from "./config.js";
import { buildRequest, sendToKindle } from "./api.js";

const program = new Command();

program
  .name("stk")
  .description("KindleDrop CLI — Drop a GitHub link. Read it on your Kindle.")
  .version("0.1.0");

program
  .command("send")
  .argument("<url>", "GitHub file URL (blob or raw)")
  .option("--to <email>", "Kindle email (overrides config)")
  .option("--engine <url>", "Engine URL (overrides config)")
  .option("--dry-run", "Resolve + validate only (no download/email)", false)
  .option("-v, --verbose", "Verbose output", false)
  .action(async (url: string, opts: any) => {
    const cfg = loadConfig();
    const engineUrl = opts.engine ?? cfg.engineUrl ?? "http://localhost:8080";
    const kindleEmail = opts.to ?? cfg.kindleEmail;

    if (!kindleEmail) {
      console.error("Missing Kindle email. Provide --to or set kindleEmail in ~/.kindledrop/config.json");
      process.exit(2);
    }

    if (!opts.dryRun && !cfg.smtp) {
      console.error("Missing SMTP config in ~/.kindledrop/config.json (smtp.host/port/user/from)");
      process.exit(2);
    }

    const spinner = ora("Sending to engine...").start();
    try {
      const req = buildRequest(cfg, { url, kindleEmail, dryRun: !!opts.dryRun, verbose: !!opts.verbose });

      if (opts.verbose) {
        spinner.stop();
        console.log("Engine:", engineUrl);
        console.log("Request:", JSON.stringify({ ...req, smtp: req.smtp ? { ...req.smtp, password: req.smtp.password ? "***" : undefined } : undefined }, null, 2));
        spinner.start("Sending to engine...");
      }

      const res = await sendToKindle(engineUrl, req);

      if (!res.ok) {
        spinner.fail(res.message || "Failed.");
        if (opts.verbose) console.log(res);
        process.exit(1);
      }

      if (opts.dryRun) {
        spinner.succeed(`Dry run OK. Resolved: ${res.resolvedUrl}`);
        console.log(`File: ${res.fileName}`);
        return;
      }

      spinner.succeed(`Sent ✅  ${res.fileName} (${formatBytes(res.bytes)})`);
      console.log(`Resolved: ${res.resolvedUrl}`);
    } catch (err: any) {
      spinner.fail(err?.response?.data?.message ?? err?.message ?? "Unknown error");
      if (opts.verbose) {
        console.error(err?.response?.data ?? err);
      }
      process.exit(1);
    }
  });

program.parse(process.argv);

function formatBytes(n: number): string {
  if (!Number.isFinite(n) || n <= 0) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  let u = 0;
  let x = n;
  while (x >= 1024 && u < units.length - 1) {
    x /= 1024;
    u++;
  }
  return `${x.toFixed(u === 0 ? 0 : 1)} ${units[u]}`;
}
