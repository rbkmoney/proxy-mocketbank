package com.rbkmoney.proxy.mocketbank.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class ConverterTest {

    @Test
    public void testMe() {
        String tag = SuspendPrefix.RECURRENT + "1112";
        assertTrue(tag.startsWith(SuspendPrefix.RECURRENT.getPrefix()));

        tag = SuspendPrefix.PAYMENT.getPrefix() + "1112";
        assertTrue(tag.startsWith(SuspendPrefix.PAYMENT.getPrefix()));
    }

    @Test
    public void byteBufferToMap() throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        ByteBuffer byteBuffer = ByteBuffer.wrap(new ObjectMapper().writeValueAsString(map).getBytes());
        Map<String, String> mapExpected = Converter.byteBufferToMap(byteBuffer);

        assertEquals(map, mapExpected);
        assertEquals("value1", mapExpected.get("key1"));
    }

    @Test
    public void mapToByteBuffer() throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        ByteBuffer expected = ByteBuffer.wrap(new ObjectMapper().writeValueAsString(map).getBytes());

        assertEquals(expected, Converter.mapToByteBuffer(map));
    }

    @Test
    public void testMapArrayToMap() {

        Map<String, String[]> mapArray = new HashMap<>();
        mapArray.put("key1", new String[]{"value1"});
        mapArray.put("key2", new String[]{"value2"});

        Map<String, String> expected = new HashMap<>();
        expected.put("key1", "value1");
        expected.put("key2", "value2");

        Map<String, String> actual = Converter.mapArrayToMap(mapArray);

        assertEquals(expected, actual);
    }

    @Test
    public void testMapObject() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        String json = objectMapper.writeValueAsString(map);
        byte[] bytes = json.getBytes();

        log.info(json);
        log.info(bytes.toString());
        log.info(new String(bytes));

        ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());

        Map<String, Object> mapObject = objectMapper.readValue(new String(buffer.array(), "UTF-8"),
                new TypeReference<Map<String, Object>>() {
                });

        log.info("Expected mapObject key1={}", mapObject.get("key1"));
        assertEquals("value1", mapObject.get("key1"));
    }

}
