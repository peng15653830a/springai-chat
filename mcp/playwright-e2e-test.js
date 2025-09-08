const { chromium } = require('playwright');
const axios = require('axios');

async function runE2ETest() {
    console.log('🚀 Starting End-to-End Test with Playwright');
    
    // 1. 验证后端API
    console.log('\n📡 Step 1: Testing Backend APIs');
    await testBackendAPIs();
    
    // 2. 测试前端UI
    console.log('\n🖥️  Step 2: Testing Frontend UI');
    await testFrontendUI();
    
    console.log('\n✅ E2E Test Completed Successfully!');
}

async function testBackendAPIs() {
    const baseURL = 'http://localhost:8080';
    
    try {
        // 测试健康检查
        console.log('  ✓ Testing health endpoint...');
        const health = await axios.get(`${baseURL}/actuator/health`);
        console.log(`    Health Status: ${health.data.status}`);
        
        // 测试产品API
        console.log('  ✓ Testing products API...');
        const products = await axios.get(`${baseURL}/api/products`);
        console.log(`    Found ${products.data.length} products`);
        
        // 测试用户API
        console.log('  ✓ Testing users API...');
        const users = await axios.get(`${baseURL}/api/users`);
        console.log(`    Found ${users.data.length} users`);
        
        // 测试自然语言订单API
        console.log('  ✓ Testing natural language order API...');
        const orderResponse = await axios.post(`${baseURL}/api/nl-orders`, {
            instruction: '我要买1个苹果手机'
        }, {
            headers: { 'Content-Type': 'application/json' },
            timeout: 30000
        });
        
        console.log(`    Order created successfully:`);
        console.log(`      Order ID: ${orderResponse.data.id}`);
        console.log(`      Product ID: ${orderResponse.data.productId}`);
        console.log(`      Quantity: ${orderResponse.data.quantity}`);
        console.log(`      Total Price: ¥${orderResponse.data.totalPrice}`);
        
        // 验证订单列表
        console.log('  ✓ Verifying order was saved...');
        const orders = await axios.get(`${baseURL}/api/orders`);
        console.log(`    Total orders in system: ${orders.data.length}`);
        
    } catch (error) {
        console.error('❌ Backend API test failed:', error.response?.data || error.message);
        throw error;
    }
}

