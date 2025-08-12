package com.example.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI响应封装类
 *
 * @author xupeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
  /** 响应内容 */
  private String content;
  
  /** 思考过程 */
  private String thinking;
}
