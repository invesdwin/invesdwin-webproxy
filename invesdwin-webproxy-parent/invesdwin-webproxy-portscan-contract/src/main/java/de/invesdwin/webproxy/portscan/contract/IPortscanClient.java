package de.invesdwin.webproxy.portscan.contract;

import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;

/**
 * This interface must be implemented by client code so that the response messages can be received.
 * 
 * @author subes
 * 
 */
public interface IPortscanClient {

    void hostIsReachable(PingResponse response);

    void portIsReachable(ScanResponse response);

}
