package com.rbkmoney.proxy.mocketbank.servlet;

import com.rbkmoney.damsel.withdrawals.provider_adapter.AdapterSrv;
import com.rbkmoney.proxy.mocketbank.handler.p2p.P2PCreditServerHandler;
import com.rbkmoney.woody.api.event.CompositeServiceEventListener;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import com.rbkmoney.woody.thrift.impl.http.event.HttpServiceEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventLogListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * @author Anatoly Cherkasov
 */
@WebServlet("/proxy/mocketbank/p2p-credit")
public class ProxyServletP2PCredit extends GenericServlet {

    @Autowired
    private P2PCreditServerHandler handler;

    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        THServiceBuilder builder = new THServiceBuilder();
        builder.withEventListener(new CompositeServiceEventListener<>(
                new ServiceEventLogListener(),
                new HttpServiceEventLogListener()
        ));
        servlet = builder.build(AdapterSrv.Iface.class, handler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}
