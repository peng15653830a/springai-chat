import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class test_regex2 {
    public static void main(String[] args) {
        String testContent = "\"reasoning_content\":\"嗯，用户问的是为什么天空是蓝色的，需要简单解释。首先得回忆一下相关知识。\"";
        
        String pattern = "\"reasoning_content\"\\s*:\\s*\"([^\"]*?)\"";
        
        Pattern regex = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher matcher = regex.matcher(testContent);
        
        if (matcher.find()) {
            String reasoning = matcher.group(1).trim();
            System.out.println("成功解析推理内容:");
            System.out.println(reasoning);
            System.out.println("长度: " + reasoning.length());
        } else {
            System.out.println("未找到匹配项");
            System.out.println("测试内容: " + testContent);
        }
    }
}
