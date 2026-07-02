<#
.SYNOPSIS
Seeds a local ClaimGuard AI backend with fake/demo claims for frontend testing.

.DESCRIPTION
Logs in through the documented local auth endpoint, generates fake healthcare
revenue-cycle claim payloads, and posts them to the backend claim creation
endpoint. This script is for local development/demo data only. Do not use real PHI.

.EXAMPLE
.\scripts\dev\seed-demo-claims.ps1

.EXAMPLE
.\scripts\dev\seed-demo-claims.ps1 -ClaimCount 50
#>

[CmdletBinding()]
param(
    [string] $ApiBaseUrl = "http://localhost:8080",

    [string] $Username = "local.analyst",

    [string] $Password = "LocalPass123!",

    [ValidateRange(20, 50)]
    [int] $ClaimCount = 30
)

$ErrorActionPreference = "Stop"

$apiRoot = $ApiBaseUrl.TrimEnd("/")
$loginUrl = "$apiRoot/api/auth/login"
$claimsUrl = "$apiRoot/api/claims"
$runId = Get-Date -Format "yyyyMMdd-HHmmss"

$providers = @(
    "Summit Demo Health",
    "North Valley Demo Clinic",
    "Riverside Demo Medical",
    "Lakeview Demo Care",
    "Pinecrest Demo Hospital",
    "Valley Demo Medical Group",
    "Harbor Demo Physicians",
    "Cedar Point Demo Clinic"
)

$payers = @(
    "Acme Health Plan",
    "HealthPlan Plus Demo",
    "BlueHealth Demo",
    "MediCare Advantage Demo",
    "Aetna Better Health Demo",
    "United Demo Care",
    "CareFirst Demo Plan",
    "Regional Demo Payer"
)

$claimTypes = @(
    "Professional",
    "Institutional",
    "Outpatient",
    "Specialty",
    "Ancillary"
)

$noteTemplates = @(
    "Demo claim intake documentation with complete administrative detail. No real PHI.",
    "Demo claim type: {0}. Service documentation appears complete for local testing.",
    "Demo reviewer scenario. Prior authorization workflow should be checked before submission.",
    "Too short",
    "Demo documentation indicates payer-specific review may be needed. No real patient data."
)

function ConvertTo-ErrorText {
    param([object] $ErrorRecord)

    $response = $ErrorRecord.Exception.Response
    if ($null -ne $response) {
        try {
            $stream = $response.GetResponseStream()
            if ($null -ne $stream) {
                $reader = [System.IO.StreamReader]::new($stream)
                $body = $reader.ReadToEnd()
                if (-not [string]::IsNullOrWhiteSpace($body)) {
                    return $body
                }
            }
        } catch {
            return $ErrorRecord.Exception.Message
        }
    }

    return $ErrorRecord.Exception.Message
}

function Invoke-ClaimGuardJson {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Uri,

        [Parameter(Mandatory = $true)]
        [string] $Method,

        [object] $Body,

        [hashtable] $Headers = @{}
    )

    $parameters = @{
        Uri         = $Uri
        Method      = $Method
        Headers     = $Headers
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $parameters.Body = ($Body | ConvertTo-Json -Depth 8)
    }

    Invoke-RestMethod @parameters
}

function Get-PatientLabel {
    param([int] $Index)

    $letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    $letterIndex = ($Index - 1) % $letters.Length
    $round = [math]::Floor(($Index - 1) / $letters.Length)
    $suffix = if ($round -gt 0) { "-$($round + 1)" } else { "" }

    "Patient $($letters[$letterIndex])$suffix"
}

