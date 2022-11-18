package de.invesdwin.webproxy.crawler.verification;

import javax.annotation.concurrent.Immutable;
import jakarta.inject.Named;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;

@Named
@Immutable
public class TaskAcquirerStub extends StubSupport {

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) throws Exception {
        super.setUpContext(test, ctx);
        ctx.deactivateBean(TaskAcquirer.class);
    }

}
