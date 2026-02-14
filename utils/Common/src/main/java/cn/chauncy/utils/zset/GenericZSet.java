package cn.chauncy.utils.zset;

import java.util.*;

public class GenericZSet<M, S> {

    private final SkipList<M, S> zsl;
    private final Map<M, S> dict;

    public GenericZSet(Comparator<M> objComparator, Comparator<S> scoreComparator, Map<M, S> dict) {
        this.zsl = new SkipList<>(objComparator, scoreComparator);
        this.dict = dict;
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
}
