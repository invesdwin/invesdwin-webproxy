package de.invesdwin.webproxy.portscan.contract;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;
import jakarta.inject.Named;

@Named
@ThreadSafe
public class PortscanClientStub extends StubSupport implements IPortscanClient {

    @Override
    public void setUpContext(final ATest test, final ITestContextSetup ctx) {
        ctx.replaceBean(IPortscanClient.class, PortscanClientStub.class);
    }

    @Override
    public void hostIsReachable(final PingResponse response) {}

    @Override
    public void portIsReachable(final ScanResponse response) {}

}
