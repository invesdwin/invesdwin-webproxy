package de.invesdwin.webproxy.broker.internal;

import javax.annotation.concurrent.Immutable;

/**
 * Interesting for websites that do not post ports or which post them as images instead of text. Once this page had it:
 * http://www.hidemyass.com/proxy-list/All-Countries/fast/show-planetlab/2/
 * 
 * The ports are sorted by frequentness descending, so that time is saved when iterating through all ports. First these
 * ports are checked, then the ports from the database.
 * 
 * This list is based on a dataset of 5k proxies.
 * 
 * @author subes
 * 
 */
@Immutable
enum OftenUsedProxyPorts {

    _8080,
    _80,
    _3128,
    _9415,
    _27977,
    _1080,
    _3124,
    _3127,
    _8123,
    _8000,
    _53,
    _808,
    _8088,
    _8090,
    _9090,
    _8888,
    _3389,
    _443,
    _8118,
    _8008,
    _2223,
    _8291,
    _8081,
    _25,
    _8001,
    _1025,
    _143,
    _1337,
    _81,
    _54321,
    _3129,
    _8085,
    _8086,
    _6588,
    _111,
    _82,
    _88,
    _3125,
    _9100,
    _1146,
    _1368,
    _1610,
    _3306,
    _1720,
    _3126,
    _9050,
    _42868,
    _9015,
    _1393,
    _8040;

    public int toPort() {
        return Integer.parseInt(toString().replace("_", ""));
    }

    public static OftenUsedProxyPorts valueOf(final int port) {
        return OftenUsedProxyPorts.valueOf("_" + port);
    }

}
