package de.invesdwin.webproxy.broker.contract;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;

public interface IBrokerService {

    GetWorkingProxiesResponse getWorkingProxies() throws RetryLaterException;

    /**
     * To be verified proxies may be proxies that have been returned as working, but did not really function properly.
     * In that case they can be submitted to be reverified.
     */
    void addToBeVerifiedProxies(AddToBeVerifiedProxiesRequest request);

    GetTaskForCrawlerResponse getTaskForCrawler();

    void processResultFromCrawler(ProcessResultFromCrawlerRequest request);

}