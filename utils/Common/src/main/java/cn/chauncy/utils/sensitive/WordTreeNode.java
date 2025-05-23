package cn.chauncy.utils.sensitive;

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharOpenHashSet;
import it.unimi.dsi.fastutil.chars.CharSet;

/**
 * DFA（Deterministic Finite Automaton 确定有穷自动机）
 * DFA单词树节点
 */
public class WordTreeNode extends Char2ObjectOpenHashMap<WordTreeNode> {

    /**
     * 存放该节点的下一节点是尾单词的字符，用于确定是否已经完成单词匹配，是否还需要继续匹配。
     */
    private final CharSet endCharacterSet = new CharOpenHashSet();

    /**
     * 获取当前节点的指定节点，若没有，则添加
     *
     * @param c 查找和添加的节点
     */
    public WordTreeNode getAndAddWordIfNotExist(char c) {
        WordTreeNode child = get(c);
        if (child == null) {
            child = new WordTreeNode();
            put(c, child);
        }
        return child;
    }

    /**
     * 是否末尾
     *
     * @param c 检查的字符
     * @return 是否末尾
     */
    public boolean isEnd(char c) {
        return this.endCharacterSet.contains(c);
    }

    /**
     * 添加词到当前节点的尾词集
     *
     * @param c 结尾的字符
     */
    public void addEnd(char c) {
        this.endCharacterSet.add(c);
    }

    /**
     * 清除当前节点下所有的词
     * endCharacterSet 也将清空
     */
    @Override
    public void clear() {
        super.clear();
        this.endCharacterSet.clear();
    }
}
