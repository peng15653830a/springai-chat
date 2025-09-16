package com.example.service.impl;

import com.example.dto.response.SearchResult;
import com.example.entity.MessageToolResult;
import com.example.mapper.MessageToolResultMapper;
import com.example.service.MessageToolResultService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息工具调用结果服务实现类
 *
 * @author xupeng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageToolResultServiceImpl implements MessageToolResultService {

    private final MessageToolResultMapper messageToolResultMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startToolCall(Long messageId, String toolName, String toolInput) {
        log.debug("开始工具调用记录，消息ID: {}, 工具: {}", messageId, toolName);

        // 获取下一个调用序号
        Integer nextSequence = messageToolResultMapper.getNextCallSequence(messageId);

        MessageToolResult toolResult = MessageToolResult.builder()
                .messageId(messageId)
                .toolName(toolName)
                .callSequence(nextSequence)
                .toolInput(toolInput)
                .status("IN_PROGRESS")
                .build();

        messageToolResultMapper.insertToolResult(toolResult);

        log.debug("工具调用记录已创建，ID: {}, 序号: {}", toolResult.getId(), nextSequence);
        return toolResult.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeToolCall(Long toolResultId, String toolResult) {
        log.debug("完成工具调用记录，ID: {}", toolResultId);

        MessageToolResult entity = MessageToolResult.builder()
                .id(toolResultId)
                .toolResult(toolResult)
                .status("SUCCESS")
                .build();

        messageToolResultMapper.updateToolResult(entity);

        log.debug("工具调用记录已完成，ID: {}", toolResultId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void failToolCall(Long toolResultId, String errorMessage) {
        log.debug("记录工具调用失败，ID: {}, 错误: {}", toolResultId, errorMessage);

        MessageToolResult entity = MessageToolResult.builder()
                .id(toolResultId)
                .status("FAILED")
                .errorMessage(errorMessage)
                .build();

        messageToolResultMapper.updateToolResult(entity);

        log.debug("工具调用失败已记录，ID: {}", toolResultId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveSearchResults(Long messageId, String query, List<SearchResult> searchResults) {
        log.debug("保存搜索结果，消息ID: {}, 查询: {}, 结果数量: {}",
                messageId, query, searchResults != null ? searchResults.size() : 0);

        try {
            // 将搜索结果序列化为JSON
            String searchResultsJson = objectMapper.writeValueAsString(searchResults);
            String queryJson = objectMapper.writeValueAsString(query);

            // 开始工具调用记录
            Long toolResultId = startToolCall(messageId, "webSearch", queryJson);

            // 完成工具调用记录
            completeToolCall(toolResultId, searchResultsJson);

            log.debug("搜索结果已保存，工具调用记录ID: {}", toolResultId);
            return toolResultId;

        } catch (JsonProcessingException e) {
            log.error("序列化搜索结果失败", e);
            throw new RuntimeException("序列化搜索结果失败", e);
        }
    }

    @Override
    public List<MessageToolResult> getMessageToolResults(Long messageId) {
        log.debug("获取消息工具调用结果，消息ID: {}", messageId);

        List<MessageToolResult> results = messageToolResultMapper.findByMessageId(messageId);

        log.debug("找到 {} 条工具调用记录", results.size());
        return results;
    }

    @Override
    public List<SearchResult> getMessageSearchResults(Long messageId) {
        log.debug("获取消息搜索结果，消息ID: {}", messageId);

        List<MessageToolResult> toolResults = messageToolResultMapper.findByMessageIdAndToolName(messageId, "webSearch");
        List<SearchResult> allSearchResults = new ArrayList<>();

        for (MessageToolResult toolResult : toolResults) {
            if ("SUCCESS".equals(toolResult.getStatus()) && toolResult.getToolResult() != null) {
                try {
                    List<SearchResult> searchResults = objectMapper.readValue(
                            toolResult.getToolResult(),
                            new TypeReference<List<SearchResult>>() {}
                    );
                    allSearchResults.addAll(searchResults);
                } catch (JsonProcessingException e) {
                    log.error("反序列化搜索结果失败，工具调用记录ID: {}", toolResult.getId(), e);
                }
            }
        }

        log.debug("消息 {} 共有 {} 条搜索结果", messageId, allSearchResults.size());
        return allSearchResults;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessageToolResults(Long messageId) {
        log.debug("删除消息工具调用结果，消息ID: {}", messageId);

        messageToolResultMapper.deleteByMessageId(messageId);

        log.debug("消息 {} 的工具调用结果已删除", messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessageToolResultsByMessageIds(java.util.List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        log.debug("批量删除消息工具调用结果，消息ID数量: {}", messageIds.size());
        try {
            messageToolResultMapper.deleteByMessageIds(messageIds);
            log.debug("已批量删除 {} 条消息的工具调用结果", messageIds.size());
        } catch (Exception e) {
            log.warn("批量删除消息工具调用结果失败，将改为逐条删除。错误: {}", e.getMessage());
            for (Long id : messageIds) {
                try {
                    messageToolResultMapper.deleteByMessageId(id);
                } catch (Exception ignore) {
                }
            }
        }
    }
}
