package com.rbkmoney.proxy.mocketbank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.adapter.helpers.hellgate.HellgateAdapterClient;
import com.rbkmoney.adapter.helpers.hellgate.exception.HellgateException;
import com.rbkmoney.damsel.p2p_adapter.Callback;
import com.rbkmoney.damsel.p2p_adapter.ProcessCallbackResult;
import com.rbkmoney.fistful.client.FistfulClient;
import com.rbkmoney.java.damsel.converter.CommonConverter;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import com.rbkmoney.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.CRes;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.ThreeDSMethodData;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import io.micrometer.shaded.io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/${server.rest.endpoint}")
public class MocketBankController {

    private final HellgateAdapterClient hellgateClient;
    private final FistfulClient fistfulClient;
    private final AdapterMockBankProperties mockBankProperties;
    private final ObjectMapper objectMapper;

    @RequestMapping(value = "term_url", method = RequestMethod.POST)
    public String receiveIncomingParameters(
            HttpServletRequest request,
            HttpServletResponse servletResponse) throws IOException {
        String tag = getTag(request);
        log.info("ReceivePaymentIncomingParameters with tag {}, info {}", tag, httpServletRequestToString(request));
        String resp = StringUtil.EMPTY_STRING;
        try {
            ByteBuffer callback = prepareCallbackParams(request);
            ByteBuffer response = hellgateClient.processPaymentCallback(tag, callback);
            resp = new String(response.array(), StandardCharsets.UTF_8);
        } catch (HellgateException e) {
            log.warn("Failed handle callback for payment", e);
        } catch (Exception e) {
            log.error("Failed handle callback for payment", e);
        }
        sendRedirect(request, servletResponse);
        return resp;
    }

    @RequestMapping(value = "/rec_term_url", method = RequestMethod.POST)
    public String receiveRecurrentIncomingParameters(
            HttpServletRequest request,
            HttpServletResponse servletResponse) throws IOException {
        String tag = getTag(request);
        log.info("ReceiveRecurrentIncomingParameters with tag {}, info {}", tag, httpServletRequestToString(request));
        String resp = StringUtil.EMPTY_STRING;
        try {
            ByteBuffer callback = prepareCallbackParams(request);
            ByteBuffer response = hellgateClient.processRecurrentTokenCallback(tag, callback);
            resp = new String(response.array(), StandardCharsets.UTF_8);
        } catch (HellgateException e) {
            log.warn("Failed handle callback for recurrent", e);
        } catch (Exception e) {
            log.error("Failed handle callback for recurrent", e);
        }
        sendRedirect(request, servletResponse);
        return resp;
    }

