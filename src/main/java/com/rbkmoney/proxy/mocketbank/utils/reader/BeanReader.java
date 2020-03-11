package com.rbkmoney.proxy.mocketbank.utils.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface BeanReader<R> {

    List<R> readList(InputStream is);

    default List<R> extractListFromFile(InputStream is, Function<String, R> func) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines().skip(1).map(func).collect(Collectors.toList());
    }

}
