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
