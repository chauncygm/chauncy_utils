package cn.chauncy.utils;

import java.util.ArrayDeque;
import java.util.Deque;

public class RateLimiter {
    private final int maxOperations;
    private final long durationMillis;
    private final Deque<Long> window;

    public RateLimiter(int maxOperations) {
        this(maxOperations, 1);
    }

    public RateLimiter(int maxOperations, long durationMillis) {
        if (maxOperations <= 0 || durationMillis <= 0) {
            throw new IllegalArgumentException("maxOperations and durationMillis must be positive.");
        }
        this.maxOperations = maxOperations;
        this.durationMillis = durationMillis * 1_000_000L;
        this.window = new ArrayDeque<>(maxOperations);
    }

    public boolean allowOperation() {
        long now = System.nanoTime();
        while(!window.isEmpty() && window.getFirst() < now - durationMillis) {
            window.removeFirst();
        }

        if (window.size() < maxOperations) {
            window.addLast(now);
            return true;
        } else {
            return false;
        }
    }

    public long recentlyOpTime() {
        Long last = window.peekLast();
        return last == null ? -1L : last;
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = new RateLimiter(3, 10_000L);
        for(int i = 0; i < 6; i++) {
            System.out.println("Operation " + (limiter.allowOperation() ? "allowed" : "denied"));
            Thread.sleep(2000L);
        }
    }
}