package cn.chauncy.utils.zset;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GenericZSet<M, S> {

    private final SkipList<M, S> zsl;
    private final Map<M, S> dict;

    public GenericZSet(Comparator<M> objComparator, Comparator<S> scoreComparator, Map<M, S> dict) {
        this.zsl = new SkipList<>(objComparator, scoreComparator);
        this.dict = dict;
    }


    //region 查询

    /**
     * 查询某个成员的分数
     *
     * @param member 成员
     * @return  成员对应的分数
     */
    public S zscore(M member) {
        Objects.requireNonNull(member);
        return dict.get(member);
    }

    /**
     * 获取某个成员的排名
     *
     * @param member 成员
     * @return  成员的排名，从1开始，不存在则返回-1
     */
    public int zrank(M member) {
        S score = zscore(member);
        if (score == null) {
            return -1;
        }
        return zsl.zslGetRank(score, member);
    }

    /**
     * 获取某个排名的成员及分数
     *
     * @param rank 排名，从1开始
     * @return  该排名的成员及分数
     */
    public Entry<M, S> zmemberByRank(int rank) {
        if (rank <= 0 || rank > zsl.getLength()) {
            return null;
        }
        SkipList.SkipListNode<M, S> node = zsl.zslGetElementByRank(rank);
        return new ZSetEntry<>(node.member, node.score);
    }

    /**
     * 获取指定排名范围的成员及分数
     *
     * @param start 起始排名
     * @param end   结束排名
     * @return  该排名的成员及分数
     */
    public List<Entry<M, S> > zrangeByRank(int start, int end) {
        int length = zsl.getLength();
        if (start < 0) {
            start += length + 1;
        }
        if (end < 0) {
            end += length + 1;
        }
        start = Math.max(1, start);
        end = Math.min(length, end);
        if (start > length || start > end) {
            return Collections.emptyList();
        }

        int rangeLen = end - start + 1;

        final List<Entry<M, S>> result = new ArrayList<>(rangeLen);
        SkipList.SkipListNode<M, S> node = zsl.zslGetElementByRank(start);
        while (node != null && rangeLen-- > 0) {
            result.add(new ZSetEntry<>(node.member, node.score));
            node = node.forward0();
        }
        return result;
    }

    /**
     * 获取某个分数范围的成员及分数
     *
     * @param start 起始分数
     * @param end   结束分数
     * @return  该分数范围的成员及分数
     */
    public List<Entry<M, S>> zrangeByScore(S start, S end) {
        SkipList.ZScoreRangeSpec<S> scoreRangeSpec = zsl.newScoreRange(start, end);
        SkipList.SkipListNode<M, S> node = zsl.zslFirstInRange(scoreRangeSpec);
        if (node == null) {
            return List.of();
        }

        final List<Entry<M, S>> result = new ArrayList<>();
        while (node != null) {
            if (zsl.zslScoreLteMax(node.score, scoreRangeSpec)) {
                result.add(new ZSetEntry<>(node.member, node.score));
            } else {
                break;
            }
            node = node.forward0();
        }
        return result;
    }

    /**
     * 获取某个分数范围的成员数量
     *
     * @param start 起始分数
     * @param end   结束分数
     * @return  该分数范围的成员数量
     */
    public int zcount(S start, S end) {
        SkipList.ZScoreRangeSpec<S> scoreRangeSpec = zsl.newScoreRange(start, end);
        SkipList.SkipListNode<M, S> firstNode = zsl.zslFirstInRange(scoreRangeSpec);
        if (firstNode != null) {
            int firstNodeRank = zsl.zslGetRank(firstNode.score, firstNode.member);
            SkipList.SkipListNode<M, S> lastNode = zsl.zslLastInRange(scoreRangeSpec);
            int lastNodeRank = zsl.zslGetRank(lastNode.score, lastNode.member);
            return lastNodeRank - firstNodeRank + 1;
        }
        return 0;
    }

    /**
     * 获取集合的成员数量
     *
     * @return  集合的成员数量
     */
    public int zcard() {
        return zsl.getLength();
    }
    //endregion

    //region 添加成员

    /**
     * 添加成员
     *
     * @param member 成员
     * @param score 分数
     */
    public void zadd(M member, S score) {
        Objects.requireNonNull(member);
        Objects.requireNonNull(score);
        S oldScore = dict.put(member, score);
        if (oldScore != null) {
            zsl.zslDelete(member, oldScore);
        }
        zsl.zslInsert(member, score);
    }

    /**
     * 添加成员，如果已存在则返回false
     *
     * @param member 成员
     * @param score 分数
     * @return  是否添加成功
     */
    public boolean zaddnx(M member, S score) {
        S oldScore = dict.putIfAbsent(member, score);
        if (oldScore == null) {
            zsl.zslInsert(member, score);
            return true;
        }
        return false;
    }
    //endregion

    //region 删除成员
    /**
     * 删除成员
     *
     * @param member 成员
     * @return 删除成员的分数
     */
    public S zrem(M member) {
        S oldScore = dict.remove(member);
        if (oldScore != null) {
            zsl.zslDelete(member, oldScore);
            return oldScore;
        }
        return null;
    }

    /**
     * 删除某个分数范围的成员
     *
     * @param start 起始分数
     * @param end 结束分数
     * @return 删除的成员数量
     */
    public int zremrangeByScore(S start, S end) {
        return zremrangeByScore(zsl.newScoreRange(start, end));
    }

    private int zremrangeByScore(SkipList.ZScoreRangeSpec<S> range) {
        return zsl.zslDeleteRangeByScore(range, dict);
    }

    /**
     * 弹出并删除第一名
     */
    public Entry<M, S> zpopFirst() {
        return zremByRank(1);
    }

    /**
     * 弹出并删除最后一名
     */
    public Entry<M, S> zpopLast() {
        return zremByRank(zsl.getLength());
    }

    /**
     * 删除指定排名的成员
     * @param rank 排名，从1开始
     * @return 删除的成员及分数
     */
    public Entry<M, S> zremByRank(int rank) {
        if (rank <= 0 || rank > zsl.getLength()) {
            return null;
        }
        SkipList.SkipListNode<M, S> msSkipListNode = zsl.zslDeleteByRank(rank, dict);
        return new ZSetEntry<>(msSkipListNode.member, msSkipListNode.score);
    }

    /**
     * 删除指定排名范围的成员
     * @param start 起始分数
     * @param end 结束分数
     * @return 删除的成员数量
     */
    public int zremrangeByRank(int start, int end) {
        int length = zsl.getLength();
        if (start < 0) {
            start += length + 1;
        }
        if (end < 0) {
            end += length + 1;
        }
        start = Math.max(1, start);
        end = Math.min(length, end);
        if (start > length || start > end) {
            return 0;
        }

        return zsl.zslDeleteRangeByRank(start, end, dict);
    }

    /**
     * 限制数量
     *
     * @param count 数量
     * @return 删除的成员数量
     */
    public int zlimit(int count) {
        int length = zsl.getLength();
        if (length <= count) {
            return 0;
        }
        return zsl.zslDeleteRangeByRank(count + 1, length, dict);
    }
    //endregion

    public SkipList<M, S> getSkipList() {
        return zsl;
    }

    public Iterator<Entry<M, S> > scan() {
        return scan(0);
    }

    public Iterator<Entry<M, S> > scan(int offset) {
        if (offset <= 0) {
            return new Itr(zsl.head.forward0());
        }
        if (offset >= zsl.getLength()) {
            return new Itr(null);
        }
        return new Itr(zsl.zslGetElementByRank(offset + 1));
    }

    private class Itr implements Iterator<Entry<M, S>> {

        private SkipList.SkipListNode<M, S> curNode;
        private int expectedModCount;

        public Itr(SkipList.SkipListNode<M, S> node) {
            this.curNode = node;
            this.expectedModCount = zsl.getModCount();
        }

        @Override
        public boolean hasNext() {
            return curNode != null;
        }

        @Override
        public Entry<M, S> next() {
            checkForModification();

            Entry<M, S> node = curMember(curNode);
            curNode = curNode.forward0();
            return node;
        }

        protected Entry<M, S> curMember(SkipList.SkipListNode<M, S> curNode) {
            // or return new ZSetEntry(curNode.member, curNode.score);
            return curNode;
        }

        @Override
        public void remove() {
            if (curNode == null) {
                throw new IllegalStateException();
            }
            checkForModification();

            dict.remove(curNode.member);
            zsl.zslDelete(curNode.member, curNode.score);
            expectedModCount = zsl.getModCount();

            curNode = null;
        }

        protected void checkForModification() {
            if (expectedModCount != zsl.getModCount())
                throw new ConcurrentModificationException();
        }
    }

    static class ZSetEntry<K, S> implements Entry<K, S> {

        private final K member;
        private final S score;

        public ZSetEntry(K member, S score) {
            this.member = member;
            this.score = score;
        }

        @Override
        public K getKey() {
            return member;
        }

        @Override
        public S getValue() {
            return score;
        }

        @Override
        public String toString() {
            return "ZSetEntry{" +
                    "member=" + member +
                    ", score=" + score +
                    '}';
        }
    }
}