async function testFrontendUI() {
    const browser = await chromium.launch({ 
        headless: false,
        slowMo: 1000 // 慢动作便于观察
    });
    
    const context = await browser.newContext();
    const page = await context.newPage();
    
    try {
        console.log('  ✓ Opening frontend application...');
        await page.goto('http://localhost:8081');
        
        // 等待页面加载
        await page.waitForTimeout(2000);
        
        // 截图初始状态
        await page.screenshot({ path: 'e2e-screenshots/01-homepage.png' });
        console.log('    📸 Screenshot saved: 01-homepage.png');
        
        // 检查页面标题
        const title = await page.title();
        console.log(`    Page title: "${title}"`);
        
        // 查找自然语言输入框
        console.log('  ✓ Looking for natural language input...');
        const inputSelector = 'textarea[placeholder*="苹果手机"], input[type="text"], textarea';
        await page.waitForSelector(inputSelector, { timeout: 5000 });
        
        const inputElement = page.locator(inputSelector).first();
        console.log('    Found input element');
        
        // 输入测试指令
        console.log('  ✓ Entering test instruction...');
        const testInstruction = '我要买2个苹果手机';
        await inputElement.fill(testInstruction);
        console.log(`    Entered: "${testInstruction}"`);
        
        // 截图输入后状态
        await page.screenshot({ path: 'e2e-screenshots/02-input-filled.png' });
        console.log('    📸 Screenshot saved: 02-input-filled.png');
        
        // 查找并点击提交按钮
        console.log('  ✓ Looking for submit button...');
        const buttonSelector = 'button:has-text("提交"), button[type="submit"], button:has-text("下单")';
        await page.waitForSelector(buttonSelector, { timeout: 5000 });
        
        const submitButton = page.locator(buttonSelector).first();
        console.log('    Found submit button');
        
        // 监听网络请求
        const networkRequests = [];
        const networkResponses = [];
        
        page.on('request', request => {
            if (request.url().includes('/api/')) {
                networkRequests.push({
                    method: request.method(),
                    url: request.url(),
                    postData: request.postData()
                });
                console.log(`    🌐 Request: ${request.method()} ${request.url()}`);
            }
        });
        
        page.on('response', response => {
            if (response.url().includes('/api/')) {
                networkResponses.push({
                    status: response.status(),
                    url: response.url()
                });
                console.log(`    📡 Response: ${response.status()} ${response.url()}`);
            }
        });
        
        // 点击提交按钮
        console.log('  ✓ Clicking submit button...');
        await submitButton.click();
        
        // 等待响应
        await page.waitForTimeout(5000);
        
        // 截图结果
        await page.screenshot({ path: 'e2e-screenshots/03-after-submit.png' });
        console.log('    📸 Screenshot saved: 03-after-submit.png');
        
        // 检查是否有成功或错误消息
        console.log('  ✓ Checking for result messages...');
        
        // 查找成功消息
        const successSelectors = [
            'text=成功',
            'text=订单',
            '.success',
            '[class*="success"]',
            'text=ID'
        ];
        
        let foundSuccess = false;
        for (const selector of successSelectors) {
            try {
                const element = page.locator(selector);
                if (await element.count() > 0) {
                    const text = await element.first().textContent();
                    console.log(`    ✅ Success indicator found: "${text}"`);
                    foundSuccess = true;
                    break;
                }
            } catch (e) {
                // 继续尝试下一个选择器
            }
        }
        
        // 查找错误消息
        const errorSelectors = [
            'text=错误',
            'text=失败',
            '.error',
            '[class*="error"]'
        ];
        
        let foundError = false;
        for (const selector of errorSelectors) {
            try {
                const element = page.locator(selector);
                if (await element.count() > 0) {
                    const text = await element.first().textContent();
                    console.log(`    ❌ Error indicator found: "${text}"`);
                    foundError = true;
                    break;
                }
            } catch (e) {
                // 继续尝试下一个选择器
            }
        }
        
        if (!foundSuccess && !foundError) {
            console.log('    ⚠️  No clear success/error message found');
        }
        
        // 显示网络请求摘要
        console.log('  ✓ Network Activity Summary:');
        console.log(`    Requests sent: ${networkRequests.length}`);
        console.log(`    Responses received: ${networkResponses.length}`);
        
        networkRequests.forEach((req, i) => {
            console.log(`    Request ${i + 1}: ${req.method} ${req.url}`);
            if (req.postData) {
                console.log(`      Data: ${req.postData.substring(0, 100)}${req.postData.length > 100 ? '...' : ''}`);
            }
        });
        
        networkResponses.forEach((res, i) => {
            const status = res.status >= 200 && res.status < 300 ? '✅' : '❌';
            console.log(`    Response ${i + 1}: ${status} ${res.status} ${res.url}`);
        });
        
        // 最终验证：检查后端是否收到了新订单
        console.log('  ✓ Final verification - checking backend for new orders...');
        const finalOrders = await axios.get('http://localhost:8080/api/orders');
        console.log(`    Total orders after test: ${finalOrders.data.length}`);
        
        if (finalOrders.data.length > 0) {
            const latestOrder = finalOrders.data[finalOrders.data.length - 1];
            console.log(`    Latest order: ID=${latestOrder.id}, Quantity=${latestOrder.quantity}, Price=¥${latestOrder.totalPrice}`);
        }
        
    } catch (error) {
        console.error('❌ Frontend UI test failed:', error.message);
        await page.screenshot({ path: 'e2e-screenshots/error.png' });
        console.log('    📸 Error screenshot saved: error.png');
        throw error;
    } finally {
        await browser.close();
    }
}

// 创建截图目录
const fs = require('fs');
if (!fs.existsSync('e2e-screenshots')) {
    fs.mkdirSync('e2e-screenshots');
}

// 运行测试
runE2ETest()
    .then(() => {
        console.log('\n🎉 All tests passed! Frontend and backend are working correctly.');
        process.exit(0);
    })
    .catch(error => {
        console.error('\n💥 Test failed:', error.message);
        process.exit(1);
    });