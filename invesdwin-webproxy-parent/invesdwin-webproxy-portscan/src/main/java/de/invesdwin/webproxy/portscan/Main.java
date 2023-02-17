package de.invesdwin.webproxy.portscan;

import javax.annotation.concurrent.ThreadSafe;

import org.kohsuke.args4j.CmdLineParser;

import de.invesdwin.context.beans.init.AMain;

@ThreadSafe
public final class Main extends AMain {

    private Main(final String[] args) {
        super(args, true);
    }

    public static void main(final String[] args) {
        new Main(args).run();
    }

    @Override
    protected void startApplication(final CmdLineParser parser) throws Exception {
        waitForShutdown();
    }

}
