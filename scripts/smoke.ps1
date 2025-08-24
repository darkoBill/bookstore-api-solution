#!/usr/bin/env pwsh
# PowerShell wrapper for smoke tests
# Requires bash via WSL or Git Bash

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$smokeScript = Join-Path $scriptDir "smoke.sh"

if (Get-Command bash -ErrorAction SilentlyContinue) {
    bash $smokeScript @args
} else {
    Write-Error "bash not found. Install WSL or Git Bash to run smoke tests."
    exit 1
}