function New-DemoClaimPayload {
    param([int] $Index)

    $provider = $providers[($Index - 1) % $providers.Count]
    $payer = $payers[($Index - 1) % $payers.Count]
    $claimType = $claimTypes[($Index - 1) % $claimTypes.Count]
    $serviceDate = (Get-Date).Date.AddDays(-1 * (Get-Random -Minimum 3 -Maximum 180)).ToString("yyyy-MM-dd")
    $priorAuthRequired = ($Index % 4 -eq 0) -or ($Index % 9 -eq 0)
    $omitPriorAuthNumber = $priorAuthRequired -and ($Index % 8 -eq 0)
    $omitPatientControl = $Index % 7 -eq 0
    $highDollar = $Index % 6 -eq 0
    $amountMinimum = if ($highDollar) { 10500 } else { 240 }
    $amountMaximum = if ($highDollar) { 24500 } else { 8500 }
    $amount = [decimal](Get-Random -Minimum $amountMinimum -Maximum $amountMaximum)
    $amount = [math]::Round($amount + ([decimal](Get-Random -Minimum 0 -Maximum 99) / 100), 2)
    $noteTemplate = $noteTemplates[($Index - 1) % $noteTemplates.Count]
    $claimNotes = if ($noteTemplate.Contains("{0}")) { $noteTemplate -f $claimType } else { $noteTemplate }

    [ordered]@{
        claimNumber          = "CLM-DEMO-$runId-$('{0:D3}' -f $Index)"
        patientControlNumber = if ($omitPatientControl) { $null } else { Get-PatientLabel -Index $Index }
        payerName            = $payer
        providerName         = $provider
        serviceDate          = $serviceDate
        billedAmount         = $amount
        priorAuthRequired    = $priorAuthRequired
        priorAuthNumber      = if ($omitPriorAuthNumber) { $null } elseif ($priorAuthRequired) { "PA-DEMO-$runId-$('{0:D3}' -f $Index)" } else { $null }
        claimNotes           = $claimNotes
    }
}

Write-Host "ClaimGuard AI demo claim seeder"
Write-Host "API base URL: $apiRoot"
Write-Host "Claim count: $ClaimCount"
Write-Host "Data policy: fake/demo data only. Do not use real PHI."
Write-Host ""

try {
    Invoke-RestMethod -Uri "$apiRoot/api/health" -Method Get | Out-Null
} catch {
    Write-Error "Backend is not reachable at $apiRoot. Start it with: cd backend; mvn spring-boot:run -Dspring-boot.run.profiles=local"
    exit 1
}

try {
    $loginResponse = Invoke-ClaimGuardJson -Uri $loginUrl -Method "Post" -Body @{
        username = $Username
        password = $Password
    }
} catch {
    $message = ConvertTo-ErrorText -ErrorRecord $_
    Write-Error "Login failed for user '$Username' at $loginUrl. Backend response: $message"
    exit 1
}

if ([string]::IsNullOrWhiteSpace($loginResponse.accessToken)) {
    Write-Error "Login response did not include accessToken. Cannot seed claims."
    exit 1
}

$headers = @{
    Authorization = "Bearer $($loginResponse.accessToken)"
}

$created = New-Object System.Collections.Generic.List[object]
$failed = New-Object System.Collections.Generic.List[object]

for ($i = 1; $i -le $ClaimCount; $i++) {
    $payload = New-DemoClaimPayload -Index $i

    try {
        $claim = Invoke-ClaimGuardJson -Uri $claimsUrl -Method "Post" -Body $payload -Headers $headers
        $created.Add($claim) | Out-Null
        Write-Host ("Created claim {0}: id={1}, status={2}, amount={3}, provider='{4}', payer='{5}'" -f $i, $claim.id, $claim.claimStatus, $claim.billedAmount, $claim.providerName, $claim.payerName)
    } catch {
        $message = ConvertTo-ErrorText -ErrorRecord $_
        $failed.Add([pscustomobject]@{
            index       = $i
            claimNumber = $payload.claimNumber
            error       = $message
        }) | Out-Null
        Write-Warning "Failed to create claim $i ($($payload.claimNumber)): $message"
    }
}

Write-Host ""
Write-Host "Seed summary"
Write-Host "Created: $($created.Count)"
Write-Host "Failed:  $($failed.Count)"

if ($failed.Count -gt 0) {
    Write-Host ""
    Write-Host "Failures:"
    $failed | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "Done. Open the frontend Claims and Dashboard pages to review the seeded demo data."
