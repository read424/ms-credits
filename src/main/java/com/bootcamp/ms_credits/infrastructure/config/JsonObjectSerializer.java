package com.bootcamp.ms_credits.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonObjectSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper;

    public JsonObjectSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            log.error("Error serializing data to JSON for topic: {}", topic, e);
            throw new RuntimeException("Error serializing object to JSON", e);
        }
    }
}
