package cn.chauncy.base;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutableListDeserializer extends JsonDeserializer<List<?>> {
    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        
        if (node instanceof ArrayNode arrayNode) {
            List<Object> list = new ArrayList<>();
            for (JsonNode element : arrayNode) {
                list.add(mapper.treeToValue(element, Object.class));
            }
            // 返回不可变集合
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }
}