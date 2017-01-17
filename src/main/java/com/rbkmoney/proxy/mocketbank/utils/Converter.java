package com.rbkmoney.proxy.mocketbank.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Converter {

    public static Map byteBufferToMap(ByteBuffer byteBuffer) throws IOException {
        return  new ObjectMapper().readValue(new String(byteBuffer.array(), StandardCharsets.UTF_8), HashMap.class);
    }

    public static ByteBuffer mapToByteBuffer(Map<String, String> map) throws JsonProcessingException {
        return ByteBuffer.wrap(new ObjectMapper().writeValueAsString(map).getBytes());
    }

    public static Map<String, String> mapArrayToMap(Map<String, String[]> map) {
        if (map == null) {
            return new HashMap<>();
        }
        Map<String, String> newMap = new HashMap<>();
        map.forEach((K, V) -> newMap.put(K.trim(), V[0]));
        return newMap;
    }

    // Convert Map to byte array
    public static byte[] mapToByteArray(Map<String, String> map) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(map).getBytes();
    }

    // Parse byte array to Map
    public static Map byteArrayToMap(byte[] data) throws IOException {
        return new ObjectMapper().readValue(data, HashMap.class);
    }

}
