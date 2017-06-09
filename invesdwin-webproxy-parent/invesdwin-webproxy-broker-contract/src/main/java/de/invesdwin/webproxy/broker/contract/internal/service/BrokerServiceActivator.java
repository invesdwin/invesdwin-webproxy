package de.invesdwin.webproxy.broker.contract.internal.service;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.springframework.integration.annotation.ServiceActivator;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.AddToBeVerifiedProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.ProcessResultFromCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.RetryLaterExceptionResponse;

@ThreadSafe
public class BrokerServiceActivator implements IBrokerServiceEndpoint {

    @Inject
    private IBrokerService service;

    @ServiceActivator
    @Override
    public BrokerResponse request(final BrokerRequest request) {
        final BrokerResponse response = new BrokerResponse();
        try {
            if (request.getGetWorkingProxiesRequest() != null) {
                response.setGetWorkingProxiesResponse(service.getWorkingProxies());
            } else if (request.getAddToBeVerifiedProxiesRequest() != null) {
                service.addToBeVerifiedProxies(request.getAddToBeVerifiedProxiesRequest());
                response.setAddToBeVerifiedProxiesResponse(new AddToBeVerifiedProxiesResponse());
            } else if (request.getGetTaskForCrawlerRequest() != null) {
                response.setGetTaskForCrawlerResponse(service.getTaskForCrawler());
            } else if (request.getProcessResultFromCrawlerRequest() != null) {
                service.processResultFromCrawler(request.getProcessResultFromCrawlerRequest());
                response.setProcessResultFromCrawlerResponse(new ProcessResultFromCrawlerResponse());
            }
        } catch (final RetryLaterException e) {
            final RetryLaterExceptionResponse excResponse = new RetryLaterExceptionResponse();
            excResponse.setMessage(e.getMessage());
            response.setRetryLaterExceptionResponse(excResponse);
        }
        return response;
    }

}
