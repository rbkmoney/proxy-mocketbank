package com.rbkmoney.proxy.test.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;


public class TestControllerTest {

    @Mock
    TestController controller;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void receiveIncomingParameters() throws Exception {
        Mockito.when(controller.receiveIncomingParameters(request, response)).thenReturn("");
        assertEquals("", controller.receiveIncomingParameters(request, response));
    }

}
