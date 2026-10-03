# Start FitLife with a privately entered Gemini key

If a key has been posted in chat, source code, or a screenshot, revoke it in [Google AI Studio](https://aistudio.google.com/apikey) and create a replacement. Do not send the replacement in chat.

## Windows PowerShell

Stop any existing FitLife backend first. From the backend folder (the folder containing `pom.xml`), run:

```powershell
powershell -NoProfile -File .\scripts\Start-FitLife.ps1
```

Paste the replacement key at the **hidden local prompt**, then press Enter. This does not save the key in command history, a project file, or command-line arguments.

The launcher:

1. Makes two small, sequential Gemini requests containing only `Reply with just OK.` No gym/member data is included. These requests consume a small amount of API quota and may be billable if your project uses a paid tier.
2. Stops with sanitized guidance if either request fails. It does not print the key or Google's raw error payload.
3. Starts the existing Maven/Spring Boot application with `GEMINI_API_KEY` and `GEMINI_MODEL` in its child-process environment. JDK 17 or newer and the normal database configuration are still required.
4. Restores the terminal's original environment when the script ends. Keep the terminal open while using the backend; Ctrl+C stops it.

If Windows blocks local scripts, and your organization's policy permits it, use this **single-process** override (it does not change the global execution policy):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-FitLife.ps1
```

Check Gemini without starting Java:

```powershell
powershell -NoProfile -File .\scripts\Start-FitLife.ps1 -CheckOnly
```

The model defaults to the existing `GEMINI_MODEL` environment variable, or `gemini-3.1-flash-lite`, matching the current backend default. Override it only if needed with `-Model <available-model-id>`.

## Interpreting errors

- **401:** Gemini authentication failed. Inspect the key status in AI Studio; replace blocked/revoked/exposed keys and secure unrestricted standard keys.
- **403:** Check API permissions and application restrictions. Browser website-referrer restrictions are not suitable for a server-side call.
- **404:** Check the model's availability for your project.
- **429:** Check project quota/rate limits. Rotating keys does not reset project quota.
- **503:** Provider availability issue; try later.
- **0:** No HTTP response was available (for example a network/TLS/timeout failure).

If both checks pass but the application still reports 401, confirm that the frontend is talking to the backend started by this script, not an old process or remote deployment. Do not put the Gemini key in React's `.env` or any `VITE_` variable. This launcher does not modify an IDE run configuration.

## Offline verification

```powershell
powershell -NoProfile -File .\scripts\Test-GeminiLauncher.ps1
```

Tests replace the HTTP call and hidden-input prompt with local fixtures. They cover two successful calls, first/second-call 401, permission/quota/model/provider errors, empty output, environment restoration, and suppressed sensitive error output. They do **not** prove that a real key is valid or that the database/backend can start.

## Hosted chat: temporary Gemini overload

`Gemini is experiencing high demand ... (HTTP 503)` is the backend's message for an HTTP 503 returned by the Gemini endpoint. It differs from a Railway application-startup 503 and from a Gemini credential rejection (401/403). Key rotation is not a remedy for overload.

The chat backend now makes at most **three attempts** for Gemini HTTP 500/502/503/504, with roughly 1-second then 2-second backoff plus a small random delay. All attempts share the original **45-second total HTTP time budget**. A valid `Retry-After` is respected; if it asks for more than 5 seconds or exceeds the remaining budget, the backend stops rather than retrying too early or waiting indefinitely.

Authentication, invalid-model, and quota responses are not automatically retried. Ambiguous network failures also are not replayed. The model, API key, and account context remain unchanged. Retry logs contain only status codes, attempt counts, and delay times, not credentials, prompts, or provider response bodies. The local preflight launcher above intentionally still reports failures immediately so configuration problems remain visible.

Persistent provider overload can still fail after these bounded attempts. Wait briefly and try again; check [Gemini service status](https://aistudio.google.com/status) if failures persist. A different model may help only if it is available to your project; set `GEMINI_MODEL` in Railway's **backend** service and deploy the variable change. Do not change providers or send member data to a new service without reviewing that decision.

For the code fix to affect Railway, commit/push the backend changes and deploy that revision. A local edit alone does not update a running deployment.

Run the focused Java regression tests from the backend folder:

```powershell
.\mvnw.cmd "-Dtest=AiServiceTest,GeminiRequestExecutorTest" test
```

These tests mock Gemini and repositories; they require no real API key or database. They verify recovery after a 503, capped attempts/delays, unchanged request credentials/payload, the total deadline, `Retry-After`, interruption, and non-retryable errors. They do not verify a live Railway deployment.
