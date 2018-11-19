package com.rbkmoney.proxy.mocketbank.controller;

import com.rbkmoney.adapter.helpers.hellgate.HellgateAdapterClient;
import com.rbkmoney.adapter.helpers.hellgate.exception.HellgateException;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankTag;
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
import java.nio.ByteBuffer;

@RestController
@RequestMapping(value = "/mocketbank")
public class MocketBankController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HellgateAdapterClient hellgateClient;

    @RequestMapping(value = "term_url", method = RequestMethod.POST)
    public String receiveIncomingParameters(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {

        log.info("Input params: {}", request.getParameterMap());

        String tag = "";
        ByteBuffer callback = null;
        String resp = "";

        try {
            callback = Converter.mapToByteBuffer(Converter.mapArrayToMap(request.getParameterMap()));
        } catch (IOException e) {
            log.warn("Exception Map to ByteBuffer in processCallback", e);
        }

        if (StringUtils.hasText(request.getParameter("MD"))) {
            tag = request.getParameter("MD");
        } else {
            log.warn("Missing a required parameter 'MD' ");
        }

        // Узнать рекурент или нет, после чего вызвать тот или иной метод
        try {

            ByteBuffer response;
            if (tag.startsWith(MocketBankTag.RECURRENT_SUSPEND_TAG)) {
                response = hellgateClient.processRecurrentTokenCallback(tag, callback);
            } else {
                response = hellgateClient.processPaymentCallback(tag, callback);
            }

            resp = new String(response.array(), "UTF-8");
        } catch (HellgateException e) {
            log.warn("Exception in processPaymentCallback", e);
        } catch (Exception e) {
            log.error("Exception in processPaymentCallback", e);
        }

        if (StringUtils.hasText(request.getParameter("termination_uri")))
            servletResponse.sendRedirect(request.getParameter("termination_uri"));

        return resp;
    }

}
