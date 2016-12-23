package com.rbkmoney.proxy.mocketbank.controller;

import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.hellgate.HellGateApi;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Optional;

@RestController
@RequestMapping(value = "/mocketbank")
public class MocketBankController {

    private final static Logger LOGGER = LoggerFactory.getLogger(MocketBankController.class);

    @Autowired
    HellGateApi hellGateApi;

    @RequestMapping(value = "term_url", method = RequestMethod.POST)
    public String receiveIncomingParameters(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {

        LOGGER.info("Input params: {}", request.getParameterMap());

        String tag = "";
        ByteBuffer callback = null;
        String resp = "";

        try {
            callback = Converter.mapToByteBuffer(Converter.mapArrayToMap(request.getParameterMap()));
        } catch (IOException e) {
            LOGGER.error("Exception Map to ByteBuffer in processCallback", e);
        }

        if (Optional.ofNullable(request.getParameter("MD")).isPresent()) {
            tag = request.getParameter("MD");
        } else {
            LOGGER.error("Missing a required parameter 'MD' ");
        }

        try {
            ByteBuffer response = hellGateApi.processCallback(tag, callback);
            resp =  new String(response.array(), "UTF-8");
        } catch (TException | UnsupportedEncodingException e) {
            LOGGER.error("Exception in processCallback", e);
        }

        if (!request.getParameter("termination_uri").isEmpty())
            servletResponse.sendRedirect(request.getParameter("termination_uri"));

        return resp;
    }

}
