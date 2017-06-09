package de.invesdwin.webproxy.broker.contract;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class BrokerContractProperties {

    public static final int MAX_PROXIES_PER_TASK = 500;

    private BrokerContractProperties() {}

}
