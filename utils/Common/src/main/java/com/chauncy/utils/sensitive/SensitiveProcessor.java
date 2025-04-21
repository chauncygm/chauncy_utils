package com.chauncy.utils.sensitive;

/**
 * 敏感词过滤处理器，默认按字符数替换成*
 */
public interface SensitiveProcessor {

    /**
     * 敏感词过滤处理
     *
     * @param foundWord 敏感词匹配到的内容
     * @return 敏感词过滤后的内容，默认按字符数替换成*
     */
    default String process(FoundWord foundWord) {
        int length = foundWord.getFoundWord().length();
        return "*".repeat(length);
    }
}
