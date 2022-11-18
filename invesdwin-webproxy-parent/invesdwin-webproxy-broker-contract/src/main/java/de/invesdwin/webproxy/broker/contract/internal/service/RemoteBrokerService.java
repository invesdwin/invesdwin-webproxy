package de.invesdwin.webproxy.broker.contract.internal.service;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.GetTaskForCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.GetWorkingProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import jakarta.inject.Inject;

@ThreadSafe
public class RemoteBrokerService implements IBrokerService {

    @Inject
    private IBrokerServiceEndpoint endpoint;

    @Override
    public GetWorkingProxiesResponse getWorkingProxies() throws RetryLaterException {
        final BrokerRequest r = new BrokerRequest();
        r.setGetWorkingProxiesRequest(new GetWorkingProxiesRequest());
        return requestWithExceptionHandling(r).getGetWorkingProxiesResponse();
    }

    @Override
    public void addToBeVerifiedProxies(final AddToBeVerifiedProxiesRequest request) {
        final BrokerRequest r = new BrokerRequest();
        r.setAddToBeVerifiedProxiesRequest(request);
        try {
            Assertions.assertThat(requestWithExceptionHandling(r).getAddToBeVerifiedProxiesResponse()).isNotNull();
        } catch (final RetryLaterException e) {
            throw newIllegalesFehlerHandlingException(e);
        }
    }

    @Override
    public GetTaskForCrawlerResponse getTaskForCrawler() {
        final BrokerRequest r = new BrokerRequest();
        r.setGetTaskForCrawlerRequest(new GetTaskForCrawlerRequest());
        try {
            return requestWithExceptionHandling(r).getGetTaskForCrawlerResponse();
        } catch (final RetryLaterException e) {
            throw newIllegalesFehlerHandlingException(e);
        }
    }

    @Override
    public void processResultFromCrawler(final ProcessResultFromCrawlerRequest request) {
        final BrokerRequest r = new BrokerRequest();
        r.setProcessResultFromCrawlerRequest(request);
        try {
            Assertions.assertThat(requestWithExceptionHandling(r).getProcessResultFromCrawlerResponse()).isNotNull();
        } catch (final RetryLaterException e) {
            throw newIllegalesFehlerHandlingException(e);
        }
    }

    private BrokerResponse requestWithExceptionHandling(final BrokerRequest request) throws RetryLaterException {
        final BrokerResponse response = endpoint.request(request);
        if (response.getRetryLaterExceptionResponse() != null) {
            throw new RetryLaterException(response.getRetryLaterExceptionResponse().getMessage());
        } else {
            return response;
        }
    }

    private IllegalStateException newIllegalesFehlerHandlingException(final RetryLaterException e) {
        return new IllegalStateException("Programming error! The interface does not support this exception here!", e);
    }

}
