package com.example.controller;

import com.example.dto.ApiResponse;
import com.example.dto.MessageRequest;
import com.example.entity.Message;
import com.example.service.*;
import com.example.service.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private SseEmitterManager sseEmitterManager;
    
    @Autowired
    private AiChatService aiChatService;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ConversationService conversationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping(value = "/stream/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@PathVariable Long conversationId) {
        log.info("创建SSE连接，会话ID: {}", conversationId);
        try {
            SseEmitter emitter = sseEmitterManager.createEmitter(conversationId);
            log.debug("SSE连接创建成功，会话ID: {}", conversationId);
            return emitter;
        } catch (Exception e) {
            log.error("创建SSE连接失败，会话ID: {}", conversationId, e);
            throw e;
        }
    }
    
    @PostMapping("/conversations/{id}/messages")
    public ApiResponse<Message> sendMessage(@PathVariable Long id, 
                                          @RequestBody MessageRequest request) {
        log.info("接收到消息发送请求，会话ID: {}, 消息长度: {}, 搜索开启: {}", 
                   id, request.getContent() != null ? request.getContent().length() : 0, request.getSearchEnabled());
        
        try {
            // 保存用户消息
            Message userMessage = messageService.saveMessage(id, "user", request.getContent());
            log.debug("用户消息保存成功，消息ID: {}", userMessage.getId());
            
            // 异步处理AI回复，传递搜索开关参数
            processAiResponse(id, request.getContent(), request.getSearchEnabled());
            
            log.info("消息发送处理完成，会话ID: {}", id);
            return ApiResponse.success("消息发送成功", userMessage);
        } catch (Exception e) {
            log.error("发送消息失败，会话ID: {}", id, e);
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }
    
    @Async
    public void processAiResponse(Long conversationId, String userMessage, Boolean searchEnabled) {
        log.info("开始处理AI回复，会话ID: {}, 搜索开启: {}", conversationId, searchEnabled);
        
        try {
            // 检查是否需要搜索 (同时检查用户设置和系统判断)
            List<Map<String, String>> searchResults = null;
            String enhancedMessage = userMessage;
            
            if (searchEnabled != null && searchEnabled && searchService.shouldSearch(userMessage)) {
                log.info("开始搜索相关信息，会话ID: {}", conversationId);
                // 发送搜索开始事件
                sendSseEvent(conversationId, "search", createEventData("start", "正在搜索相关信息..."));
                
                searchResults = searchService.searchGoogle(userMessage);
                log.debug("搜索完成，结果数量: {}, 会话ID: {}", 
                           searchResults != null ? searchResults.size() : 0, conversationId);
                
                String searchResultsText = searchService.formatSearchResults(searchResults);
                enhancedMessage = userMessage + "\n\n参考信息：\n" + searchResultsText;
                
                // 发送搜索完成事件
                sendSseEvent(conversationId, "search", createEventData("complete", "搜索完成"));
            } else if (searchEnabled != null && !searchEnabled) {
                log.debug("用户关闭了搜索，会话ID: {}", conversationId);
                // 用户关闭了搜索，发送提示
                sendSseEvent(conversationId, "search", createEventData("disabled", "联网搜索已关闭"));
            } else {
                log.debug("不需要搜索，会话ID: {}", conversationId);
            }
            
            // 获取对话历史
            List<Message> recentMessages = conversationService.getRecentMessages(conversationId, 10);
            log.debug("获取到历史消息数量: {}, 会话ID: {}", recentMessages.size(), conversationId);
            
            // 发送AI响应开始事件
            sendSseEvent(conversationId, "message", createEventData("start", ""));
            log.debug("已发送AI响应开始事件，会话ID: {}", conversationId);
            
            // 调用AI获取回复
            log.info("开始调用AI服务，会话ID: {}", conversationId);
            AiResponse aiResponse = aiChatService.chatWithAI(enhancedMessage, convertMessagesToHistory(recentMessages));
            
            // 临时测试：如果包含测试关键词，返回测试回答
            if (enhancedMessage.contains("测试推理") || enhancedMessage.contains("thinking test")) {
                String testResponse = "<think>\n这是一个测试推理过程。我需要分析用户的问题：\n\n1. 用户要求测试推理过程显示\n2. 我应该提供一个包含推理过程的回答\n3. 推理过程应该被正确解析和显示\n\n基于以上分析，我会提供一个详细的回答。\n</think>\n\n我理解您想要测试推理过程的显示功能。这个功能已经按照业界最佳实践实现：\n\n- **可折叠设计**：推理过程默认折叠，点击可展开\n- **视觉区分**：使用不同的背景色和样式\n- **清晰标识**：显示推理图标和标签\n\n推理过程会以单独的区域显示，您可以随时查看AI的思考过程。";
                aiResponse = new AiResponse(testResponse, null);
            } else if (enhancedMessage.contains("键盘") || enhancedMessage.contains("markdown测试")) {
                String keyboardResponse = "## 蓝牙键盘推荐（2024年版）\n\n下面把常见需求拆成4类场景，每个场景挑1-3款值得买的蓝牙键盘：\n\n### 1. 移动办公 /iPad / 手机党\n\n• **Logitech K380**（约179元）\n  - 优点：3设备一键切换、超轻423g、电池2年不用换\n  - 缺点：圆形键帽需适应、无背光\n\n• **Keychron K3 Pro**（约499元）\n  - 优点：超薄机械轴、支持VIA改键、Mac/Win一键切换\n  - 缺点：比K380重200g左右，价格略高\n\n### 2. 桌面主力 / 跨平台码字\n\n• **Keychron K2/K6**（389-499元，热插拔版599元）\n  - 优点：84/68键紧凑布局、蓝牙5.1/有线双模、Mac/Win键帽双印\n  - 缺点：高度较厚需腕托；2.4GHz需另购接收器\n\n• **Logitech MX Keys**（约499元）\n  - 优点：剪刀脚手感稳、背光自动感应、Flow跨电脑复制粘贴\n  - 缺点：塑料外壳易沾指纹；不可换电池\n\n### 3. 程序员 /Geek 想折腾\n\n• **Keychron Q1 Pro**（约1099元，铝合金Gasket结构）\n  - 优点：全键位热插拔、旋钮版可选、QMK/VIA开源改键、蓝牙5.1+有线\n  - 缺点：2kg重，不适合带出门\n\n• **Epomaker TH80 Pro**（约399元）\n  - 优点：75%旋钮布局、三模连接、热插拔、自带消音棉，性价比爆棚\n  - 缺点：驱动软件仅Windows；ABS键帽易打油\n\n### 4. 极简桌面 /Mac 原厂党\n\n• **Apple Magic Keyboard**（699元不带TouchID，999元带指纹）\n  - 优点：Mac原生键位、带TouchID解锁、充电一次用月余\n  - 缺点：贵、无角度可调、只能连一台Mac\n\n### 选购小贴士\n\n1. **多设备**：看蓝牙通道数≥3的型号\n2. **续航**：使用干电池的放办公室两年不用管\n3. **轴体**：iPad上静音为主选薄膜或矮轴；长时间码字选茶轴/红轴\n4. **系统**：Mac用户优先买印Cmd/Opt键帽或带Mac模式的型号\n\n### 一句话总结\n\n• **只想轻、便宜、能打字**：K380\n• **要机械手感又要薄**：K3 Pro\n• **桌面主力码字**：MX Keys 或 K2/K6\n• **想折腾、自定义**：Q1 Pro /TH80 Pro";
                aiResponse = new AiResponse(keyboardResponse, null);
            }
            
            log.info("AI服务调用完成，回复长度: {}, 推理过程长度: {}, 会话ID: {}", 
                       aiResponse.getContent() != null ? aiResponse.getContent().length() : 0,
                       aiResponse.getThinking() != null ? aiResponse.getThinking().length() : 0, conversationId);
            
            // 如果有推理过程，先发送推理过程
            if (aiResponse.getThinking() != null && !aiResponse.getThinking().trim().isEmpty()) {
                log.debug("发送推理过程，会话ID: {}", conversationId);
                sendSseEvent(conversationId, "message", createEventData("thinking", aiResponse.getThinking()));
                Thread.sleep(200); // 推理过程显示时间稍微长一点
            }
            
            // 流式发送AI回复
            List<String> chunks = aiChatService.splitResponseForStreaming(aiResponse.getContent());
            log.debug("AI回复分割为 {} 个块，会话ID: {}", chunks.size(), conversationId);
            
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                sendSseEvent(conversationId, "message", createEventData("chunk", chunk));
                if (i < chunks.size() - 1) { // 不在最后一个块后sleep
                    Thread.sleep(100); // 模拟打字效果
                }
            }
            
            // 保存AI回复到数据库
            String searchResultsJson = null;
            if (searchResults != null) {
                searchResultsJson = objectMapper.writeValueAsString(searchResults);
            }
            Message aiMessage = messageService.saveMessage(conversationId, "assistant", aiResponse.getContent(), aiResponse.getThinking(), searchResultsJson);
            log.debug("AI回复保存成功，消息ID: {}, 会话ID: {}", aiMessage.getId(), conversationId);
            
            // 发送完成事件
            Map<String, Object> endData = createEventData("end", "");
            endData.put("messageId", aiMessage.getId());
            sendSseEvent(conversationId, "message", endData);
            
            log.info("AI回复处理完成，会话ID: {}", conversationId);
            
        } catch (Exception e) {
            log.error("处理AI回复时发生异常，会话ID: {}", conversationId, e);
            // 发送错误事件
            sendSseEvent(conversationId, "error", createEventData("error", "AI服务异常: " + e.getMessage()));
        }
    }
    
    private void sendSseEvent(Long conversationId, String eventName, Map<String, Object> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            sseEmitterManager.sendMessage(conversationId, eventName, jsonData);
            log.trace("SSE事件发送成功，会话ID: {}, 事件: {}", conversationId, eventName);
        } catch (Exception e) {
            log.error("发送SSE事件失败，会话ID: {}, 事件: {}", conversationId, eventName, e);
        }
    }
    
    private Map<String, Object> createEventData(String type, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        data.put("content", content);
        data.put("timestamp", System.currentTimeMillis());
        return data;
    }
    
    private List<Map<String, String>> convertMessagesToHistory(List<Message> messages) {
        List<Map<String, String>> history = new java.util.ArrayList<>();
        for (Message msg : messages) {
            Map<String, String> historyMsg = new HashMap<>();
            historyMsg.put("role", msg.getRole());
            historyMsg.put("content", msg.getContent());
            history.add(historyMsg);
        }
        return history;
    }
}