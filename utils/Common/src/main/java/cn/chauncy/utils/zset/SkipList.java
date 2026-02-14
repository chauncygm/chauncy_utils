package cn.chauncy.utils.zset;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 跳表，即含多层索引的有序列表
 * <p>空间换时间，利用随机数生成节点的层数，生成接近二分查找的索引，实现快速查找范围元素</p>
 *
 */
public class SkipList<M, S> {

    /** 跳表最高层级，32层 */
    private static final int SKIP_LIST_MAX_LEVEL = 32;

    private final int[] rankCache = new int[SKIP_LIST_MAX_LEVEL];
    @SuppressWarnings("unchecked")
    private final SkipListNode<M, S>[] updateCache = new SkipListNode[SKIP_LIST_MAX_LEVEL];

    private final Comparator<M> objComparator;
    private final Comparator<S> scoreComparator;

    /**
     * 跳表头节点，默认为空值节点，主要存放head层级信息，next才是第一个节点
     */
    private final SkipListNode<M, S> head;

    /**
     * 跳表尾节点，跳表size为0时，尾节点为null
     */
    private SkipListNode<M, S> tail;

    /** 列表长度 */
    private int length;

    /**
     * 修改次数，防止错误的迭代
     */
    private int modCount;

    /**
     * 跳表最大层级
     */
    private int maxLevel;

    public SkipList(Comparator<M> objComparator, Comparator<S> scoreComparator) {
        this.head = zslCreateNode(SKIP_LIST_MAX_LEVEL, null ,null);
        this.objComparator = objComparator;
        this.scoreComparator = scoreComparator;
    }

    public int getLength() {
        return length;
    }

    int getModCount() {
        return modCount;
    }

    /**
     * 插入一个节点
     *
     * @param member 成员
     * @param score 分数
     * @return  插入的新节点
     */
    SkipListNode<M, S> zslInsert(M member, S score) {
        final int level = randomLevel();

        final int[] rank = rankCache;
        SkipListNode<M, S>[] update = updateCache;
        try {
            // 找到最大层级下插入节点每个层级的前驱节点和对应排名
            SkipListNode<M, S> preNode = head, curNode;
            for (int i = maxLevel - 1; i >= 0; i--) {
                rank[i] = (i == maxLevel - 1) ? 0 : rank[i + 1];

                curNode = preNode.levels[i].forward;
                while (curNode != null && compareScoreAndMember(curNode, score, member) < 0) {
                    rank[i] += preNode.levels[i].span;
                    preNode = curNode;
                    curNode = curNode.levels[i].forward;
                }
                update[i] = preNode;
            }

            // 如果插入节点高于最大层级，则高层的前驱为head且span为length
            if (level > maxLevel) {
                for (int i = maxLevel; i < level; i++) {
                    rank[i] = 0;
                    update[i] = head;
                    update[i].levels[i].span = this.length;
                }
                this.maxLevel = level;
            }

            /* 更新新节点的层级信息，每层的引用关系和跨度 */
            final SkipListNode<M, S> newNode = zslCreateNode(level, score, member);
            newNode.backward = (update[0] == head) ? null : update[0];
            for (int i = 0; i < level; i++) {
                ZSkipListLevel<M, S> levelPreNode = update[i].levels[i];
                newNode.levels[i].forward = levelPreNode.forward;
                levelPreNode.forward = newNode;

                int levelPreSpan = levelPreNode.span;
                // 层前驱跨度 = 当前节点排名(即直接前驱的排名+1) - 当前层前驱的排名
                levelPreNode.span = (rank[0] + 1) - rank[i];
                // 新节点层跨度 = 原前驱跨度 - 现前驱跨度 + 1
                newNode.levels[i].span = levelPreSpan - levelPreNode.span + 1;
            }


            // 更新直接后继节点的前驱
            SkipListNode<M, S> forward = newNode.forward0();
            if (forward != null) {
                forward.backward = newNode;
            } else {
                tail = newNode;
            }

            this.length++;
            this.modCount++;
            return newNode;
        } finally {
            releaseRankCache();
            releaseUpdateCache();
        }
    }

