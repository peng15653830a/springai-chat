import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class test_regex {
    public static void main(String[] args) {
        // 模拟ModelScope API响应中的reasoning_content字段
        String testContent = "\"reasoning_content\":\"嗯，用户问的是为什么天空是蓝色的，需要简单解释。首先得回忆一下相关知识。记得这跟光的散射有关，可能是瑞利散射？对，应该是瑞利散射，因为气体分子对短波长的光散射更强。\"";
        
        String[] patterns = {
            "\"reasoning_content\"\\\\s*:\\\\s*\"([^\"]*?)\"(?=\\\\s*[,}])",
            "reasoning_content[\"']?\\\\s*[=:]\\\\s*[\"']([^\"']*?)[\"']",
            "reasoning_content\\\\s*=\\\\s*([^,}\\\\n]*)",
            "reasoning_content[\"']?\\\\s*[=:]\\\\s*([^,}\\\\n]*)"
        };
        
        for (String patternStr : patterns) {
            try {
                Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(testContent);
                if (matcher.find()) {
                    String reasoning = matcher.group(1).trim();
                    System.out.println("Pattern: " + patternStr);
                    System.out.println("Found: " + reasoning);
                    System.out.println("Length: " + reasoning.length());
                    System.out.println("---");
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error with pattern: " + patternStr + " - " + e.getMessage());
            }
        }
    }
}
