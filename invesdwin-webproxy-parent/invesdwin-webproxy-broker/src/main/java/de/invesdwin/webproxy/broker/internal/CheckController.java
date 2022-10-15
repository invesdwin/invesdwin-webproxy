package de.invesdwin.webproxy.broker.internal;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import de.invesdwin.util.collections.Arrays;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.webproxy.broker.contract.CheckClient;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;

@Immutable
@Controller
@RequestMapping("/check")
public class CheckController {

    /**
     * REMOTE_ADDR is not good in my opinion, because if it is always there, we wouldn't find INVISIBLE proxies anymore.
     */
    private static final Set<String> PROXY_CHECK_HEADERS = new HashSet<String>(
            Arrays.asList("FORWARDED", "FORWARDED_FOR", "FORWARDED_FOR_IP", "CLIENT_IP", "HTTP_CLIENT_IP",
                    "HTTP_FORWARDED ", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED_FOR_IP", "HTTP_PROXY_CONNECTION", "VIA",
                    "HTTP_VIA", "HTTP_X_FORWARDED", "HTTP_X_FORWARDED_FOR", "X-FORWARDED-FOR", "MT-PROXY-ID",
                    "X-PROXY-ID", "HTTP_CLIENT_IP", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_PROXY_USER"));

    @RequestMapping(CheckClient.PROXY_QUALITY)
    public void proxyquality(final HttpServletRequest request, final HttpServletResponse response,
            @PathVariable(CheckClient.CLIENT_IP) final String clientip) throws IOException {

        ProxyQuality quality = null;
        if (!request.getRemoteAddr().equals(clientip)) {
            quality = ProxyQuality.INVISIBLE;
        }
        final Enumeration<String> headerNames = request.getHeaderNames();
        final StringBuilder debug = new StringBuilder();
        int headerCount = 0;
        final String trimmedClientIp = clientip.trim();
        while (headerNames.hasMoreElements()) {
            headerCount++;
            final String headerName = headerNames.nextElement();
            final String headerValue = request.getHeader(headerName);
            debug.append(headerCount);
            debug.append(": [");
            debug.append(headerName);
            debug.append("] = [");
            debug.append(headerValue);
            debug.append("]\n");
            if (trimmedClientIp.equals(Strings.trim(headerValue))) {
                if ("REMOTE_ADDR".equalsIgnoreCase(headerName)) {
                    quality = null;
                    break;
                } else {
                    quality = ProxyQuality.TRANSPARENT;
                    break;
                }
            }
            if (PROXY_CHECK_HEADERS.contains(Strings.upperCase(headerName)) && Strings.isNotBlank(headerValue)) {
                quality = ProxyQuality.ANONYMOUS;
            }
        }

        ProxyQualityLog.writeLog(clientip, debug.toString(), quality);

        //null means no proxy
        response.getOutputStream().print(String.valueOf(quality));
    }

    @RequestMapping(CheckClient.CLIENT_IP)
    public void clientip(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.getOutputStream().print(request.getRemoteAddr());
    }

}
