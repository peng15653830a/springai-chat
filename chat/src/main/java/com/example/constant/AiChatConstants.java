package com.example.constant;

/**
 * AI聊天服务相关常量
 *
 * @author xupeng
 */
public final class AiChatConstants {

  /** SSE事件类型 - 开始 */
  public static final String SSE_EVENT_START = "start";

  /** SSE事件类型 - 数据块 */
  public static final String SSE_EVENT_CHUNK = "chunk";

  /** SSE事件类型 - 结束 */
  public static final String SSE_EVENT_END = "end";

  /** SSE事件类型 - 错误 */
  public static final String SSE_EVENT_ERROR = "error";

  /** SSE事件类型 - 搜索 */
  public static final String SSE_EVENT_SEARCH = "search";

  /** SSE事件类型 - 思考 */
  public static final String SSE_EVENT_THINKING = "thinking";

  /** SSE事件类型 - 消息 */
  public static final String SSE_EVENT_MESSAGE = "message";

  /** 消息角色 - 用户 */
  public static final String ROLE_USER = "user";

  /** 消息角色 - 助手 */
  public static final String ROLE_ASSISTANT = "assistant";

  /** 消息角色 - 系统 */
  public static final String ROLE_SYSTEM = "system";

  /** 搜索事件状态 - 开始 */
  public static final String SEARCH_STATUS_START = "start";

  /** 搜索事件状态 - 完成 */
  public static final String SEARCH_STATUS_COMPLETE = "complete";

  /** 错误消息 - 无效会话ID */
  public static final String ERROR_INVALID_CONVERSATION_ID = "会话ID无效";

  /** 错误消息 - 空消息内容 */
  public static final String ERROR_EMPTY_MESSAGE_CONTENT = "消息内容不能为空";

  /** 错误消息 - AI服务异常 */
  public static final String ERROR_AI_SERVICE_EXCEPTION = "处理消息时发生错误: ";

  /** 错误消息 - 网络连接错误 */
  public static final String ERROR_NETWORK_CONNECTION = "网络连接错误";

  /** 错误消息 - AI服务失败 */
  public static final String ERROR_AI_SERVICE_FAILURE = "AI服务出现异常";

  /** HTTP头 - 内容类型 */
  public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

  /** HTTP头 - 授权 */
  public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";

  /** HTTP内容类型 - JSON */
  public static final String HTTP_CONTENT_TYPE_JSON = "application/json";

  /** HTTP授权前缀 - Bearer */
  public static final String HTTP_AUTH_BEARER_PREFIX = "Bearer ";

  /** HTTP状态码 - OK */
  public static final int HTTP_STATUS_OK = 200;

  /** 默认消息 - 开始 */
  public static final String DEFAULT_START_MESSAGE = "开始生成回复";

  /** 默认消息 - 抱歉 */
  public static final String DEFAULT_SORRY_MESSAGE = "抱歉，我现在无法回答您的问题，请稍后再试。";

  private AiChatConstants() {
    // 防止实例化
  }
}
