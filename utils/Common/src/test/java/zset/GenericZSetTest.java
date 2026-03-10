package zset;

import cn.chauncy.utils.zset.GenericZSet;
import cn.chauncy.utils.zset.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GenericZSet 测试类
 * 测试跳表有序集合的各种功能
 */
public class GenericZSetTest {

    private GenericZSet<String, Integer> zset;
    private GenericZSet<String, Double> doubleZSet;

    @BeforeEach
    void setUp() {
        // 初始化整数分数的zset
        zset = new GenericZSet<>(
            String::compareTo,
            Integer::compareTo,
            new HashMap<>()
        );
        
        // 初始化双精度浮点数分数的zset
        doubleZSet = new GenericZSet<>(
            String::compareTo,
            Double::compareTo,
            new ConcurrentHashMap<>()
        );
    }

    @Test
    void testBasicAddAndGet() {
        // 测试基本添加和查询功能
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);

        assertEquals(Integer.valueOf(100), zset.zscore("player1"));
        assertEquals(Integer.valueOf(200), zset.zscore("player2"));
        assertEquals(Integer.valueOf(150), zset.zscore("player3"));
        assertNull(zset.zscore("nonexistent"));

        assertEquals(3, zset.zcard());
    }

    @Test
    void testAddNx() {
        // 测试添加不重复元素
        assertTrue(zset.zaddnx("player1", 100));
        assertFalse(zset.zaddnx("player1", 150)); // 已存在，应该返回false
        assertEquals(Integer.valueOf(100), zset.zscore("player1")); // 分数不应该改变
    }

    @Test
    void testUpdateScore() {
        // 测试更新已有元素的分数
        zset.zadd("player1", 100);
        zset.zadd("player1", 200); // 更新分数
        
        assertEquals(Integer.valueOf(200), zset.zscore("player1"));
        assertEquals(1, zset.zcard()); // 应该还是只有一个元素
    }

    @Test
    void testRankOperations() {
        // 测试排名相关操作
        zset.zadd("player3", 150); // rank 2
        zset.zadd("player1", 100); // rank 1
        zset.zadd("player2", 200); // rank 3

        zset.getSkipList().printDetail();

        assertEquals(1, zset.zrank("player1")); // 分数100，排名第1
        assertEquals(2, zset.zrank("player3")); // 分数150，排名第2
        assertEquals(3, zset.zrank("player2")); // 分数200，排名第3
        assertEquals(-1, zset.zrank("nonexistent")); // 不存在的元素

        // 测试按排名获取元素
        Entry<String, Integer> entry = zset.zmemberByRank(0);
        assertNotNull(entry);
        assertEquals("player1", entry.getKey());
        assertEquals(Integer.valueOf(100), entry.getValue());

        assertNull(zset.zmemberByRank(-1)); // 无效排名
        assertNull(zset.zmemberByRank(10)); // 超出范围的排名
    }

    @Test
    void testRangeByRank() {
        // 测试按排名范围查询
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);
        zset.zadd("player4", 300);
        zset.zadd("player5", 50);

        // 正常范围查询
        List<Entry<String, Integer>> result = zset.zrangeByRank(1, 3);
        assertEquals(3, result.size());
        assertEquals("player5", result.get(0).getKey()); // rank 1: score 100
        assertEquals("player1", result.get(1).getKey()); // rank 2: score 150
        assertEquals("player3", result.get(2).getKey()); // rank 3: score 200

        // 负数索引测试
        List<Entry<String, Integer>> result2 = zset.zrangeByRank(-3, -1);
        assertEquals(3, result2.size());

        // 边界情况
        assertTrue(zset.zrangeByRank(10, 20).isEmpty()); // 超出范围
        assertTrue(zset.zrangeByRank(3, 1).isEmpty()); // 起始大于结束
    }

    @Test
    void testRangeByScore() {
        // 测试按分数范围查询
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);
        zset.zadd("player4", 300);
        zset.zadd("player5", 50);

        // 查询分数范围 [100, 200]
        List<Entry<String, Integer>> result = zset.zrangeByScore(100, 200);
        assertEquals(3, result.size());
        
        Set<String> expectedMembers = Set.of("player1", "player3", "player2");
        Set<String> actualMembers = new HashSet<>();
        for (Entry<String, Integer> entry : result) {
            actualMembers.add(entry.getKey());
        }
        assertEquals(expectedMembers, actualMembers);

        // 查询单个分数
        List<Entry<String, Integer>> singleResult = zset.zrangeByScore(150, 150);
        assertEquals(1, singleResult.size());
        assertEquals("player3", singleResult.get(0).getKey());

        // 查询不存在的范围
        assertTrue(zset.zrangeByScore(1000, 2000).isEmpty());
    }

    @Test
    void testCountByScore() {
        // 测试统计分数范围内元素数量
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);
        zset.zadd("player4", 300);
        zset.zadd("player5", 50);

        assertEquals(3, zset.zcount(100, 200)); // 包含 100, 150, 200
        assertEquals(1, zset.zcount(150, 150)); // 只包含 150
        assertEquals(0, zset.zcount(1000, 2000)); // 无匹配
        assertEquals(5, zset.zcount(0, 1000)); // 全部包含
    }

    @Test
    void testRemoveOperations() {
        // 测试删除操作
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);

        // 删除单个元素
        Integer removedScore = zset.zrem("player2");
        assertEquals(Integer.valueOf(200), removedScore);
        assertNull(zset.zrem("player2")); // 再次删除应该返回null
        assertEquals(2, zset.zcard());

        // 按排名删除
        Entry<String, Integer> removedEntry = zset.zremByRank(1); // 删除排名第一的
        assertNotNull(removedEntry);
        assertEquals("player1", removedEntry.getKey()); // 应该是分数150的player3
        assertEquals(1, zset.zcard());

        // 删除不存在的元素
        assertNull(zset.zrem("nonexistent"));
    }

    @Test
    void testRemoveRangeByRank() {
        // 测试按排名范围删除
        for (int i = 1; i <= 10; i++) {
            zset.zadd("player" + i, i * 10);
        }

        // 删除排名1-3的元素
        int removedCount = zset.zremrangeByRank(1, 3);
        assertEquals(3, removedCount);
        assertEquals(7, zset.zcard());

        // 负数索引测试
        int removedCount2 = zset.zremrangeByRank(-2, -1);
        assertEquals(2, removedCount2);
        assertEquals(5, zset.zcard());

        zset.getSkipList().printDetail();
    }

    @Test
    void testRemoveRangeByScore() {
        // 测试按分数范围删除
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);
        zset.zadd("player4", 300);
        zset.zadd("player5", 50);

        // 删除分数在[100, 200]范围内的元素
        int removedCount = zset.zremrangeByScore(100, 200);
        assertEquals(3, removedCount); // player1(100), player3(150), player2(200)
        assertEquals(2, zset.zcard()); // 剩下player5(50)和player4(300)

        // 删除剩余元素
        int removedCount2 = zset.zremrangeByScore(0, 1000);
        assertEquals(2, removedCount2);
        assertEquals(0, zset.zcard());
    }

    @Test
    void testPopOperations() {
        // 测试弹出操作
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);

        // 弹出第一名（最低分）
        Entry<String, Integer> first = zset.zpopFirst();
        assertNotNull(first);
        assertEquals("player1", first.getKey());
        assertEquals(Integer.valueOf(100), first.getValue());

        // 弹出最后一名（最高分）
        Entry<String, Integer> last = zset.zpopLast();
        assertNotNull(last);
        assertEquals("player2", last.getKey());
        assertEquals(Integer.valueOf(200), last.getValue());

        assertEquals(1, zset.zcard());
    }

    @Test
    void testLimitOperation() {
        // 测试限制数量操作
        for (int i = 1; i <= 10; i++) {
            zset.zadd("player" + i, i * 10);
        }

        // 限制为前5名
        int removedCount = zset.zlimit(5);
        assertEquals(5, removedCount); // 删除了5个元素
        assertEquals(5, zset.zcard());

        // 再次限制，数量已经足够，不应该删除任何元素
        int removedCount2 = zset.zlimit(5);
        assertEquals(0, removedCount2);
    }

    @Test
    void testIterator() {
        // 测试迭代器功能
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);
        zset.zadd("player3", 150);

        List<Entry<String, Integer>> entries = new ArrayList<>();
        Iterator<Entry<String, Integer>> iterator = zset.scan();
        
        while (iterator.hasNext()) {
            entries.add(iterator.next());
        }

        assertEquals(3, entries.size());
        
        // 验证排序正确性（按分数升序）
        assertTrue(entries.get(0).getValue() <= entries.get(1).getValue());
        assertTrue(entries.get(1).getValue() <= entries.get(2).getValue());

        // 测试带偏移的迭代
        Iterator<Entry<String, Integer>> iteratorWithOffset = zset.scan(1);
        List<Entry<String, Integer>> offsetEntries = new ArrayList<>();
        while (iteratorWithOffset.hasNext()) {
            offsetEntries.add(iteratorWithOffset.next());
        }
        assertEquals(2, offsetEntries.size()); // 跳过第一个元素
    }

    @Test
    void testConcurrentModification() {
        // 测试并发修改异常
        zset.zadd("player1", 100);
        zset.zadd("player2", 200);

        Iterator<Entry<String, Integer>> iterator = zset.scan();
        iterator.next();
        
        // 在迭代过程中修改集合
        zset.zadd("player3", 150);
        
        // 下一次调用应该抛出ConcurrentModificationException
        assertThrows(ConcurrentModificationException.class, iterator::next);
    }

    @Test
    void testDoubleScoreZSet() {
        // 测试双精度浮点数分数
        doubleZSet.zadd("item1", 1.5);
        doubleZSet.zadd("item2", 2.7);
        doubleZSet.zadd("item3", 1.8);

        assertEquals(Double.valueOf(1.5), doubleZSet.zscore("item1"));
        assertEquals(3, doubleZSet.zcard());

        List<Entry<String, Double>> result = doubleZSet.zrangeByScore(1.0, 2.0);
        assertEquals(2, result.size());
    }

    @Test
    void testLargeDataSet() {
        // 测试大数据集性能
        int size = 10000;
        for (int i = 0; i < size; i++) {
            zset.zadd("player" + i, ThreadLocalRandom.current().nextInt(100000));
        }

        assertEquals(size, zset.zcard());
        
        // 测试随机查询性能
        for (int i = 0; i < 100; i++) {
            String randomKey = "player" + ThreadLocalRandom.current().nextInt(size);
            assertNotNull(zset.zscore(randomKey));
        }
    }

    @Test
    void testEdgeCases() {
        // 测试边界情况
        
        // 空集合操作
        assertEquals(0, zset.zcard());
        assertNull(zset.zscore("any"));
        assertEquals(-1, zset.zrank("any"));
        assertNull(zset.zmemberByRank(1));
        assertTrue(zset.zrangeByRank(1, 10).isEmpty());
        assertEquals(0, zset.zcount(0, 100));

        // 单元素集合
        zset.zadd("single", 100);
        assertEquals(1, zset.zcard());
        assertEquals(Integer.valueOf(100), zset.zscore("single"));
        assertEquals(1, zset.zrank("single"));
        
        Entry<String, Integer> singleEntry = zset.zmemberByRank(1);
        assertEquals("single", singleEntry.getKey());
        assertEquals(Integer.valueOf(100), singleEntry.getValue());
    }

    @Test
    void testDuplicateScores() {
        // 测试相同分数的情况
        zset.zadd("player_a", 100);
        zset.zadd("player_b", 100);
        zset.zadd("player_c", 100);

        assertEquals(3, zset.zcard());
        assertEquals(3, zset.zcount(100, 100));

        List<Entry<String, Integer>> result = zset.zrangeByScore(100, 100);
        assertEquals(3, result.size());

        // 相同分数时按member排序
        List<String> members = new ArrayList<>();
        for (Entry<String, Integer> entry : result) {
            members.add(entry.getKey());
        }
        Collections.sort(members);
        assertEquals(Arrays.asList("player_a", "player_b", "player_c"), members);
    }
}