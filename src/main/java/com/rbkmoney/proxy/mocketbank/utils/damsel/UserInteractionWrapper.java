package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.user_interaction.BrowserGetRequest;
import com.rbkmoney.damsel.user_interaction.BrowserHTTPRequest;
import com.rbkmoney.damsel.user_interaction.BrowserPostRequest;
import com.rbkmoney.damsel.user_interaction.UserInteraction;

import java.util.Map;

public class UserInteractionWrapper {

    public static UserInteraction makePostUserInteraction(String url, Map<String, String> form) {
        return makeUserInteraction(makeBrowserPostRequest(url, form));
    }

    public static UserInteraction makeGetUserInteraction(String url) {
        return makeUserInteraction(makeBrowserGetRequest(url));
    }

    public static UserInteraction makeUserInteraction(BrowserHTTPRequest browserHTTPRequest) {
        return UserInteraction.redirect(browserHTTPRequest);
    }

    public static BrowserHTTPRequest makeBrowserPostRequest(String url, Map<String, String> form) {
        return BrowserHTTPRequest.post_request(new BrowserPostRequest(url, form));
    }

    public static BrowserHTTPRequest makeBrowserGetRequest(String url) {
        return BrowserHTTPRequest.get_request(new BrowserGetRequest(url));
    }

}
