package de.invesdwin.webproxy.crawler.verification;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import jakarta.inject.Named;

@Named
@Immutable
public class TaskAcquirerStub extends StubSupport {

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) throws Exception {
        ctx.deactivateBean(TaskAcquirer.class);
    }

}
