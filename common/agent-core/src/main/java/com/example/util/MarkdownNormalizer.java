package com.example.util;

public class MarkdownNormalizer {

  private static final java.util.regex.Pattern INLINE_BULLET = java.util.regex.Pattern.compile("(?m)^-\\S");
  private static final java.util.regex.Pattern INLINE_CONCAT_LIST =
      java.util.regex.Pattern.compile("(?<!^)\\S-([\\u4e00-\\u9fa5A-Za-z])");
  private static final java.util.regex.Pattern BOLD_AT_MIDLN_HEADING =
      java.util.regex.Pattern.compile("(?<!\\n)\\*\\*([\\u4e00-\\u9fa5A-Za-z0-9：:（）()\u3000\s]{2,40})\\*\\*(?=\\n|$)");

  public static String normalize(String text) {
    if (text == null || text.isBlank()) return text;
    String s = text;
    s = INLINE_BULLET.matcher(s).replaceAll(m -> m.group().charAt(0) + " " + m.group().substring(1));
    s = INLINE_CONCAT_LIST.matcher(s).replaceAll("\n- $1");
    s = BOLD_AT_MIDLN_HEADING.matcher(s).replaceAll("\n**$1**");
    int fences = countOccurrences(s, "```\n");
    fences += countOccurrences(s, "```");
    if ((fences % 2) == 1) {
      s = s + "\n```\n";
    }
    s = s.replaceAll("(?m)([^\n])\n(- )", "$1\n\n$2");
    s = s.replaceAll("(?m)([^\n])\n(\\*\\*[^\n]+\\*\\*)$", "$1\n\n$2");
    s = s.replaceAll("(?m)^(\\*\\*[^\n]+\\*\\*)\n(?!\n)", "$1\n\n");
    return s;
  }

  private static int countOccurrences(String s, String sub) {
    int count = 0, idx = 0;
    while ((idx = s.indexOf(sub, idx)) != -1) {
      count++;
      idx += sub.length();
    }
    return count;
  }
}