    /**
     * 删除一个节点
     *
     * @param member 节点成员
     * @param score 节点分数
     * @return 存在则删除成功，不存在则失败
     */
    boolean zslDelete(M member, S score) {
        final SkipListNode<M, S>[] update = updateCache;
        try {
            SkipListNode<M, S> preNode = head, curNode;
            for (int i = maxLevel - 1; i >= 0 ; i--) {
                curNode = preNode.levels[i].forward;
                while (curNode != null && compareScoreAndMember(curNode, score, member) < 0) {
                    preNode = curNode;
                    curNode = curNode.levels[i].forward;
                }
                update[i] = preNode;
            }

            curNode = update[0].forward0();
            if (curNode != null && compareScoreAndMember(curNode, score, member) == 0) {
                zslDeleteNode(curNode, update);
                return true;
            }
            return false;
        } finally {
            releaseUpdateCache();
        }
    }

    private void zslDeleteNode(SkipListNode<M, S> delNode, SkipListNode<M, S>[] update) {
        // 更新所有层的引用关系和跨度
        for (int i = maxLevel - 1; i >= 0; i--) {
            if (update[i].levels[i].forward == delNode) {
                // 该层存在指向删除节点的层，则跨度相加
                update[i].levels[i].forward = delNode.levels[i].forward;
                update[i].levels[i].span += delNode.levels[i].span - 1;
                delNode.levels[i].forward = null;
                delNode.levels[i].span = 0;
            } else {
                // 该层前驱的后继不指向删除节点的层,说明高度更高，则跨度减1即可
                update[i].levels[i].span--;
            }
        }

        // 更新删除节点的直接后继或尾结点
        if (delNode.forward0() != null) {
            delNode.forward0().backward = delNode.backward;
        } else {
            tail = delNode.backward;
        }

        // 如果是最高层级的节点，则需要重新计算最高层
        if (delNode.getLevel() == maxLevel) {
            while (maxLevel > 1 && head.levels[maxLevel - 1].forward == null) {
                maxLevel--;
            }
        }

        this.length--;
        this.modCount++;
    }

    /**
     * 判断是否存在分数在区间内的成员
     *
     * @param range 分数范围
     * @return 是否存在分数区间内的成员
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean zslIsInRange(ZScoreRangeSpec<S> range) {
        if (isEmptyRange(range)) {
            return false;
        }

        // 如果尾结点为空或者尾结点小于最小值，则返回false
        if (tail == null || !zslScoreGteMin(tail.score, range)) {
            return false;
        }

        // 如果头结点为空或者头结点大于最大值，则返回false
        final SkipListNode<M, S> firstNode = head.forward0();
        return firstNode != null && zslScoreLteMax(firstNode.score, range);
    }

    private boolean isEmptyRange(ZScoreRangeSpec<S> range) {
        int compare = scoreComparator.compare(range.min, range.max);
        return compare > 0 || (compare == 0 && !range.minInclusive && !range.maxInclusive);
    }

    /**
     * 获取分数区间内的成员数量
     *
     * @param range 分数区间
     * @return 分数区间内的成员数量
     */
    int zslCountInRange(ZScoreRangeSpec<S> range) {
        final SkipListNode<M, S> firstNode = zslFirstInRange(range);
        if (firstNode == null) {
            return 0;
        }

        final SkipListNode<M, S> lastNode = zslLastInRange(range);
        int firstNodeRank = zslGetRank(firstNode.score, firstNode.member);
        int lastNodeRank = zslGetRank(lastNode.score, lastNode.member);
        return lastNodeRank - firstNodeRank + 1;
    }

