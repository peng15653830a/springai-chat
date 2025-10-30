package com.example.novel.constant;

import com.example.constant.MessageRoles;

public final class NovelConstants {

  public static final String ROLE_USER = MessageRoles.USER;
  public static final String ROLE_ASSISTANT = MessageRoles.ASSISTANT;
  public static final String ROLE_SYSTEM = MessageRoles.SYSTEM;

  public static final String DEFAULT_SESSION_TITLE = "创作会话";

  public static final String PROVIDER_OLLAMA = "ollama";

  private NovelConstants() {
    throw new UnsupportedOperationException("Utility class");
  }
}
