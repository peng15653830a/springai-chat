# PostgreSQL root 用户创建脚本
# 作者: Assistant
# 日期: 2025-09-15

Write-Host "开始创建 PostgreSQL root 用户..." -ForegroundColor Green

# 设置 PostgreSQL 连接参数
$PGUSER = "postgres"
$PGDATABASE = "postgres"
$NEW_USER = "root"
$NEW_PASSWORD = "xupeng2016"
$TARGET_DATABASE = "ai_chat"

try {
    # 1. 创建 root 用户
    Write-Host "1. 创建 root 用户..." -ForegroundColor Yellow
    psql -U $PGUSER -d $PGDATABASE -c "CREATE USER $NEW_USER WITH PASSWORD '$NEW_PASSWORD';"
    
    # 2. 授予 CREATEDB 权限
    Write-Host "2. 授予 CREATEDB 权限..." -ForegroundColor Yellow
    psql -U $PGUSER -d $PGDATABASE -c "ALTER USER $NEW_USER CREATEDB;"
    
    # 3. 授予 SUPERUSER 权限
    Write-Host "3. 授予 SUPERUSER 权限..." -ForegroundColor Yellow
    psql -U $PGUSER -d $PGDATABASE -c "ALTER USER $NEW_USER WITH SUPERUSER;"
    
    # 4. 创建 ai_chat 数据库并将所有者设置为 root
    Write-Host "4. 创建 ai_chat 数据库..." -ForegroundColor Yellow
    psql -U $PGUSER -d $PGDATABASE -c "CREATE DATABASE $TARGET_DATABASE OWNER $NEW_USER;"
    
    Write-Host "PostgreSQL root 用户创建成功！" -ForegroundColor Green
    Write-Host "用户名: $NEW_USER" -ForegroundColor Cyan
    Write-Host "密码: $NEW_PASSWORD" -ForegroundColor Cyan
    Write-Host "数据库: $TARGET_DATABASE" -ForegroundColor Cyan
} 
catch {
    Write-Host "创建过程中出现错误: $_" -ForegroundColor Red
}

Write-Host "脚本执行完成。" -ForegroundColor Green