    SkipListNode<M, S> zslFirstInRange(ZScoreRangeSpec<S> range) {
        if (!zslIsInRange(range)) {
            return null;
        }

        SkipListNode<M, S> curNode = head;
        for (int i = 0; i < maxLevel - 1; i++) {
            while (curNode.levels[i].forward != null && !zslScoreGteMin(curNode.levels[i].forward.score, range)) {
                curNode = curNode.levels[i].forward;
            }
        }

        assert curNode != null;
        if (!zslScoreLteMax(curNode.score, range)) {
            return null;
        }
        return curNode;
    }

    SkipListNode<M, S> zslLastInRange(ZScoreRangeSpec<S> range) {
        if (!zslIsInRange(range)) {
            return null;
        }

        SkipListNode<M, S> curNode = head;
        for (int i = 0; i < maxLevel - 1; i++) {
            while (curNode.levels[i].forward != null && zslScoreLteMax(curNode.levels[i].forward.score, range)) {
                curNode = curNode.levels[i].forward;
            }
        }

        assert curNode != null;
        if (!zslScoreGteMin(curNode.score, range)) {
            return null;
        }
        return curNode;
    }

    /**
     * 通过指定排名获取节点
     *
     * @param rank 排名
     * @return 节点
     */
    SkipListNode<M, S> zslGetElementByRank(int rank) {
        int curRank = 0;
        SkipListNode<M, S> curNode = head;
        for (int i = maxLevel - 1; i >= 0; i--) {
            while (curNode.levels[i].forward != null && curRank +curNode.levels[i].span < rank) {
                curNode = curNode.levels[i].forward;
                curRank += curNode.levels[i].span;
            }
            if (curRank == rank) {
                return curNode;
            }
        }
        return null;
    }

    /**
     * 查找指定成员和分数的排名
     *
     * @param score 分数
     * @param member 成员
     * @return 排名
     */
     int zslGetRank(S score, M member) {
        int rank = 0;
        SkipListNode<M, S> curNode = head;
        for (int i = maxLevel - 1; i >= 0; i--) {
            while (curNode.levels[i].forward != null && compareScoreAndMember(curNode, score, member) < 0) {
                curNode = curNode.levels[i].forward;
                rank += curNode.levels[i].span;
            }
        }

        final SkipListNode<M, S> node = curNode.forward0();
        if (node != null && compareScoreAndMember(node, score, member) == 0) {
            return rank;
        }
        return 0;
    }

    /**
     * 删除指定排名的节点
     *
     * @param rank 排名
     * @param dict 成员字典 删除的成员会从该字典中删除
     * @return 删除的节点
     */
    SkipListNode<M, S> zslDeleteByRank(int rank, Map<M, S> dict) {
        if (rank <= 0 || rank > length) {
            return null;
        }

        SkipListNode<M, S>[] update = updateCache;
        try {
            int curRank = 0;
            SkipListNode<M, S> curNode = head;
            for (int i = maxLevel - 1; i >= 0; i--) {
                while (curNode.levels[i].forward != null && curRank + curNode.levels[i].span < rank) {
                    curNode = curNode.levels[i].forward;
                    curRank += curNode.levels[i].span;
                }
                update[i] = curNode;
            }

            final SkipListNode<M, S> delNode = curNode.forward0();
            if (delNode != null) {
                dict.remove(delNode.member);
                zslDeleteNode(delNode, update);
                return delNode;
            }
            return null;
        } finally {
            releaseUpdateCache();
        }
    }

    /**
     * 删除指定范围的节点
     *
     * @param start 起始排名
     * @param end 结束排名
     * @param dict 删除的成员会从该字典中删除
     * @return 删除的节点数量
     */
    int zslDeleteRangeByRank(int start, int end, Map<M, S> dict) {
        start = Math.max(start, 1);
        if (start > end || start > length) {
            return 0;
        }

        SkipListNode<M, S>[] update = updateCache;
        try {
            int curRank = 0, delCount = 0;
            SkipListNode<M, S> curNode = head;
            for (int i = maxLevel - 1; i >= 0; i--) {
                while (curNode.levels[i].forward != null && curRank + curNode.levels[i].span < start) {
                    curNode = curNode.levels[i].forward;
                    curRank += curNode.levels[i].span;
                }
                update[i] = curNode;
            }
            SkipListNode<M, S> node = curNode.forward0();
            curRank++;
            while (node != null && curRank <= end) {
                final SkipListNode<M, S> nextNode = node.forward0();
                zslDeleteNode(node, update);
                dict.remove(node.member);
                node = nextNode;

                curRank++;
                delCount++;
            }
            return delCount;
        } finally {
            releaseUpdateCache();
        }
    }

