# invesdwin-webproxy
A massively parallel download manager for web scraping that supports proxy servers. Developed as modules for the [invesdwin-context](https://github.com/subes/invesdwin-context) module system. 

## Maven

Releases and snapshots are deployed to this maven repository:
```
http://invesdwin.de/artifactory/invesdwin-oss-remote
```

Dependency declaration:
```xml
<dependency>
	<groupId>de.invesdwin</groupId>
	<artifactId>invesdwin-webproxy</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```
## Legal Discussion



## Architecture

The following context diagram shows what components this project consists of:

<p align="center"><img src="https://github.com/subes/invesdwin-webproxy/raw/master/invesdwin-webproxy-parent/invesdwin-webproxy/doc/webproxy_context.png" alt="Context Diagram" width="60%" /></p>

- **webproxy**: This is the actual webproxy module you use in your application to handles the downloads. Just inject the `IWebproxyService` into your spring beans and call the following provided methods:
	- `getString(GetStringConfig, URIs)`: to download via [HttpClient](http://hc.apache.org/httpclient-3.x/). You could parse the HTML string with [JSoup](https://jsoup.org/) to extract the required data or directly process a REST service result as CSV/XML/JSON. By giving multiple requests at the same time here, they will be processes in parallel for maximum performance. The `GetStringConfig` allows to configure things like browser agent, parallelity, proxy pool settings, proxy quality, retries, visited URI filtering and callbacks. Since proxies often are restricted or give garbage results, the `AProxyResponseCallback` can be provided with an implementation that verifies the payload of a proxy response against an expected result. For example you could check for a title string that should be in the given returned web site via `HtmlPageTitleProxyResponseCallback` and/or you could request the website to be downloaded twice with the same result by more than one proxy server via `DownloadTwiceProxyResponseCallback`. You can also provide a different kind of callback with `AStatisticsCallback` that allows you to measure statistics of the downloads. For example the `ConsoleReportStatisticsCallback` will print a nice summary to the console when calling its `logFinalReport()` method. You can even reuse the same callback instance over multiple requests to measure aggregated statistics.
	- `getPage(GetPageConfig, URIs)`: to download via [HtmlUnit](http://htmlunit.sourceforge.net/) which provides an in-memory/headless web browser that supports Javascript and CSS for more elaborate and dynamic parsing needs. Try the `page.asText()` method to parse the result as a TEXT/CSV file to get more robust code that does not rely on the actual HTML tags. By giving multiple requests at the same time here, they will be processes in parallel for maximum performance.  The `GetPageConfig` allows configuration beyond what `GetStringConfig` allows to configure a threshold for page refresh (might be a redirect), to enable CSS and to enable Javascript. You can also provide an implementation of `JavascriptWaitCallback` to define how long the download should wait for Ajax updates for data loading or initial rendering to finish when working with complex websites.
	- `newWebClient(GetPageConfig)`: if you want to manage an HtmlUnit session over multiple page requests manually. This will give you an instance of a preconfigured `WebClient` instance that you can use yourself. You have to manage parallelization here yourself, though it is not recommended to reuse the same `WebClient` instance in multiple threads, instead make sure to get a new instance for each thread. Also make sure to shut down the `WebClient` instance by calling `closeAllWindows()` after you are done with it.
	- `newProxy(GetStringConfig)`: if you want to fetch a specific proxy from the available pool by applying advanced filtering like timezone/country/quality/type and then setting it as a fixed proxy in `GetStringConfig`/`GetPageConfig`. The proxies don't have to be returned, since they will just rotate automatically. Proxies will automatically be verified and warmed up to make sure they actually work before they are returned from the pool, this happens here in the same way as it is done internally by the `getString`/`getPage` methods. Though if a proxy does not work you have to manually call `IBrokerService.addToBeVerifiedProxies` so the broker can update this information in its database and schedule a reverification of the proxy server (this normally happens automatically when using the other methods).
	- Advanced Configuration: the following system properties can be used to apply advanced customization:
```properties
# defines how many downloads are possible in parallel
de.invesdwin.webproxy.WebproxyProperties.MAX_PARALLEL_DOWNLOADS=100
# sleep time that is given for automatic page refreshes to occur
de.invesdwin.webproxy.WebproxyProperties.PROXY_VERIFICATION_REDIRECT_SLEEP=15 SECONDS
# may increase the number of detected proxies at the cost of performance. If deactivated only invalid responses may cause a retry
de.invesdwin.webproxy.WebproxyProperties.PROXY_VERIFICATION_RETRY_ON_ALL_EXCEPTIONS=false
# how long a proxy may reside in the pool before it is being reverified
de.invesdwin.webproxy.WebproxyProperties.PROXY_POOL_WARMUP_TIMEOUT=10 MINUTES
# if proxies should have a pause after a download, before the next download is started
de.invesdwin.webproxy.WebproxyProperties.PROXY_POOL_COOLDOWN_ALLOWED=true
# determines the randomized minimum limit of the proxy pause after a download
de.invesdwin.webproxy.WebproxyProperties.PROXY_POOL_COOLDOWN_MIN_TIMEOUT=100 MILLISECONDS
# determines the randomized maximum limit of the proxy pause after a download
de.invesdwin.webproxy.WebproxyProperties.PROXY_POOL_COOLDOWN_MAX_TIMEOUT=15 SECONDS
# how long a download may take maximally. This may help against evil or slow proxies that don't cause a timeout but also don't return anything
de.invesdwin.webproxy.WebproxyProperties.DEFAULT_MAX_DOWNLOAD_TRY_DURATION=10 MINUTES
# how many retries are allowed normally (0-99). Proxy caused tries do not count here
de.invesdwin.webproxy.WebproxyProperties.DEFAULT_MAX_DOWNLOAD_RETRIES=3
# determines if download exceptions should only cause a warning or should be rethrown. This is useful for debugging.
de.invesdwin.webproxy.WebproxyProperties.DEFAULT_MAX_DOWNLOAD_RETRIES_WARNING_ONLY=false
# all retries count here. This is a safety net if IsNotProxiesFaultProxyResponseCallback has bad rules.
de.invesdwin.webproxy.WebproxyProperties.MAX_ABSOLUTE_DOWNLOAD_RETRIES=100
# if not working proxies should be transmitted as such to the webproxy broker during the verification of proxies
de.invesdwin.webproxy.WebproxyProperties.AUTO_NOTIFY_ABOUT_NOT_WORKING_POOLED_PROXIES=false
```
- **ws-registry-dist**: specifically this is the `invesdwin-context-integration-ws-registry` module as provided by [invesdwin-context-integration](https://github.com/subes/invesdwin-context-integration) to act as a mediator for looking up the `webproxy-broker` instance to fetch working proxies from. If you do not enable proxy support in the `invesdwin-webproxy` module, then you don't have to actually deploy a registry server. You could even implement `IBrokerService` yourself and serve it as a spring bean to use an entirely different implementation.
