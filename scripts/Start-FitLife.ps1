# Enter a replacement Gemini key locally; never pass it as a command argument.
[CmdletBinding()]
param(
    [switch]$CheckOnly,
    [string]$Model = $(if ($env:GEMINI_MODEL) { $env:GEMINI_MODEL } else { 'gemini-3.1-flash-lite' })
)

$ErrorActionPreference = 'Stop'
$backendDirectory = Split-Path -Parent $PSScriptRoot
$previousKey = $env:GEMINI_API_KEY
$previousModel = $env:GEMINI_MODEL
$previousTls = [Net.ServicePointManager]::SecurityProtocol
$key = $null
$secret = $null
$headers = $null

try {
    if ($Model -notmatch '^[a-zA-Z0-9._-]+$') {
        throw 'Use a Gemini model ID, not a URL (for example gemini-3.1-flash-lite).'
    }
    if (-not $CheckOnly -and -not $env:JAVA_HOME -and -not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java is not configured. Install/configure JDK 17 or newer, or use -CheckOnly to test Gemini without starting the backend.'
    }

    Write-Host 'Use a NEW key if your previous key was shared in chat or published.'
    $secret = Read-Host 'Enter your Gemini API key (hidden)' -AsSecureString
    $key = ([System.Net.NetworkCredential]::new('', $secret)).Password.Trim()
    if ([string]::IsNullOrWhiteSpace($key)) {
        throw 'No key entered. Nothing was sent to Gemini.'
    }

    # Send only a generic test prompt, never gym/member records.
    $headers = @{ 'x-goog-api-key' = $key }
    $body = @{ contents = @(@{ role = 'user'; parts = @(@{ text = 'Reply with just OK.' }) }) } | ConvertTo-Json -Depth 6
    $uri = 'https://generativelanguage.googleapis.com/v1beta/models/' + $Model + ':generateContent'
    [Net.ServicePointManager]::SecurityProtocol = $previousTls -bor [Net.SecurityProtocolType]::Tls12

    # Check twice to detect the reported first-request/second-request failure.
    for ($attempt = 1; $attempt -le 2; $attempt++) {
        try {
            $response = Invoke-RestMethod -Method Post -Uri $uri -Headers $headers -ContentType 'application/json' -Body $body -TimeoutSec 45 -Verbose:$false -Debug:$false
        } catch {
            $status = 0
            if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
                $status = [int]$_.Exception.Response.StatusCode
            }
            $advice = switch ($status) {
                400 { 'Check the model and key in Google AI Studio. The request was rejected.' }
                401 { 'Gemini rejected authentication. Check whether the key is blocked, revoked, or unrestricted in AI Studio; use a new Gemini key.' }
                403 { 'Check Generative Language API access and key restrictions. Website-referrer restrictions are not suitable for this backend.' }
                404 { 'This model is unavailable. Set GEMINI_MODEL or pass -Model with a model available to your project.' }
                429 { 'The project hit a rate/quota limit. Check AI Studio usage and wait before retrying; replacing a key does not reset project quota.' }
                503 { 'Gemini is temporarily unavailable. Retry later.' }
                default { 'Check your internet connection, Google service status, and project configuration.' }
            }
            # Do not print the raw exception, headers, or response body: they can contain secrets.
            throw "Gemini check $attempt failed (HTTP $status; 0 means no HTTP response). $advice"
        }
        $answer = @($response.candidates | Select-Object -First 1 | ForEach-Object { $_.content.parts } | Where-Object { $_.text -and -not $_.thought })
        if ($answer.Count -eq 0) {
            throw "Gemini check $attempt returned no text. The backend has not been started. Check model availability and safety settings."
        }
        Write-Host "Gemini check $attempt/2 succeeded."
        if ($attempt -eq 1) { Start-Sleep -Seconds 2 }
    }

    if ($CheckOnly) {
        Write-Host 'Both requests succeeded. Run this script without -CheckOnly to start FitLife.'
        return
    }

    # The Java child inherits these variables. No key is written to disk or CLI arguments.
    $env:GEMINI_API_KEY = $key
    $env:GEMINI_MODEL = $Model
    Write-Host 'Starting FitLife with the verified key. Keep this terminal open; Ctrl+C stops the backend.'
    Push-Location $backendDirectory
    try {
        & '.\mvnw.cmd' 'spring-boot:run'
        if ($LASTEXITCODE -ne 0) {
            throw 'Backend startup failed. Check the Maven/Java/database error above; Gemini verification already succeeded.'
        }
    } finally {
        Pop-Location
    }
} finally {
    $env:GEMINI_API_KEY = $previousKey
    $env:GEMINI_MODEL = $previousModel
    [Net.ServicePointManager]::SecurityProtocol = $previousTls
    if ($headers) { $headers.Clear() }
    if ($secret) { $secret.Dispose() }
    $key = $null
    $secret = $null
}
