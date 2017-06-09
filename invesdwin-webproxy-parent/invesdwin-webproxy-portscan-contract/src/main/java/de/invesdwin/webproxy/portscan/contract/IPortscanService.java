package de.invesdwin.webproxy.portscan.contract;

import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.ScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncResponse.StatusResponse;

/**
 * This is the specification of the portscanner. For example it checks via Stealth SYN Method if the given port is open
 * for connections. This might be interesting to find possible proxy hosts in the internet or to verify those. All
 * requests are processed asynchronously except for the status request.
 * 
 * @author subes
 * 
 */
public interface IPortscanService {

    void scan(ScanRequest request);

    void ping(PingRequest request);

    void randomScan(RandomScanRequest request);

    StatusResponse status();

}
