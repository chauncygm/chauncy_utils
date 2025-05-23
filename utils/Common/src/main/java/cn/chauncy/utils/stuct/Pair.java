package cn.chauncy.utils.stuct;

import org.checkerframework.checker.nullness.qual.NonNull;

public record Pair<F, S>(F first, S second) {

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<F, S>(first, second);
    }

    @Override
    @NonNull
    public String toString() {
        return "Pair{" + first + ", " + second + '}';
    }
}
