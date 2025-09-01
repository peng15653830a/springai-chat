import os
import certifi
from openai import OpenAI
import sys

# Set the certificate bundle path
os.environ['SSL_CERT_FILE'] = certifi.where()
os.environ['REQUESTS_CA_BUNDLE'] = certifi.where()

client = OpenAI(
    base_url='https://api-inference.modelscope.cn/v1',
    api_key='ms-d2ce925e-2397-4c4c-9de2-62e820357af6', # ModelScope Token
)

print("🔍 Testing ModelScope API...")
print(f"📡 Base URL: https://api-inference.modelscope.cn/v1")
print(f"🤖 Model: Qwen/Qwen3-235B-A22B-Thinking-2507")
print("=" * 50)

try:
    response = client.chat.completions.create(
        model='Qwen/Qwen3-235B-A22B-Thinking-2507', # ModelScope Model-Id
        messages=[
            {
                'role': 'user',
                'content': '你好，请简单介绍一下你自己'
            }
        ],
        stream=True,
        max_tokens=500,
        temperature=0.7
    )
    
    done_reasoning = False
    reasoning_content = ""
    answer_content = ""
    
    print("🎯 Streaming response:")
    for chunk in response:
        if hasattr(chunk.choices[0].delta, 'reasoning_content') and chunk.choices[0].delta.reasoning_content:
            reasoning_chunk = chunk.choices[0].delta.reasoning_content
            reasoning_content += reasoning_chunk
            if not done_reasoning:
                print("🧠 [Thinking]", end='', flush=True)
            print(reasoning_chunk, end='', flush=True)
        elif chunk.choices[0].delta.content:
            answer_chunk = chunk.choices[0].delta.content
            answer_content += answer_chunk
            if not done_reasoning:
                print('\n\n✅ [Final Answer]')
                done_reasoning = True
            print(answer_chunk, end='', flush=True)
    
    print(f"\n\n📊 Summary:")
    print(f"   Reasoning length: {len(reasoning_content)} chars")
    print(f"   Answer length: {len(answer_content)} chars")
    print("✅ API test completed successfully!")
    
except Exception as e:
    print(f"❌ API test failed: {e}")
    print(f"   Error type: {type(e).__name__}")
    sys.exit(1)