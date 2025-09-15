package com.example.memory;

import com.example.entity.Message;
import com.example.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.constant.AiChatConstants.ROLE_ASSISTANT;
import static com.example.constant.AiChatConstants.ROLE_SYSTEM;
import static com.example.constant.AiChatConstants.ROLE_USER;

/**
 * 持久化 ChatMemory 实现：通过 MessageMapper 读写数据库。
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseChatMemory implements ChatMemory {

    private final MessageMapper messageMapper;

    @Override
    public List<org.springframework.ai.chat.messages.Message> get(String conversationId) {
        Long cid = parseConversationId(conversationId);
        if (cid == null) {
            return List.of();
        }

        List<Message> history = messageMapper.selectByConversationId(cid);
        List<org.springframework.ai.chat.messages.Message> result = new ArrayList<>();
        for (Message m : history) {
            if (Objects.equals(m.getRole(), ROLE_USER)) {
                result.add(new UserMessage(m.getContent()));
            } else if (Objects.equals(m.getRole(), ROLE_ASSISTANT)) {
                result.add(new AssistantMessage(m.getContent()));
            } else if (Objects.equals(m.getRole(), ROLE_SYSTEM)) {
                result.add(new SystemMessage(m.getContent()));
            }
        }
        return result;
    }

    @Override
    public void add(String conversationId, List<org.springframework.ai.chat.messages.Message> messages) {
        log.info("🔥 DatabaseChatMemory.add() 被调用: conversationId={}, messages数量={}",
            conversationId, messages != null ? messages.size() : 0);

        Long cid = parseConversationId(conversationId);
        if (cid == null || messages == null || messages.isEmpty()) {
            log.warn("⚠️ 参数验证失败: cid={}, messages={}", cid, messages);
            return;
        }

        for (org.springframework.ai.chat.messages.Message msg : messages) {
            try {
                Message entity = new Message();
                entity.setConversationId(cid);

                if (msg.getMessageType() == MessageType.USER) {
                    // 跳过用户消息保存，由应用层手动保存以获取真实messageId
                    log.debug("跳过用户消息保存（应用层已处理）: {}", ((UserMessage) msg).getText());
                    continue;
                } else if (msg.getMessageType() == MessageType.ASSISTANT) {
                    entity.setRole(ROLE_ASSISTANT);
                    entity.setContent(((AssistantMessage) msg).getText());
                    // 搜索结果已通过WebSearchTool保存到message_tool_results表，无需重复保存
                    log.info("💾 保存助手回复消息: {}", entity.getContent().length() > 50 ?
                        entity.getContent().substring(0, 50) + "..." : entity.getContent());
                } else if (msg.getMessageType() == MessageType.SYSTEM) {
                    entity.setRole(ROLE_SYSTEM);
                    entity.setContent(((SystemMessage) msg).getText());
                } else {
                    continue;
                }

                messageMapper.insert(entity);
            } catch (Exception e) {
                log.error("保存聊天消息到数据库失败: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void clear(String conversationId) {
        Long cid = parseConversationId(conversationId);
        if (cid == null) return;
        try {
            messageMapper.deleteByConversationId(cid);
        } catch (Exception e) {
            log.warn("清理会话历史失败: {}", e.getMessage());
        }
    }

    private Long parseConversationId(String conversationId) {
        try {
            return Long.valueOf(conversationId);
        } catch (Exception e) {
            log.warn("非法的conversationId: {}", conversationId);
            return null;
        }
    }
}
