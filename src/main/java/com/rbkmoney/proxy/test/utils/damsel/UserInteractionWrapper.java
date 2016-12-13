package com.rbkmoney.proxy.test.utils.damsel;

import com.rbkmoney.damsel.user_interaction.BrowserGetRequest;
import com.rbkmoney.damsel.user_interaction.BrowserHTTPRequest;
import com.rbkmoney.damsel.user_interaction.BrowserPostRequest;
import com.rbkmoney.damsel.user_interaction.UserInteraction;

import java.util.Map;

public class UserInteractionWrapper {

    public static UserInteraction makeUserInteraction(BrowserHTTPRequest browserHTTPRequest) {
        UserInteraction userInteraction = new UserInteraction();
        userInteraction.setRedirect(browserHTTPRequest);
        return userInteraction;
    }

    public static BrowserHTTPRequest makeBrowserGetRequest(String url) {
        BrowserGetRequest browserGetRequest = new BrowserGetRequest();
        browserGetRequest.setUri(url);
        BrowserHTTPRequest browserHTTPRequest = new BrowserHTTPRequest();
        browserHTTPRequest.setGetRequest(browserGetRequest);
        return browserHTTPRequest;
    }

    public static BrowserHTTPRequest makeBrowserPostRequest(String url, Map<String, String> form) {
        BrowserPostRequest browserPostRequest = new BrowserPostRequest();
        browserPostRequest.setUri(url);
        browserPostRequest.setForm(form);
        BrowserHTTPRequest browserHTTPRequest = new BrowserHTTPRequest();
        browserHTTPRequest.setPostRequest(browserPostRequest);
        return browserHTTPRequest;
    }

}
