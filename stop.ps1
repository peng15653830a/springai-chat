# PowerShell脚本用于停止AI聊天应用

# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "🛑 停止AI聊天应用..." -ForegroundColor Red
Write-Host ""

# 从文件读取Job ID并停止
if (Test-Path ".backend.job") {
    $backendJobId = Get-Content ".backend.job" -Raw
    $backendJobId = $backendJobId.Trim()
    if ($backendJobId) {
        $job = Get-Job -Id $backendJobId -ErrorAction SilentlyContinue
        if ($job) {
            Stop-Job $job -ErrorAction SilentlyContinue
            Remove-Job $job -ErrorAction SilentlyContinue
            Write-Host "✅ 已停止后端Job (ID: $backendJobId)" -ForegroundColor Green
        }
    }
    Remove-Item ".backend.job" -ErrorAction SilentlyContinue
}

if (Test-Path ".frontend.job") {
    $frontendJobId = Get-Content ".frontend.job" -Raw
    $frontendJobId = $frontendJobId.Trim()
    if ($frontendJobId) {
        $job = Get-Job -Id $frontendJobId -ErrorAction SilentlyContinue
        if ($job) {
            Stop-Job $job -ErrorAction SilentlyContinue
            Remove-Job $job -ErrorAction SilentlyContinue
            Write-Host "✅ 已停止前端Job (ID: $frontendJobId)" -ForegroundColor Green
        }
    }
    Remove-Item ".frontend.job" -ErrorAction SilentlyContinue
}

# 停止占用8080端口的进程（后端）
Write-Host "停止后端服务..." -ForegroundColor Yellow
try {
    $backendProcesses = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    foreach ($pid in $backendProcesses) {
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process) {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "✅ 已停止后端服务 (PID: $pid)" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "无法获取8080端口进程信息" -ForegroundColor Yellow
}

# 停止占用3000端口的进程（前端）
Write-Host "停止前端服务..." -ForegroundColor Yellow
try {
    $frontendProcesses = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    foreach ($pid in $frontendProcesses) {
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process) {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "✅ 已停止前端服务 (PID: $pid)" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "无法获取3000端口进程信息" -ForegroundColor Yellow
}

# 停止所有Spring Boot相关的Java进程
Write-Host "停止Spring Boot进程..." -ForegroundColor Yellow
try {
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    foreach ($process in $javaProcesses) {
        $commandLine = (Get-WmiObject Win32_Process -Filter "ProcessId = $($process.Id)" -ErrorAction SilentlyContinue).CommandLine
        if ($commandLine -and $commandLine.Contains("spring-boot")) {
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
            Write-Host "✅ 已停止Spring Boot进程 (PID: $($process.Id))" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "无法获取Java进程信息" -ForegroundColor Yellow
}

# 停止Node.js进程
Write-Host "停止Node.js进程..." -ForegroundColor Yellow
try {
    $nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
    foreach ($process in $nodeProcesses) {
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        Write-Host "✅ 已停止Node.js进程 (PID: $($process.Id))" -ForegroundColor Green
    }
} catch {
    Write-Host "无法获取Node.js进程信息" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 所有服务已停止" -ForegroundColor Green
Read-Host "按Enter键退出"