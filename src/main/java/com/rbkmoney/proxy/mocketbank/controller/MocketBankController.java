package com.rbkmoney.proxy.mocketbank.controller;

import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.hellgate.HellGateApi;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankTag;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

@RestController
@RequestMapping(value = "/mocketbank")
public class MocketBankController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MocketBankController.class);

    @Autowired
    private HellGateApi hellGateApi;

    @RequestMapping(value = "term_url", method = RequestMethod.POST)
    public String receiveIncomingParameters(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {

        LOGGER.info("Input params: {}", request.getParameterMap());

        String tag = "";
        ByteBuffer callback = null;
        String resp = "";

        try {
            callback = Converter.mapToByteBuffer(Converter.mapArrayToMap(request.getParameterMap()));
        } catch (IOException e) {
            LOGGER.warn("Exception Map to ByteBuffer in processCallback", e);
        }

        if (StringUtils.hasText(request.getParameter("MD"))) {
            tag = request.getParameter("MD");
        } else {
            LOGGER.warn("Missing a required parameter 'MD' ");
        }

        // Узнать рекурент или нет, после чего вызвать тот или иной метод
        try {

            ByteBuffer response;
            if (tag.startsWith(MocketBankTag.RECURRENT_SUSPEND_TAG)) {
                response = hellGateApi.processRecurrentTokenCallback(tag, callback);
            } else {
                response = hellGateApi.processPaymentCallback(tag, callback);
            }

            resp = new String(response.array(), "UTF-8");
        } catch (TException | UnsupportedEncodingException e) {
            LOGGER.error("Exception in processCallback", e);
        }

        if (StringUtils.hasText(request.getParameter("termination_uri")))
            servletResponse.sendRedirect(request.getParameter("termination_uri"));

        return resp;
    }

}
