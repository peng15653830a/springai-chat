#!/bin/bash

# 阿里巴巴P3C代码规范检查脚本
# 作者: Claude Code
# 功能: 执行P3C代码规范检查、编译验证等

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Maven是否可用
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装或不在PATH中"
        exit 1
    fi
}

# 执行P3C代码规范检查
run_p3c_check() {
    log_info "🔍 开始执行阿里巴巴P3C代码规范检查..."
    
    # 执行P3C检查（通过PMD插件）
    if mvn pmd:check -q; then
        log_success "✅ P3C代码规范检查通过"
        return 0
    else
        log_error "❌ P3C代码规范检查发现问题"
        log_info "请修复上述代码规范问题后重试"
        log_info "详细报告可查看: target/pmd.xml"
        return 1
    fi
}

# 执行编译检查
run_compile_check() {
    log_info "🔨 开始执行编译检查..."
    
    if mvn compile -q; then
        log_success "✅ 代码编译通过"
        return 0
    else
        log_error "❌ 代码编译失败"
        return 1
    fi
}

# 执行代码格式检查
run_format_check() {
    log_info "💅 开始检查代码格式..."
    
    if mvn spotless:check -q 2>/dev/null; then
        log_success "✅ 代码格式检查通过"
        return 0
    else
        log_warning "⚠️ 代码格式需要调整"
        log_info "可以运行 'mvn spotless:apply' 自动格式化代码"
        return 1
    fi
}

# 主函数
main() {
    local exit_code=0
    
    echo "========================================"
    log_info "🚀 开始代码质量检查"
    echo "========================================"
    
    # 检查Maven环境
    check_maven
    
    # 执行各项检查
    if ! run_compile_check; then
        exit_code=1
    fi
    
    if ! run_p3c_check; then
        exit_code=1
    fi
    
    # 格式检查失败不影响整体结果，只是警告
    if ! run_format_check; then
        log_warning "代码格式检查未通过，但不影响整体结果"
    fi
    
    echo "========================================"
    if [ $exit_code -eq 0 ]; then
        log_success "🎉 所有代码质量检查通过！"
    else
        log_error "💥 代码质量检查失败，请修复上述问题"
    fi
    echo "========================================"
    
    exit $exit_code
}

# 支持命令行参数
case "${1:-}" in
    "p3c")
        log_info "仅执行P3C检查..."
        check_maven
        run_p3c_check
        ;;
    "compile")
        log_info "仅执行编译检查..."
        check_maven
        run_compile_check
        ;;
    "format")
        log_info "仅执行格式检查..."
        check_maven
        run_format_check
        ;;
    *)
        main
        ;;
esac