package cn.chauncy.logic.hero.attr;

import cn.chauncy.logic.hero.attr.reducer.*;
import cn.chauncy.logic.hero.attr.type.AttrType;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 属性集合计算器
 * 可指代一个系统的属性集合
 */
public class AttrReducer {

    private static final Logger logger = LoggerFactory.getLogger(AttrReducer.class);

    private final Int2ObjectMap<ValueReducer> attrs = new Int2ObjectArrayMap<>();
    public boolean debugFlag = false;

    public boolean contains(int attrType) {
        return attrs.containsKey(attrType);
    }

    public int getAttr(int attrType) {
        ValueReducer reducer = attrs.get(attrType);
        return reducer == null ? 0 : reducer.getValue();
    }

    public int remove(int attrType) {
        ValueReducer remove = attrs.remove(attrType);
        return remove == null ? 0 : remove.getValue();
    }

    public boolean isEmpty() {
        return attrs.isEmpty();
    }

    public int size() {
        return attrs.size();
    }

    public void clear() {
        attrs.clear();
    }

    public void merge(int attrType, int value) {
        if (value == 0) {
            return;
        }
        ValueReducer valueReducer = computeIfAbsent(attrType);
        valueReducer.merge(value);
        if (debugFlag) {
            logger.debug("merge attr: {}, value: {}", attrType, value);
        }
    }

    public void unmerge(int attrType, int value) {
        if (value == 0) {
            return;
        }
        ValueReducer valueReducer = computeIfAbsent(attrType);
        valueReducer.unmerge(value);
        if (debugFlag) {
            logger.debug("unmerge attr: {}, value: {}", attrType, value);
        }
    }

    public void merge(Int2IntMap attrMap) {
        if (attrMap.isEmpty()) {
            return;
        }
        ObjectIterator<Int2IntMap.Entry> iterator = Int2IntMaps.fastIterator(attrMap);
        while (iterator.hasNext()) {
            Int2IntMap.Entry entry = iterator.next();
            merge(entry.getIntKey(), entry.getIntValue());
            if (debugFlag) {
                logger.debug("merge attr map key: {}, value: {}", entry.getIntKey(), entry.getIntValue());
            }
        }
    }

    public void mergeMul(Int2IntMap attrMap, float factor) {
        if (attrMap.isEmpty()) {
            return;
        }
        ObjectIterator<Int2IntMap.Entry> iterator = Int2IntMaps.fastIterator(attrMap);
        while (iterator.hasNext()) {
            Int2IntMap.Entry entry = iterator.next();
            int value = (int) (entry.getIntValue() * factor);
            merge(entry.getIntKey(), value);
            if (debugFlag) {
                logger.debug("merge attr mul map key: {}, value: {}", entry.getIntKey(), value);
            }
        }
    }

    public void mergeOther(AttrReducer other) {
        ObjectIterator<Int2ObjectMap.Entry<ValueReducer>> iterator = Int2ObjectMaps.fastIterator(other.attrs);
        while (iterator.hasNext()) {
            Int2ObjectMap.Entry<ValueReducer> entry = iterator.next();
            int attrType = entry.getIntKey();

            ValueReducer valueReducer = computeIfAbsent(attrType);
            int value = entry.getValue().getValue();
            valueReducer.merge(value);
            if (debugFlag) {
                logger.debug("merge attr other key: {}, value: {}", entry.getIntKey(), value);
            }
        }
    }

    public void unmergeOther(AttrReducer other) {
        ObjectIterator<Int2ObjectMap.Entry<ValueReducer>> iterator = Int2ObjectMaps.fastIterator(other.attrs);
        while (iterator.hasNext()) {
            Int2ObjectMap.Entry<ValueReducer> entry = iterator.next();
            int attrType = entry.getIntKey();

            ValueReducer valueReducer = computeIfAbsent(attrType);
            valueReducer.unmerge(entry.getValue().getValue());
            if (debugFlag) {
                logger.debug("unmerge attr other key: {}, value: {}", entry.getIntKey(), entry.getValue().getValue());
            }
        }
    }

    public void unmerge(Int2IntMap attrMap) {
        if (attrMap.isEmpty()) {
            return;
        }
        ObjectIterator<Int2IntMap.Entry> iterator = Int2IntMaps.fastIterator(attrMap);
        while (iterator.hasNext()) {
            Int2IntMap.Entry entry = iterator.next();
            unmerge(entry.getIntKey(), entry.getIntValue());
            if (debugFlag) {
                logger.debug("unmerge attr map key: {}, value: {}", entry.getIntKey(), entry.getIntValue());
            }
        }
    }

    private ValueReducer computeIfAbsent(int attrType) {
        ValueReducer valueReducer = attrs.get(attrType);
        if (valueReducer == null) {
            valueReducer = createValueReducer(attrType);
            attrs.put(attrType, valueReducer);
        }
        return valueReducer;
    }

    private ValueReducer createValueReducer(int attrType) {
        AttrType attr = AttrType.valueOf(attrType);
        if (attr == null) {
            throw new IllegalArgumentException("Invalid attr type: " + attrType);
        }
        return switch (attr.mergeType) {
            case SUM -> new AddValueReducer();
            case COVER -> new CoverValueReducer();
            case MAX -> new MaxValueReducer();
            case MIN -> new MinValueReducer();
            case MUL -> new MultiplyValueReducer();
        };
    }

}

