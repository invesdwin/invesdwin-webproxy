package de.invesdwin.webproxy.portscan.contract;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;

@Named
@ThreadSafe
public class PortscanClientStub extends StubSupport implements IPortscanClient {

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) {
        ctx.replaceBean(IPortscanClient.class, PortscanClientStub.class);
    }

    @Override
    public void hostIsReachable(final PingResponse response) {}

    @Override
    public void portIsReachable(final ScanResponse response) {}

}
