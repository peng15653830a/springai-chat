# PowerShell脚本用于启动AI聊天应用
param(
    [switch]$SkipChecks = $false
)

# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "🚀 启动AI聊天应用..." -ForegroundColor Green
Write-Host ""

# 检查环境
if (-not $SkipChecks) {
    Write-Host "📋 检查环境..." -ForegroundColor Yellow
    
    # 检查Java版本
    try {
        $javaVersion = (java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString().Split('"')[1] })
        Write-Host "Java版本: $javaVersion" -ForegroundColor Green
    } catch {
        Write-Host "❌ Java未安装或不在PATH中" -ForegroundColor Red
        Read-Host "按Enter键退出"
        exit 1
    }
    
    # 检查Node.js版本
    try {
        $nodeVersion = node --version
        Write-Host "Node.js版本: $nodeVersion" -ForegroundColor Green
    } catch {
        Write-Host "❌ Node.js未安装或不在PATH中" -ForegroundColor Red
        Read-Host "按Enter键退出"
        exit 1
    }
    
    # 检查Maven
    try {
        mvn --version | Out-Null
        Write-Host "Maven: 已安装" -ForegroundColor Green
    } catch {
        Write-Host "❌ Maven未安装或不在PATH中" -ForegroundColor Red
        Read-Host "按Enter键退出"
        exit 1
    }
    
    Write-Host ""
}

# 编译后端代码（如果需要）
if (-not (Test-Path "target\classes\com\example\springai\SpringaiApplication.class")) {
    Write-Host "📦 编译后端代码..." -ForegroundColor Yellow
    mvn clean compile
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 编译失败" -ForegroundColor Red
        Read-Host "按Enter键退出"
        exit 1
    }
}

# 启动后端服务
Write-Host "📦 启动后端服务..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run 2>&1 | Out-File "backend.log" -Encoding UTF8
}

# 等待后端启动
Write-Host "等待后端服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# 检查后端是否启动成功
Write-Host "检查后端服务状态..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10
    Write-Host "✅ 后端服务启动成功 (http://localhost:8080)" -ForegroundColor Green
} catch {
    Write-Host "❌ 后端服务启动失败，请检查日志" -ForegroundColor Red
    Write-Host "查看后端日志: Get-Content backend.log -Tail 50" -ForegroundColor Yellow
    Read-Host "按Enter键退出"
    exit 1
}

# 启动前端服务
Write-Host "🌐 启动前端服务..." -ForegroundColor Yellow
Set-Location "frontend"

if (-not (Test-Path "node_modules")) {
    Write-Host "安装前端依赖..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 安装前端依赖失败" -ForegroundColor Red
        Set-Location ".."
        Read-Host "按Enter键退出"
        exit 1
    }
}

# 启动前端开发服务器
Write-Host "启动前端开发服务器..." -ForegroundColor Yellow
$frontendJob = Start-Job -ScriptBlock {
    Set-Location (Join-Path $using:PWD "frontend")
    npm run dev 2>&1 | Out-File "../frontend.log" -Encoding UTF8
}

Set-Location ".."

Write-Host ""
Write-Host "🎉 应用启动完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📍 访问地址:" -ForegroundColor Cyan
Write-Host "   前端: http://localhost:3000" -ForegroundColor White
Write-Host "   后端: http://localhost:8080" -ForegroundColor White
Write-Host "   H2数据库: http://localhost:8080/h2-console" -ForegroundColor White
Write-Host ""
Write-Host "📋 进程信息:" -ForegroundColor Cyan
Write-Host "   后端Job ID: $($backendJob.Id)" -ForegroundColor White
Write-Host "   前端Job ID: $($frontendJob.Id)" -ForegroundColor White
Write-Host ""
Write-Host "🛑 停止服务:" -ForegroundColor Cyan
Write-Host "   Stop-Job $($backendJob.Id), $($frontendJob.Id); Remove-Job $($backendJob.Id), $($frontendJob.Id)" -ForegroundColor White
Write-Host "   或运行: .\stop.ps1" -ForegroundColor White
Write-Host ""
Write-Host "📝 查看日志:" -ForegroundColor Cyan
Write-Host "   后端: Get-Content backend.log -Tail 50 -Wait" -ForegroundColor White
Write-Host "   前端: Get-Content frontend.log -Tail 50 -Wait" -ForegroundColor White
Write-Host ""

# 保存Job ID到文件以便停止脚本使用
"$($backendJob.Id)" | Out-File ".backend.job" -Encoding ASCII
"$($frontendJob.Id)" | Out-File ".frontend.job" -Encoding ASCII

Write-Host "按Ctrl+C或关闭窗口停止服务..." -ForegroundColor Yellow

# 等待作业完成或用户中断
try {
    while ($backendJob.State -eq "Running" -or $frontendJob.State -eq "Running") {
        Start-Sleep -Seconds 2
    }
} finally {
    # 清理作业
    if ($backendJob) {
        Stop-Job $backendJob -ErrorAction SilentlyContinue
        Remove-Job $backendJob -ErrorAction SilentlyContinue
    }
    if ($frontendJob) {
        Stop-Job $frontendJob -ErrorAction SilentlyContinue
        Remove-Job $frontendJob -ErrorAction SilentlyContinue
    }
}