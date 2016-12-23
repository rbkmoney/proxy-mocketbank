package com.rbkmoney.proxy.mocketbank.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Converter {

    public static Map<String, String> byteBufferToMap(ByteBuffer byteBuffer) throws IOException {
        return new ObjectMapper().readValue(
                new String(byteBuffer.array(), "UTF-8"),
                new TypeReference<Map<String, String>>() {
                }
        );
    }

    public static ByteBuffer mapToByteBuffer(Map<String, String> map) throws IOException {
        return ByteBuffer.wrap(new ObjectMapper().writeValueAsString(map).getBytes());
    }

    public static Map<String, String> mapArrayToMap(Map<String, String[]> map) {
        if (!Optional.ofNullable(map).isPresent()) {
            return new HashMap<>();
        }
        Map<String, String> newMap = new HashMap<>();
        map.forEach((K, V) -> newMap.put(K.trim(), V[0]));
        return newMap;
    }

    // Convert Map to byte array
    public static byte[] mapToByteArray(Map<String, String> map) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(map);
        return byteOut.toByteArray();
    }

    // Parse byte array to Map
    public static Map<String, String> byteArrayToMap(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return (Map<String, String>) in.readObject();
    }

}
