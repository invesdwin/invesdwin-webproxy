package de.invesdwin.webproxy.crawler;

import javax.annotation.concurrent.Immutable;

import org.kohsuke.args4j.CmdLineParser;

import de.invesdwin.context.beans.init.AMain;

@Immutable
public class Main extends AMain {

    public Main(final String[] args) {
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
