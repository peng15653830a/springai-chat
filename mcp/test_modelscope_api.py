#!/usr/bin/env python3
import requests
import json
import os

# 从环境变量获取API密钥
api_key = os.getenv('DeepSeek_API_KEY')
if not api_key:
    print("Error: DeepSeek_API_KEY environment variable not set")
    exit(1)

# API配置
base_url = "https://api-inference.modelscope.cn/v1"
model = "deepseek-ai/DeepSeek-V3.1"

# 构建请求
url = f"{base_url}/chat/completions"
headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/json"
}

data = {
    "model": model,
    "messages": [
        {
            "role": "user",
            "content": "你是一个订单处理助手。请将以下用户的自然语言指令转换为结构化的订单信息。\n用户指令: 我要买2个苹果\n\n请按照以下JSON格式回复:\n{\n  \"product\": \"产品名称\",\n  \"quantity\": 数量\n}\n\n只回复JSON，不要包含其他内容。"
        }
    ],
    "temperature": 0.7,
    "max_tokens": 1000
}

print("=== Request Details ===")
print(f"URL: {url}")
print(f"Headers: {headers}")
print(f"Data: {json.dumps(data, indent=2, ensure_ascii=False)}")
print()

try:
    response = requests.post(url, headers=headers, json=data, timeout=30)
    
    print("=== Response Details ===")
    print(f"Status Code: {response.status_code}")
    print(f"Headers: {dict(response.headers)}")
    print()
    
    if response.status_code == 200:
        response_json = response.json()
        print("=== Response JSON Structure ===")
        print(json.dumps(response_json, indent=2, ensure_ascii=False))
        
        # 分析响应结构
        print("\n=== Response Analysis ===")
        print(f"Root keys: {list(response_json.keys())}")
        
        if 'choices' in response_json:
            choices = response_json['choices']
            print(f"Choices count: {len(choices)}")
            if len(choices) > 0:
                first_choice = choices[0]
                print(f"First choice keys: {list(first_choice.keys())}")
                
                if 'message' in first_choice:
                    message = first_choice['message']
                    print(f"Message keys: {list(message.keys())}")
                    
                    if 'content' in message:
                        content = message['content']
                        print(f"Content: {content}")
                        print(f"Content type: {type(content)}")
    else:
        print(f"Error Response: {response.text}")
        
except Exception as e:
    print(f"Request failed: {e}")
    import traceback
    traceback.print_exc()