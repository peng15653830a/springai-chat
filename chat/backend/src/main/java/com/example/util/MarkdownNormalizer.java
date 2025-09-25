package com.example.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very light post-processor to improve readability of LLM outputs without enforcing strict format.
 * - Ensure list bullets have a space after dash: "-X" -> "- X"
 * - Break inline list concatenation: "……人-以色列：" -> "……人\n- 以色列："
 * - Ensure code fence closed when there is an odd number of ```
 * - Add a blank line between consecutive blocks heuristically (conservative)
 */
public class MarkdownNormalizer {

  private static final Pattern INLINE_BULLET = Pattern.compile("(?m)^-\\S");
  private static final Pattern INLINE_CONCAT_LIST =
      Pattern.compile("(?<!^)\\S-([\\u4e00-\\u9fa5A-Za-z])"); // "…字-以" -> break line
  private static final Pattern BOLD_AT_MIDLN_HEADING =
      Pattern.compile("(?<!\\n)\\*\\*([\\u4e00-\\u9fa5A-Za-z0-9：:（）()\u3000\s]{2,40})\\*\\*(?=\\n|$)");

  public static String normalize(String text) {
    if (text == null || text.isBlank()) return text;
    String s = text;

    // 1) Ensure dash-space for bullets at line start
    s = INLINE_BULLET.matcher(s).replaceAll(m -> m.group().charAt(0) + " " + m.group().substring(1));

    // 2) Break obvious inline-concatenated list markers: "……人-以色列：" => newline before dash
    s = INLINE_CONCAT_LIST.matcher(s).replaceAll("\n- $1");

    // 2.1) If a bold heading appears mid-line, move it to a new line (common with **国防预算**黏连)
    s = BOLD_AT_MIDLN_HEADING.matcher(s).replaceAll("\n**$1**");

    // 3) Ensure triple backticks are closed if opened odd number of times
    int fences = countOccurrences(s, "```\n");
    fences += countOccurrences(s, "```");
    if ((fences % 2) == 1) {
      s = s + "\n```\n";
    }

    // 4) Add a blank line between a paragraph and a list start (conservative)
    s = s.replaceAll("(?m)([^\n])\n(- )", "$1\n\n$2");

    // 5) Ensure a blank line before and after a bold-only line (section-like)
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
