# Train the full curriculum SEQUENTIALLY (each task gets the whole GPU/CPU -> best
# convergence + reliable on Windows subproc), then eval + chart everything.
# Resilient: if a run dies (e.g. a transient worker pipe break), it retries once.
# Run:  powershell -NoProfile -ExecutionPolicy Bypass -File train_all.ps1
$ErrorActionPreference = "Continue"
$env:PYTHONUTF8 = "1"; $env:PYTHONIOENCODING = "utf-8"
Set-Location $PSScriptRoot
$py = ".\.venv\Scripts\python.exe"

$tasks = @("express_priority", "junction_management", "rush_hour")
foreach ($t in $tasks) {
    Write-Output ("=" * 64)
    Write-Output "# TRAIN $t  @ $(Get-Date -Format HH:mm:ss)"
    Write-Output ("=" * 64)
    & $py -m training.train_ppo --task $t
    if ($LASTEXITCODE -ne 0) {
        Write-Output "# $t FAILED (exit $LASTEXITCODE) -> retry once @ $(Get-Date -Format HH:mm:ss)"
        & $py -m training.train_ppo --task $t
        Write-Output "# $t retry exit=$LASTEXITCODE"
    }
}

Write-Output ("=" * 64)
Write-Output "# EVAL ALL (RL vs baselines)"
Write-Output ("=" * 64)
& $py -m eval.run_eval --task all --seeds 50

foreach ($t in @("basic_control", "express_priority", "junction_management", "rush_hour")) {
    & $py -m eval.plot --task $t
}
Write-Output "# CURRICULUM COMPLETE @ $(Get-Date -Format HH:mm:ss)"
