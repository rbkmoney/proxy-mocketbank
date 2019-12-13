package com.rbkmoney.proxy.mocketbank.utils.mocketbank;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MockMpiUtils {

    public static String prepareUrl(String url, String path) {
        return String.format("%s/mpi/%s", url, path);
    }

    public static String getCallbackUrl(String callbackUrl, String path) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .path(path)
                .build()
                .toUriString();
    }

}
