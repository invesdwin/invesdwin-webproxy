package de.invesdwin.webproxy.crawler.verification;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.context.test.stub.StubSupport;
import jakarta.inject.Named;

@Named
@Immutable
public class TaskAcquirerStub extends StubSupport {

    @Override
    public void setUpContext(final ATest test, final ITestContextSetup ctx) throws Exception {
        ctx.deactivateBean(TaskAcquirer.class);
    }

}
