package com.chauncy.utils.collection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 集合工具类
 */
public class CollectionUtils {

    private CollectionUtils() {}

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static <T> boolean identityContains(List<T> list, T element) {
        return indexOfIdentity(list, element) >= 0;
    }

    public static int indexOfIdentity(List<?> list, Object element) {
        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i) == element) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOfIdentity(List<?> list, Object element) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == element) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int indexOfPredicate(List<T> list, Predicate<T> predicate) {
        for (int i = 0, size = list.size(); i < size; i++) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int lastIndexOfPredicate(List<T> list, Predicate<T> predicate) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (predicate.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

}
