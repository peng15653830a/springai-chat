package com.example.novel.service.mcp;

import com.example.novel.dto.request.McpExecuteRequest;
import com.example.novel.dto.response.McpExecuteResponse;
import com.example.novel.dto.response.McpToolListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
public class McpServiceImpl implements McpService {

    private final List<McpToolListResponse.McpTool> mockTools;

    public McpServiceImpl() {
        this.mockTools = initializeMockTools();
    }

    @Autowired(required = false)
    private com.example.novel.mapper.NovelToolCallMapper novelToolCallMapper;

    private List<McpToolListResponse.McpTool> initializeMockTools() {
        List<McpToolListResponse.McpTool> tools = new ArrayList<>();

        McpToolListResponse.McpTool bashTool = new McpToolListResponse.McpTool();
        bashTool.setName("bash");
        bashTool.setDescription("Execute bash commands");
        Map<String, Object> bashSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "command", Map.of("type", "string", "description", "Command to execute")
                ),
                "required", List.of("command")
        );
        bashTool.setInputSchema(bashSchema);
        tools.add(bashTool);

        McpToolListResponse.McpTool fileTool = new McpToolListResponse.McpTool();
        fileTool.setName("filesystem");
        fileTool.setDescription("Read and write files");
        Map<String, Object> fileSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "action", Map.of("type", "string", "enum", List.of("read", "write", "list")),
                        "path", Map.of("type", "string", "description", "File path"),
                        "content", Map.of("type", "string", "description", "File content (for write)")
                ),
                "required", List.of("action", "path")
        );
        fileTool.setInputSchema(fileSchema);
        tools.add(fileTool);

        return tools;
    }

    @Override
    public Mono<McpToolListResponse> getAvailableTools() {
        return Mono.fromCallable(() -> {
            McpToolListResponse response = new McpToolListResponse();
            response.setSuccess(true);
            response.setMessage("MCP工具列表获取成功");
            response.setTools(mockTools);
            return response;
        });
    }

    @Override
    public Mono<McpExecuteResponse> executeTool(McpExecuteRequest request) {
        return Mono.fromCallable(() -> {
            McpExecuteResponse response = new McpExecuteResponse();

            try {
                Long callId = null;
                if (novelToolCallMapper != null) {
                    com.example.novel.entity.NovelToolCall call = new com.example.novel.entity.NovelToolCall();
                    call.setSessionId(request.getSessionId());
                    call.setMessageId(request.getMessageId());
                    call.setToolName(request.getToolName());
                    call.setInputJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.getParameters()));
                    call.setStatus("IN_PROGRESS");
                    try { novelToolCallMapper.insert(call); callId = call.getId(); } catch (Exception ignore) {}
                }

                String toolName = request.getToolName();
                Map<String, Object> parameters = request.getParameters();

                switch (toolName) {
                    case "bash":
                        response = executeBashTool(parameters);
                        break;
                    case "filesystem":
                        response = executeFilesystemTool(parameters);
                        break;
                    default:
                        response.setSuccess(false);
                        response.setError("Unknown tool: " + toolName);
                }

                if (novelToolCallMapper != null && callId != null) {
                    com.example.novel.entity.NovelToolCall done = new com.example.novel.entity.NovelToolCall();
                    done.setId(callId);
                    done.setStatus(Boolean.TRUE.equals(response.getSuccess()) ? "SUCCESS" : "FAILED");
                    done.setResultJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(response.getResult()));
                    done.setErrorMessage(response.getError());
                    try { novelToolCallMapper.update(done); } catch (Exception ignore) {}
                }

            } catch (Exception e) {
                log.error("MCP工具执行失败", e);
                response.setSuccess(false);
                response.setError("执行失败: " + e.getMessage());
                // 错误持久化
                try {
                    if (novelToolCallMapper != null) {
                        com.example.novel.entity.NovelToolCall err = new com.example.novel.entity.NovelToolCall();
                        err.setToolName(request.getToolName());
                        err.setSessionId(request.getSessionId());
                        err.setMessageId(request.getMessageId());
                        err.setStatus("FAILED");
                        err.setErrorMessage(e.getMessage());
                        novelToolCallMapper.insert(err);
                    }
                } catch (Exception ignore) {}
            }

            return response;
        });
    }

    private McpExecuteResponse executeBashTool(Map<String, Object> parameters) {
        McpExecuteResponse response = new McpExecuteResponse();
        String command = (String) parameters.get("command");

        if (command == null || command.trim().isEmpty()) {
            response.setSuccess(false);
            response.setError("Command is required");
            return response;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
            Process process = pb.start();
            int exitCode = process.waitFor();

            Map<String, Object> result = new HashMap<>();
            result.put("command", command);
            result.put("exitCode", exitCode);
            result.put("output", "模拟命令执行输出: " + command);

            response.setSuccess(true);
            response.setMessage("命令执行完成");
            response.setResult(result);

        } catch (Exception e) {
            log.error("执行bash命令失败: {}", command, e);
            response.setSuccess(false);
            response.setError("命令执行失败: " + e.getMessage());
        }

        return response;
    }

    private McpExecuteResponse executeFilesystemTool(Map<String, Object> parameters) {
        McpExecuteResponse response = new McpExecuteResponse();
        String action = (String) parameters.get("action");
        String path = (String) parameters.get("path");

        if (action == null || path == null) {
            response.setSuccess(false);
            response.setError("Action and path are required");
            return response;
        }

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("action", action);
            result.put("path", path);

            switch (action) {
                case "read":
                    result.put("content", "模拟文件内容: " + path);
                    break;
                case "write":
                    String content = (String) parameters.get("content");
                    result.put("content", content);
                    result.put("written", true);
                    break;
                case "list":
                    result.put("files", List.of("file1.txt", "file2.txt", "dir1/"));
                    break;
                default:
                    response.setSuccess(false);
                    response.setError("Unknown action: " + action);
                    return response;
            }

            response.setSuccess(true);
            response.setMessage("文件操作完成");
            response.setResult(result);

        } catch (Exception e) {
            log.error("执行文件操作失败: action={}, path={}", action, path, e);
            response.setSuccess(false);
            response.setError("文件操作失败: " + e.getMessage());
        }

        return response;
    }
}
