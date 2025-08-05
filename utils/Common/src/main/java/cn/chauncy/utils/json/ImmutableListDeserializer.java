package cn.chauncy.utils.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutableListDeserializer extends JsonDeserializer<List<?>> implements ContextualDeserializer {
    private final JavaType contentType;

    public ImmutableListDeserializer() {
        this.contentType = null;
    }

    public ImmutableListDeserializer(JavaType contentType) {
        this.contentType = contentType;
    }

    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node instanceof ArrayNode arrayNode) {
            List<Object> list = new ArrayList<>();

            for (JsonNode element : arrayNode) {
                Object value;
                if (contentType != null && contentType.getRawClass() != Object.class) {
                    // 使用指定的元素类型进行反序列化
                    value = mapper.treeToValue(element, contentType.getRawClass());
                } else {
                    // 默认处理
                    value = mapper.treeToValue(element, Object.class);
                }
                list.add(value);
            }
            // 返回不可变集合
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            JavaType contextualType = property.getType();
            if (contextualType.isCollectionLikeType()) {
                JavaType contentType = contextualType.getContentType();
                return new ImmutableListDeserializer(contentType);
            }
        }
        return new ImmutableListDeserializer();
    }
}