    int zslDeleteRangeByScore(ZScoreRangeSpec<S> range, Map<M, S> dict) {
        if (!zslIsInRange(range)) {
            return 0;
        }

        SkipListNode<M, S>[] update = updateCache;
        try {
            int delCount = 0;
            SkipListNode<M, S> curNode = head;
            for (int i = maxLevel - 1; i >= 0; i--) {
                while (curNode.levels[i].forward != null && !zslScoreGteMin(curNode.levels[i].forward.score, range)) {
                    curNode = curNode.levels[i].forward;
                }
                update[i] = curNode;
            }

            curNode = curNode.forward0();
            while (curNode != null && zslScoreLteMax(curNode.score, range)) {
                final SkipListNode<M, S> nextNode = curNode.forward0();
                zslDeleteNode(curNode, update);
                dict.remove(curNode.member);
                curNode = nextNode;
                delCount++;
            }
            return delCount;
        } finally {
            releaseUpdateCache();
        }
    }

    private int compareScoreAndMember(SkipListNode<M, S> node, S score, M member) {
        int result = scoreComparator.compare(node.score, score);
        if (result != 0) {
            return result;
        }
        return objComparator.compare(node.member, member);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean zslScoreGteMin(S score, ZScoreRangeSpec<S> range) {
        return range.minInclusive ? scoreComparator.compare(score, range.min) >= 0 : scoreComparator.compare(score, range.min) > 0;
    }

    private boolean zslScoreLteMax(S score, ZScoreRangeSpec<S> range) {
        return range.maxInclusive ? scoreComparator.compare(score, range.max) <= 0 : scoreComparator.compare(score, range.max) < 0;
    }

    private void releaseRankCache() {
        for (int i = 0; i < maxLevel; i++) {
            rankCache[i] = 0;
        }
    }

    private void releaseUpdateCache() {
        for (int i = 0; i < maxLevel; i++) {
            updateCache[i] = null;
        }
    }


    /**
     * 随机生成层级, 范围1-32级<br/>
     * 50%的概率提升一层，即每上升1层，节点数期望减少一半，查找效率等效于二分法，最大层数随节点数增加而增加，maxLevel约等于log2(N)<br/>
     *
     * @return 层级
     */
    private int randomLevel() {
        int level = 1;
        while (Math.random() < 0.5 && level < SKIP_LIST_MAX_LEVEL) {
            level++;
        }
        return level;
    }

    /**
     * 创建一个指定层级的跳表节点
     *
     * @param <K>   数据类型
     * @param level 节点的层级
     * @param score 节点对应的分数
     * @param member 节点对应的数据
     * @return 跳表节点
     */
    private static <K, S> SkipListNode<K, S> zslCreateNode(int level, S score, K member) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        SkipListNode<K, S> skipListNode = new SkipListNode<>(score, member, new ZSkipListLevel[level]);
        for (int i = 0; i < level; i++) {
            skipListNode.levels[i] = new ZSkipListLevel<>();
        }
        return skipListNode;
    }

    private ZScoreRangeSpec<S> newScoreRange(S min, S max) {
        return new ZScoreRangeSpec<>(min, max, true, true);
    }

    private ZScoreRangeSpec<S> newScoreRange(S min, S max, boolean minInclusive, boolean maxInclusive) {
        int compare = scoreComparator.compare(min, max);
        if (compare > 0) {
            return new ZScoreRangeSpec<>(max, min, maxInclusive, minInclusive);
        }
        return new ZScoreRangeSpec<>(min, max, minInclusive, maxInclusive);
    }

