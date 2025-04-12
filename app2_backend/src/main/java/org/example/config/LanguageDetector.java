package org.example.config;

import org.springframework.stereotype.Component;

@Component
public class LanguageDetector {
    public String detectLanguage(String text) {
        if (text == null || text.isEmpty()) {
            return "en"; // 默认英文
        }
        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return "zh"; // 中文
            }
        }
        return "en"; // 英文或其他
    }
}
