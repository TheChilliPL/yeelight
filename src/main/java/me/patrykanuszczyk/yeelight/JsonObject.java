package me.patrykanuszczyk.yeelight;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface JsonObject {
    default String toJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}
