package cn.chauncy.utils.sensitive;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DFA（Deterministic Finite Automaton 确定有穷自动机）
 * DFA单词树（以下简称单词树），常用于在某大段文字中快速查找某几个关键词是否存在。<br>
 * 单词树使用group区分不同的关键字集合，不同的分组可以共享树枝，避免重复建树。<br>
 * 单词树使用树状结构表示一组单词。<br>
 * 例如：红领巾，红河构建树后为：<br>
 * 红                    <br>
 * /      \                 <br>
 * 领         河             <br>
 * /                            <br>
 * 巾                            <br>
 * 其中每个节点都是一个WordTreeNode对象，查找时从上向下查找。<br>
 */
@SuppressWarnings("UnusedReturnValue")
@NotThreadSafe
public class WordTree {


    /** 根节点（入口节点） */
    private final WordTreeNode wordTreeEntry = new WordTreeNode();

    /** 字符过滤规则，通过定义字符串过滤规则，过滤不需要的字符，当accept为false时，此字符不参与匹配 */
    private Filter<Character> charFilter = StopChar::isNotStopChar;

    /**
     * 设置字符过滤规则，通过定义字符串过滤规则，过滤不需要的字符<br>
     * 当accept为false时，此字符不参与匹配
     *
     * @param charFilter 过滤函数
     * @return this
     * @since 5.2.0
     */
    public WordTree setCharFilter(Filter<Character> charFilter) {
        this.charFilter = charFilter;
        return this;
    }

    // region add word

    /**
     * 增加一组单词
     *
     * @param words 单词集合
     * @return this
     */
    public WordTree addWords(Collection<String> words) {
        if (!(words instanceof Set)) {
            words = new HashSet<>(words);
        }
        for (String word : words) {
            addWord(word);
        }
        return this;
    }

    /**
     * 添加单词，使用默认类型
     *
     * @param word 单词
     * @return this
     */
    public WordTree addWord(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException();
        }

        WordTreeNode child;
        WordTreeNode parent = null;
        WordTreeNode current = wordTreeEntry;

        char currentChar = 0;
        final int length = word.length();
        for (int i = 0; i < length; i++) {
            currentChar = word.charAt(i);
            if (charFilter.accept(currentChar)) {//只处理合法字符
                child = current.getAndAddWordIfNotExist(currentChar);
                parent = current;
                current = child;
            }
        }
        if (null != parent) {
            parent.addEnd(currentChar);
        }
        return this;
    }

    // endregion

    // region match

    /**
     * 指定文本是否包含树中的词
     *
     * @param text 被检查的文本
     * @return 是否包含
     */
    public boolean isMatch(String text) {
        if (null == text) {
            return false;
        }
        return null != matchWord(text);
    }

    /**
     * 获得第一个匹配的关键字
     *
     * @param text 被检查的文本
     * @return 匹配到的关键字
     */
    public String match(String text) {
        final FoundWord foundWord = matchWord(text);
        return Objects.toString(foundWord);
    }

    /**
     * 获得第一个匹配的关键字
     *
     * @param text 被检查的文本
     * @return 匹配到的关键字
     * @since 5.5.3
     */
    public FoundWord matchWord(String text) {
        if (null == text) {
            return null;
        }
        final List<FoundWord> matchAll = matchAllWords(text, 1);
        return matchAll.isEmpty() ? null : matchAll.get(0);
    }

    // endregion

    // region match all

    /**
     * 找出所有匹配的关键字
     *
     * @param text 被检查的文本
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text) {
        return matchAll(text, -1);
    }

    /**
     * 找出所有匹配的关键字
     *
     * @param text 被检查的文本
     * @return 匹配的词列表
     * @since 5.5.3
     */
    public List<FoundWord> matchAllWords(String text) {
        return matchAllWords(text, -1);
    }

    /**
     * 找出所有匹配的关键字
     *
     * @param text  被检查的文本
     * @param limit 限制匹配个数
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text, int limit) {
        return matchAll(text, limit, false, false);
    }

    /**
     * 找出所有匹配的关键字
     *
     * @param text  被检查的文本
     * @param limit 限制匹配个数
     * @return 匹配的词列表
     * @since 5.5.3
     */
    public List<FoundWord> matchAllWords(String text, int limit) {
        return matchAllWords(text, limit, false, false);
    }

    /**
     * 找出所有匹配的关键字<br>
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]<br>
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           被检查的文本
     * @param limit          限制匹配个数
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 匹配的词列表
     */
    public List<String> matchAll(String text, int limit, boolean isDensityMatch, boolean isGreedMatch) {
        final List<FoundWord> matchAllWords = matchAllWords(text, limit, isDensityMatch, isGreedMatch);
        return matchAllWords.stream()
                .map(Objects::toString)
                .collect(Collectors.toList());
    }

    /**
     * 找出所有匹配的关键字<br>
     * 密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]<br>
     * 贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
     *
     * @param text           被检查的文本
     * @param limit          限制匹配个数
     * @param isDensityMatch 是否使用密集匹配原则
     * @param isGreedMatch   是否使用贪婪匹配（最长匹配）原则
     * @return 匹配的词列表
     * @since 5.5.3
     */
    public List<FoundWord> matchAllWords(String text, int limit, boolean isDensityMatch, boolean isGreedMatch) {
        if (null == text) {
            return List.of();
        }

        List<FoundWord> foundWords = null;
        WordTreeNode current = wordTreeEntry;
        final int length = text.length();
        final Filter<Character> charFilter = this.charFilter;
        //存放查找到的字符缓存。完整出现一个词时加到findedWords中，否则清空
        final StringBuilder wordBuffer = new StringBuilder();
        final StringBuilder keyBuffer = new StringBuilder();
        char currentChar;
        for (int i = 0; i < length; i++) {
            wordBuffer.setLength(0);
            keyBuffer.setLength(0);
            for (int j = i; j < length; j++) {
                currentChar = text.charAt(j);
//				Console.log("i: {}, j: {}, currentChar: {}", i, j, currentChar);
                if (!charFilter.accept(currentChar)) {
                    if (!wordBuffer.isEmpty()) {
                        //做为关键词中间的停顿词被当作关键词的一部分被返回
                        wordBuffer.append(currentChar);
                    } else {
                        //停顿词做为关键词的第一个字符时需要跳过
                        i++;
                    }
                    continue;
                } else if (!current.containsKey(currentChar)) {
                    //非关键字符被整体略过，重新以下个字符开始检查
                    break;
                }
                wordBuffer.append(currentChar);
                keyBuffer.append(currentChar);
                if (current.isEnd(currentChar)) {
                    if (foundWords == null) {
                        foundWords = new ArrayList<>();
                    }
                    //到达单词末尾，关键词成立，从此词的下一个位置开始查找
                    foundWords.add(new FoundWord(keyBuffer.toString(), wordBuffer.toString(), i, j));
                    if (limit > 0 && foundWords.size() >= limit) {
                        //超过匹配限制个数，直接返回
                        return foundWords;
                    }
                    if (!isDensityMatch) {
                        //如果非密度匹配，跳过匹配到的词
                        i = j;
                        break;
                    }
                    if (!isGreedMatch) {
                        //如果懒惰匹配（非贪婪匹配）。当遇到第一个结尾标记就结束本轮匹配
                        break;
                    }
                }
                current = current.get(currentChar);
                if (null == current) {
                    break;
                }
            }
            current = wordTreeEntry;
        }
        return foundWords == null ? List.of() : foundWords;
    }
    // endregion

    public void clear() {
        wordTreeEntry.clear();
    }

    public boolean isEmpty() {
        return wordTreeEntry.isEmpty();
    }
}