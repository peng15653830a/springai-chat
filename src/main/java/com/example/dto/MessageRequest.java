package com.example.dto;

import lombok.Data;

/**
 * 消息请求DTO，用于接收客户端发送的消息内容和搜索开关设置
 *
 * @author xupeng
 */
@Data
public class MessageRequest {
  /** 消息内容 */
  private String content;
  
  /** 搜索开关，默认开启 */
  private Boolean searchEnabled = true;
}
