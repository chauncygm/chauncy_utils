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

    public void zadd(M member, S score) {
        Objects.requireNonNull(member);
        Objects.requireNonNull(score);
        S oldScore = dict.put(member, score);
        if (oldScore != null) {
            zsl.zslDelete(member, oldScore);
        }
        zsl.zslInsert(member, score);
    }

    public boolean zaddnx(M member, S score) {
        S oldScore = dict.put(member, score);
        if (oldScore == null) {
            zsl.zslInsert(member, score);
            return true;
        }
        return false;
    }

    public S zrem(M member) {
        S oldScore = dict.remove(member);
        if (oldScore != null) {
            zsl.zslDelete(member, oldScore);
            return oldScore;
        }
        return null;
    }

    public int zremrangeByScore(S start, S end) {
        return zremrangeByScore(zsl.newScoreRange(start, end));
    }

    private int zremrangeByScore(SkipList.ZScoreRangeSpec<S> range) {
        return zsl.zslDeleteRangeByScore(range, dict);
    }

    public Entry<M, S> zpopFirst() {
        return zremByRank(0);
    }

    public Entry<M, S> zpopLast() {
        return zremByRank(zsl.getLength() - 1);
    }

    public Entry<M, S> zremByRank(int rank) {
        if (rank < 0 || rank >= zsl.getLength()) {
            return null;
        }
        SkipList.SkipListNode<M, S> msSkipListNode = zsl.zslDeleteByRank(rank + 1, dict);
        return new ZSetEntry<>(msSkipListNode.member, msSkipListNode.score);
    }

    public int zremrangeByRank(int start, int end) {
        int length = zsl.getLength();
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        start = Math.max(0, start);
        end = Math.min(length - 1, end);
        if (start > length || start > end) {
            return 0;
        }

        return zsl.zslDeleteRangeByRank(start + 1, end + 1, dict);
    }

    public Entry<M, S> zmemeberByRank(int rank) {
        if (rank < 0 || rank >= zsl.getLength()) {
            return null;
        }
        SkipList.SkipListNode<M, S> node = zsl.zslGetElementByRank(rank + 1);
        return new ZSetEntry<>(node.member, node.score);
    }

    public List<Entry<M, S> > zrangeByRank(int start, int end) {
        int length = zsl.getLength();
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        start = Math.max(0, start);
        end = Math.min(length - 1, end);
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

    public int zcard() {
        return zsl.getLength();
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
        private SkipList.SkipListNode<M, S> nextNode;
        private int expectedModCount;

        public Itr(SkipList.SkipListNode<M, S> node) {
            this.nextNode = node;
            this.expectedModCount = zsl.getModCount();
        }

        @Override
        public boolean hasNext() {
            return nextNode.forward0() != null;
        }

        @Override
        public Entry<M, S> next() {
            checkForModification();

            nextNode = nextNode.forward0();
            curNode = nextNode;
            return curMember();
        }

        protected Entry<M, S> curMember() {
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

    public static void main(String[] args) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        HashMap<String, Long> dict = new HashMap<>();
        GenericZSet<String, Long> zset = new GenericZSet<>(Comparator.comparingLong(String::hashCode),
                Comparator.comparingLong(Long::longValue), dict);
        for (int i = 0; i < 100; i++) {
            zset.zadd("key" + i, random.nextLong(500));
        }
        System.out.println(zset.zcard());
        Entry<String, Long> stringLongEntry = zset.zmemeberByRank(zset.zcard() - 1);

        System.out.println(stringLongEntry);
    }
}
