package com.rbkmoney.proxy.mocketbank.servlet;

import com.rbkmoney.damsel.p2p_adapter.P2PAdapterSrv;
import com.rbkmoney.proxy.mocketbank.decorator.P2pServerHandlerLog;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@RequiredArgsConstructor
@WebServlet("/proxy/mocketbank/p2p")
public class AdapterServletP2p extends GenericServlet {

    private final transient P2pServerHandlerLog handler;
    private transient Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(P2PAdapterSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}
