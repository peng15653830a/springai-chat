#!/usr/bin/env python3
"""
测试Qwen3推理功能的脚本
发送一个需要推理的问题，观察后端日志中是否有推理内容提取的信息
"""

import requests
import json
import time

def test_reasoning():
    # 登录获取token（如果需要）
    base_url = "http://localhost:8080/api"
    
    # 创建会话
    print("🔧 创建测试会话...")
    conversation_data = {
        "title": "推理测试会话",
        "userId": 1  # 假设用户ID为1
    }
    
    try:
        conv_response = requests.post(f"{base_url}/conversations", json=conversation_data)
        if conv_response.status_code == 200:
            conversation = conv_response.json()['data']
            conversation_id = conversation['id']
            print(f"✅ 会话创建成功，ID: {conversation_id}")
        else:
            print(f"❌ 会话创建失败: {conv_response.status_code}")
            return
    except Exception as e:
        print(f"❌ 创建会话时出错: {e}")
        return
    
    # 发送需要推理的问题
    print("\n🧠 发送推理问题...")
    test_question = "请解释一下为什么鸽子能够导航回家？需要分析它们的生理机制。"
    
    message_data = {
        "content": test_question,
        "searchEnabled": False  # 关闭搜索，专注于推理
    }
    
    try:
        # 发送消息
        msg_response = requests.post(f"{base_url}/chat/stream/{conversation_id}", 
                                   json=message_data, 
                                   stream=True,
                                   timeout=30)
        
        if msg_response.status_code == 200:
            print("✅ 消息发送成功，开始接收SSE流...")
            print("📨 SSE响应:")
            
            # 处理SSE流
            for line in msg_response.iter_lines():
                if line:
                    line_str = line.decode('utf-8')
                    if line_str.startswith('data: '):
                        data_str = line_str[6:]  # 移除'data: '前缀
                        try:
                            sse_event = json.loads(data_str)
                            event_type = sse_event.get('type', 'unknown')
                            
                            if event_type == 'thinking':
                                print(f"🧠 收到thinking事件: {sse_event.get('data', {}).get('content', '')[:100]}...")
                            elif event_type == 'chunk':
                                print(f"📝 收到chunk事件: {sse_event.get('data', {}).get('content', '')[:50]}...")
                            elif event_type == 'start':
                                print(f"🚀 收到start事件")
                            elif event_type == 'end':
                                print(f"🏁 收到end事件")
                                break
                            elif event_type == 'error':
                                print(f"❌ 收到error事件: {sse_event.get('data', '')}")
                                break
                        except json.JSONDecodeError:
                            print(f"⚠️ 无法解析SSE数据: {data_str}")
        else:
            print(f"❌ 发送消息失败: {msg_response.status_code}")
            print(f"响应内容: {msg_response.text}")
            
    except Exception as e:
        print(f"❌ 发送消息时出错: {e}")

if __name__ == "__main__":
    print("🧪 开始测试Qwen3推理功能...")
    print("📋 测试步骤:")
    print("1. 创建测试会话")
    print("2. 发送需要推理的问题")
    print("3. 观察SSE流中是否包含thinking事件")
    print("4. 检查后端日志中QwenReasoningAdvisor的工作情况")
    print("-" * 60)
    
    test_reasoning()
    
    print("\n" + "-" * 60)
    print("🔍 测试完成！请检查：")
    print("1. 是否收到了thinking类型的SSE事件")
    print("2. 后端日志中QwenReasoningAdvisor是否输出了推理内容提取信息")
    print("3. 如果没有thinking事件，请查看日志中的详细调试信息")