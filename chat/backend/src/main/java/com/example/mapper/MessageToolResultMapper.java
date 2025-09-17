package com.example.mapper;

import com.example.entity.MessageToolResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息工具调用结果Mapper接口
 *
 * @author xupeng
 */
@Mapper
public interface MessageToolResultMapper {

    /**
     * 保存工具调用结果
     * @param toolResult 工具调用结果实体
     */
    @Insert("""
        INSERT INTO message_tool_results
        (message_id, tool_name, call_sequence, tool_input, tool_result, status, error_message, created_at, updated_at)
        VALUES
        (#{messageId}, #{toolName}, #{callSequence}, #{toolInput}, #{toolResult}, #{status}, #{errorMessage}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertToolResult(MessageToolResult toolResult);

    /**
     * 更新工具调用结果
     * @param toolResult 工具调用结果实体
     */
    @Update("""
        UPDATE message_tool_results
        SET tool_result = #{toolResult}, status = #{status}, error_message = #{errorMessage}, updated_at = NOW()
        WHERE id = #{id}
        """)
    void updateToolResult(MessageToolResult toolResult);

    /**
     * 根据消息ID查询所有工具调用结果
     * @param messageId 消息ID
     * @return 工具调用结果列表
     */
    @Select("""
        SELECT id, message_id, tool_name, call_sequence, tool_input, tool_result, status, error_message, created_at, updated_at
        FROM message_tool_results
        WHERE message_id = #{messageId}
        ORDER BY call_sequence ASC
        """)
    List<MessageToolResult> findByMessageId(Long messageId);

    /**
     * 根据消息ID和工具名称查询工具调用结果
     * @param messageId 消息ID
     * @param toolName 工具名称
     * @return 工具调用结果列表
     */
    @Select("""
        SELECT id, message_id, tool_name, call_sequence, tool_input, tool_result, status, error_message, created_at, updated_at
        FROM message_tool_results
        WHERE message_id = #{messageId} AND tool_name = #{toolName}
        ORDER BY call_sequence ASC
        """)
    List<MessageToolResult> findByMessageIdAndToolName(@Param("messageId") Long messageId, @Param("toolName") String toolName);

    /**
     * 获取指定消息的下一个工具调用序号
     * @param messageId 消息ID
     * @return 下一个调用序号（从1开始）
     */
    @Select("""
        SELECT COALESCE(MAX(call_sequence), 0) + 1
        FROM message_tool_results
        WHERE message_id = #{messageId}
        """)
    Integer getNextCallSequence(Long messageId);

    /**
     * 删除指定消息的所有工具调用结果
     * @param messageId 消息ID
     */
    @Delete("DELETE FROM message_tool_results WHERE message_id = #{messageId}")
    void deleteByMessageId(Long messageId);

    /**
     * 批量删除工具调用结果
     * @param messageIds 消息ID集合
     */
    @Delete("""
        <script>
        DELETE FROM message_tool_results WHERE message_id IN
        <foreach collection="messageIds" item="messageId" open="(" close=")" separator=",">
            #{messageId}
        </foreach>
        </script>
        """)
    void deleteByMessageIds(@Param("messageIds") List<Long> messageIds);
}
