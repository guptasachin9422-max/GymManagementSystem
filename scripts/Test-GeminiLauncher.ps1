# Offline regression checks: no real key, network requests, or backend startup.
$ErrorActionPreference = 'Stop'
$launcher = Join-Path $PSScriptRoot 'Start-FitLife.ps1'
$global:FitLifeLauncherTestState = @{ Key = 'local-test-placeholder'; Statuses = @(); Requests = 0; EmptyResponse = $false }
$originalKey = $env:GEMINI_API_KEY
$originalModel = $env:GEMINI_MODEL
$originalTls = [Net.ServicePointManager]::SecurityProtocol
$script:passed = 0

function Read-Host {
    param([string]$Prompt, [switch]$AsSecureString)
    if (-not $AsSecureString) { throw 'Key input must be hidden.' }
    ConvertTo-SecureString $global:FitLifeLauncherTestState.Key -AsPlainText -Force
}
function Start-Sleep { param([int]$Seconds) }
function Invoke-RestMethod {
    [CmdletBinding()]
    param($Method, $Uri, $Headers, $ContentType, $Body, $TimeoutSec)
    $status = $global:FitLifeLauncherTestState.Statuses[$global:FitLifeLauncherTestState.Requests]
    $global:FitLifeLauncherTestState.Requests++
    if ($Uri -ne 'https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent') { throw 'Unexpected request URL.' }
    if ($Headers['x-goog-api-key'] -ne $global:FitLifeLauncherTestState.Key) { throw 'Missing key header.' }
    if ($Uri.Contains($global:FitLifeLauncherTestState.Key) -or $Body.Contains($global:FitLifeLauncherTestState.Key)) { throw 'Key leaked into URL/body.' }
    if (($Body | ConvertFrom-Json).contents[0].parts[0].text -ne 'Reply with just OK.') { throw 'Unexpected test prompt.' }
    if ($status -ne 200) {
        $failure = [Exception]::new('PRIVATE_PROVIDER_BODY_SHOULD_NOT_BE_PRINTED')
        $failure | Add-Member -NotePropertyName Response -NotePropertyValue ([pscustomobject]@{ StatusCode = $status })
        throw $failure
    }
    if ($global:FitLifeLauncherTestState.EmptyResponse) { return @{ candidates = @() } }
    return @{ candidates = @(@{ content = @{ parts = @(@{ text = 'OK'; thought = $false }) } }) }
}

function Test-Scenario {
    param([string]$Name, [int[]]$Statuses, [string]$ExpectedError, [int]$ExpectedRequests, [bool]$Empty = $false)
    $global:FitLifeLauncherTestState.Statuses = $Statuses
    $global:FitLifeLauncherTestState.Requests = 0
    $global:FitLifeLauncherTestState.EmptyResponse = $Empty
    $failure = ''
    $output = @()
    try { $output = @(& $launcher -CheckOnly -Model 'gemini-3.1-flash-lite' 6>&1) }
    catch { $failure = $_.Exception.Message }
    if ($ExpectedError -and $failure -notlike "*$ExpectedError*") { throw "$Name failed: expected a sanitized $ExpectedError error." }
    if (-not $ExpectedError -and $failure) { throw "$Name failed unexpectedly: $failure" }
    if ($global:FitLifeLauncherTestState.Requests -ne $ExpectedRequests) { throw "$Name sent an unexpected number of requests." }
    if ($env:GEMINI_API_KEY -ne 'existing-test-key' -or $env:GEMINI_MODEL -ne 'existing-test-model') { throw "$Name did not restore the environment." }
    if ([Net.ServicePointManager]::SecurityProtocol -ne $originalTls) { throw "$Name did not restore TLS settings." }
    $printed = ($output | Out-String) + $failure
    if ($printed.Contains($global:FitLifeLauncherTestState.Key) -or $printed.Contains('PRIVATE_PROVIDER_BODY_SHOULD_NOT_BE_PRINTED')) { throw "$Name exposed sensitive output." }
    $script:passed++
    Write-Host "PASS: $Name"
}

try {
    $env:GEMINI_API_KEY = 'existing-test-key'
    $env:GEMINI_MODEL = 'existing-test-model'
    Test-Scenario 'Two successful requests' @(200, 200) '' 2
    Test-Scenario 'First-request authentication failure' @(401) 'HTTP 401' 1
    Test-Scenario 'Second-request authentication failure' @(200, 401) 'HTTP 401' 2
    Test-Scenario 'Permission failure' @(403) 'HTTP 403' 1
    Test-Scenario 'Quota failure' @(429) 'HTTP 429' 1
    Test-Scenario 'Unavailable model' @(404) 'HTTP 404' 1
    Test-Scenario 'Provider unavailable' @(503) 'HTTP 503' 1
    Test-Scenario 'Empty model answer' @(200) 'returned no text' 1 $true
    Write-Host "All $script:passed offline launcher checks passed. No real credentials or API requests were used."
} finally {
    $env:GEMINI_API_KEY = $originalKey
    $env:GEMINI_MODEL = $originalModel
    Remove-Variable FitLifeLauncherTestState -Scope Global
}