    @RequestMapping(value = "three_ds_method_notification", method = RequestMethod.POST)
    public String mpi20ThreeDsMethodNotification(HttpServletRequest servletRequest,
                                                 HttpServletResponse servletResponse) throws IOException {
        log.info("three_ds_method_notification {}", httpServletRequestToString(servletRequest));
        ThreeDSMethodData threeDSMethodData =
                objectMapper.readValue(servletRequest.getParameter(
                        CallbackResponseFields.THREE_DS_METHOD_DATA),
                        ThreeDSMethodData.class);
        String tag = SuspendPrefix.PAYMENT.getPrefix() + threeDSMethodData.getThreeDSServerTransID();
        ByteBuffer callback = prepareCallbackParams(servletRequest);
        String response = StringUtil.EMPTY_STRING;
        try {
            ByteBuffer callbackResponse = hellgateClient.processPaymentCallback(tag, callback);
            response = new String(callbackResponse.array(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed handle mpi20 three_ds_method_notification", e);
        }
        return response;
    }

    @RequestMapping(value = "acs_notification", method = RequestMethod.POST)
    public String mpi20AcsNotification(HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) throws IOException {
        log.info("mpi20 acs_notification {}", httpServletRequestToString(servletRequest));
        CRes challengeRes = objectMapper.readValue(
                servletRequest.getParameter(CallbackResponseFields.CRES), CRes.class);
        String tag = SuspendPrefix.PAYMENT.getPrefix() + challengeRes.getThreeDSServerTransID();
        ByteBuffer callback = prepareCallbackParams(servletRequest);
        String response = "";
        try {
            ByteBuffer callbackResponse = hellgateClient.processPaymentCallback(tag, callback);
            response = new String(callbackResponse.array(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed handle mpi20 acs_notification", e);
        }
        sendRedirect(servletRequest, servletResponse);
        return response;
    }

    @RequestMapping(value = "/p2p", method = RequestMethod.POST)
    public String receiveP2pIncomingParameters(HttpServletRequest request,
                                               HttpServletResponse servletResponse) throws IOException {
        String tag = getTag(request);
        log.info("receiveP2pIncomingParameters with tag {}, info {}", tag, httpServletRequestToString(request));
        String resp = StringUtil.EMPTY_STRING;
        try {
            ByteBuffer callbackParams = prepareCallbackParams(request);
            Callback callback = new Callback();
            callback.setTag(tag);
            callback.setPayload(callbackParams);
            ProcessCallbackResult result = fistfulClient.processCallback(callback);
            log.info("P2P Callback Result {}", result);
        } catch (HellgateException e) {
            log.warn("Failed handle callback for p2p", e);
        } catch (Exception e) {
            log.error("Failed handle callback for p2p", e);
        }
        sendRedirect(request, servletResponse);
        return resp;
    }

    @RequestMapping(value = "/qps", method = RequestMethod.GET)
    public String receiveQpsIncomingParameters(
            HttpServletRequest request,
            HttpServletResponse servletResponse) throws IOException {
        log.info("receiveQpsIncomingParameters with info {}", httpServletRequestToString(request));
        servletResponse.sendRedirect(mockBankProperties.getFinishInteraction());
        return StringUtil.EMPTY_STRING;
    }

    @RequestMapping(value = "/dw", method = RequestMethod.POST)
    public String receiveDwIncomingParameters(
            HttpServletRequest request,
            HttpServletResponse servletResponse) throws IOException {
        log.info("receiveDWIncomingParameters with info {}", httpServletRequestToString(request));
        servletResponse.sendRedirect(mockBankProperties.getFinishInteraction());
        return StringUtil.EMPTY_STRING;
    }

    private String getTag(HttpServletRequest request) {
        if (StringUtils.hasText(request.getParameter("MD"))) {
            return request.getParameter("MD");
        } else {
            log.warn("Missing a required parameter 'MD' ");
        }
        return StringUtil.EMPTY_STRING;
    }

    private ByteBuffer prepareCallbackParams(HttpServletRequest request) throws JsonProcessingException {
        Map<String, String> requestParams = CommonConverter.mapArrayToMap(request.getParameterMap());
        return CommonConverter.mapToByteBuffer(requestParams);
    }

    private void sendRedirect(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {
        if (StringUtils.hasText(request.getParameter("termination_uri"))) {
            servletResponse.sendRedirect(request.getParameter("termination_uri"));
        }
    }

    private String httpServletRequestToString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request Method = [").append(request.getMethod()).append("], ");
        sb.append("Request URL Path = [").append(request.getRequestURL()).append("], ");

        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> headerName + " : " + Collections.list(request.getHeaders(headerName)))
                .collect(Collectors.joining(", "));

        if (headers.isEmpty()) {
            sb.append("Request headers: NONE,");
        } else {
            sb.append("Request headers: [").append(headers).append("],");
        }

        String parameters = Collections.list(request.getParameterNames())
                .stream()
                .map(p -> p + " : " + Arrays.asList(request.getParameterValues(p)))
                .collect(Collectors.joining(", "));

        if (parameters.isEmpty()) {
            sb.append("Request parameters: NONE.");
        } else {
            sb.append("Request parameters: [").append(parameters).append("].");
        }

        return sb.toString();
    }

}