    record ZScoreRangeSpec<S>(S min, S max, boolean minInclusive, boolean maxInclusive) {}

    void printDetail() {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("=== SkipList Detailed Structure ===");
        System.out.println("Total nodes: " + length + ", Max level: " + maxLevel);

        if (length == 0) {
            System.out.println("SkipList is empty");
            return;
        }

        // 收集所有节点
        List<SkipListNode<?, ?>> allNodes = new ArrayList<>();
        SkipListNode<?, ?> current = head;
        while (current != null) {
            allNodes.add(current);
            current = current.forward0();
        }

        // 按层级打印
        for (int level = maxLevel - 1; level >= 0; level--) {
            System.out.printf("Level %2d : ", level);
            printLevel(allNodes, level);
        }

        //打印节点详细信息
        System.out.println("\nNode Details:");
        for (int i = 1; i < allNodes.size(); i++) {
            SkipListNode<?, ?> node = allNodes.get(i);
            System.out.printf("  Node %d: Score=%s, Value=%s\n", i, node.score, node.member);
        }
    }

    private void printLevel(java.util.List<SkipListNode<?, ?>> nodes, int level) {
        if (nodes.isEmpty()) {
            System.out.println("<empty>");
            return;
        }

        StringBuilder sb = new StringBuilder();
        SkipListNode<?, ?> current = head;
        while (current != null) {
            // 找到当前节点在基础层中的位置
            int position = nodes.indexOf(current);
            String posStr = "[" + position + (position < 10 ? " ": "") + "]";
            sb.append(posStr);

            ZSkipListLevel<?, ?> currentLevel = current.levels[level];
            current = current.levels[level].forward;
            if (currentLevel.forward != null) {
                String dicNum = " --" + currentLevel.span + "--> ";
                sb.append(dicNum);
                for (int i = 0; i < currentLevel.span - 1; i++) {
                    sb.append(" ".repeat(dicNum.length() + posStr.length()));
                }

                if (sb.toString().contains("---")) {
                    System.out.print("");
                }
            }
        }
        System.out.println(sb);
    }


    /**
     * 跳表节点
     */
    static class SkipListNode<K, S> implements Entry<K, S> {
        /**
         * 节点对应的数据
         */
        final K member;
        /**
         * 节点对应的分数
         */
        final S score;
        /**
         * 节点的层级信息，表示每层指向的后继节点，可有多个跨度
         * 第一层全指向直接后继节点，即levels[0]等价于forward，跨度为1
         */
        final ZSkipListLevel<K, S>[] levels;
        /**
         * 节点的前驱节点
         */
        SkipListNode<K, S> backward;

        public SkipListNode(S score, K member, ZSkipListLevel<K, S>[] levels) {
            Objects.requireNonNull(levels, "levels cannot be null");
            this.member = member;
            this.levels = levels;
            this.score = score;
        }

        /**
         * @return 节点的直接后继节点
         */
        public SkipListNode<K, S> forward0() {
            return levels[0].forward;
        }

        /**
         * @return 当前节点的层数
         */
        public int getLevel() {
            return levels.length;
        }

        @Override
        public K getKey() {
            return member;
        }

        @Override
        public S getValue() {
            return score;
        }
    }

    static class ZSkipListLevel<K, S> {
        /**
         * 节点层级的跳转节点
         */
        SkipListNode<K, S> forward;
        /**
         * 节点层级跳转的跨度
         */
        private int span;

        public void setSpan(int span) {
            if (span <= 0) {
                throw new IllegalArgumentException("span must be greater than 0");
            }
            this.span = span;
        }
    }


    public static void main(String[] args) {
        Random random = ThreadLocalRandom.current();
        SkipList<String, Long> skipList = new SkipList<>(String::compareTo, Long::compareTo);
        for (int i = 0; i < 30; i++) {
            long value = random.nextLong(500);
            skipList.zslInsert("key" + i, value);
        }
        skipList.printDetail();
    }
}
