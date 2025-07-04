package cn.chauncy.utils.sensitive;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * 敏感词工具类
 */
@ThreadSafe
public final class SensitiveFilter {

    /** 默认敏感词替换为(*) */
    private static final SensitiveProcessor DEFAULT_PROCESSOR = new SensitiveProcessor() {};

    /** 敏感词树 */
    private final WordTree sensitiveTree = new WordTree();
    /** 乐观锁 */
    private final StampedLock stampedLock = new StampedLock();

    /** 空的敏感词树 */
    public SensitiveFilter() {}

    /**
     * 初始化敏感词树
     *
     * @param sensitiveWords 敏感词列表
     */
    public SensitiveFilter(Collection<String> sensitiveWords) {
        sensitiveTree.addWords(sensitiveWords);
    }

    /** 是否存在敏感词 */
    public boolean hasSensitiveWords() {
        return executeWithOptimisticRead(() -> !sensitiveTree.isEmpty());
    }

    /** 添加敏感词 */
    public void add(Collection<String> sensitiveWords) {
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return;
        }

        long stamp = stampedLock.writeLock();
        try {
            sensitiveTree.addWords(sensitiveWords);
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    /** 清空敏感词库 */
    public void clear() {
        long stamp = stampedLock.writeLock();
        try {
            sensitiveTree.clear();
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    /**
     * 设置字符过滤规则，通过定义字符串过滤规则，过滤不需要的字符<br>
     * 当accept为false时，此字符不参与匹配
     *
     * @param charFilter 过滤函数
     * @since 5.4.4
     */
    public void setCharFilter(Filter<Character> charFilter) {
        long stamp = stampedLock.writeLock();
        try {
            if (charFilter != null) {
                sensitiveTree.setCharFilter(charFilter);
            }
        } finally {
            stampedLock.unlockWrite(stamp);
        }
    }

    /**
     * 是否包含敏感词
     *
     * @param text 文本
     * @return 是否包含
     */
    public boolean containsSensitive(String text) {
        return executeWithOptimisticRead(() -> sensitiveTree.isMatch(text));
    }

    /**
     * 查找敏感词，返回找到的第一个敏感词
     *
     * @param text 文本
     * @return 敏感词
     * @since 5.5.3
     */
    public FoundWord getFoundFirstSensitive(String text) {
        return executeWithOptimisticRead(() -> sensitiveTree.matchWord(text));
    }

    /**
     * 查找敏感词，返回找到的所有敏感词
     *
     * @param text 文本
     * @return 敏感词
     * @since 5.5.3
     */
    public List<FoundWord> getFoundAllSensitive(String text) {
        return executeWithOptimisticRead(() -> sensitiveTree.matchAllWords(text));
    }

    /**
     * 查找敏感词，返回找到的所有敏感词<br>
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]<br>
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           文本
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 敏感词
     */
    public List<FoundWord> getFoundAllSensitive(String text, boolean isDensityMatch, boolean isGreedMatch) {
        return executeWithOptimisticRead(() -> sensitiveTree.matchAllWords(text, -1, isDensityMatch, isGreedMatch));
    }

    /**
     * 处理过滤文本中的敏感词，默认替换成*
     *
     * @param text 文本
     * @return 敏感词过滤处理后的文本
     * @since 5.7.21
     */
    public String filter(String text) {
        return filter(text, true, null);
    }

    /**
     * 处理过滤文本中的敏感词，默认替换成*
     *
     * @param text               文本
     * @param isGreedMatch       贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     * @param sensitiveProcessor 敏感词处理器，默认按匹配内容的字符数替换成*
     * @return 敏感词过滤处理后的文本
     */
    public String filter(String text, boolean isGreedMatch, SensitiveProcessor sensitiveProcessor) {
        if (text.isBlank()) {
            return text;
        }

        //敏感词过滤场景下，不需要密集匹配
        final List<FoundWord> foundWordList = getFoundAllSensitive(text, true, isGreedMatch);
        if (foundWordList.isEmpty()) {
            return text;
        }
        sensitiveProcessor = sensitiveProcessor == null ? DEFAULT_PROCESSOR : sensitiveProcessor;

        int lastIndex = 0;
        final StringBuilder textBuilder = new StringBuilder();
        for (FoundWord fw : foundWordList) {
            if (fw.getStartIndex() >= lastIndex) {
                textBuilder.append(text, lastIndex, fw.getStartIndex());
                textBuilder.append(sensitiveProcessor.process(fw));
                lastIndex = fw.getEndIndex() + 1;
            }
        }
        textBuilder.append(text.substring(lastIndex));
        return textBuilder.toString();
    }

    private <T> T executeWithOptimisticRead(Supplier<T> supplier) {
        long stamp = stampedLock.tryOptimisticRead();
        T result = supplier.get();
        if (!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                result = supplier.get();
            } finally {
                stampedLock.unlockRead(stamp);
            }
        }
        return result;
    }
}
