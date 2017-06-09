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

Please be aware that web scraping is a bit of a grey area in a legal sense. For personal use it is normally not a problem to do web scraping and data extraction as long as the terms of use given by the specific website allow automated processing as a use case. Though you might still not be allowed to do whatever you want with the acquired information as there are database laws in many countries that prohibit redistribution of such acquired data. Also commercial use of such data is often not allowed. On the other hand it is mostly allowed to do web scraping in a manner like google or other search/index companies do. It is often tolerated to redistribute parts of the data as long as e.g. not more than 25% of the original data is exposed (which again is a grey area definition and nothing to be counted on). Or it might be allowed to redistribute an aggregated form of the data like statistics or other kinds of measurements. In any case you should consult a lawyer for your specific case if you are in doubt about the legality of what you are doing.

Despite the case of web scraping and the database laws, there are also laws in many countries regarding intrusion into protected systems. Some websites might throttle requests per IP on a time basis which you could work around by using a pool of proxies to distribute your requests over. But this could count as an intrusion since you are actually working around protection mechanisms the provider built to enforce his terms of use you maybe agreed to by accessing his service. The provider most likey defined those terms of use to prevent their server load from exploding because of unwanted users that want to copy their data. In that case your actions could be illegal and you should refrain from doing this. The provider could even see your actions as a [denial of service attack](https://en.wikipedia.org/wiki/Denial-of-service_attack). It would be better to license the data you are interested in and/or find an agreement with the provider about how you are allowed to obtain the data. In any case your code should be as efficient as possible and should cause as little impact on the actual service as possible.

Another example besides the web scraping is the case of scanning the internet for public proxies and actually using them. In most countries this does not count as an intrusion, since a proxy server that is not protected (e.g. by a password or some other means) but instead available for public use is legal to be discovered and used. Though again this is no guarantee that a proxy might not have some terms of use included that you might be neglecting or that someone might mistake your actions as an intrusion into their systems. So it is better to refrain from acquiring proxies yourself and relying on any public proxy you find. Instead you should license a proxy list provider that either hosts his own proxy servers for your use or who makes sure his proxies are legal to be used for your use cases. In no way whatsoever should you hide illegal activities by executing them over proxy servers. Normally you would not need any proxy servers to hide behind, since you are only doing legal things anyway.

Anyhow, making the tools available that could be used for illegal deeds is in itself not illegal. Otherwise tools like [metasploit](https://www.metasploit.com/) or other penetration testing frameworks would not be allowed to exist. And web scraping is definitely not as harmful as actually breaking into systems via security weaknesses is. So what you do with this is what you should consider and consult your lawyers about if you are unsure about the legal aspects. We only provide this software for its usefulness in legally sound scenarios.

Legitimate use cases for web scraping might be:
- Automation of tasks on systems that do not provide APIs for those tasks.
- A solution for data integration scenarios with systems that do not provide proper REST services or if those are unreliable in comparison to the website itself (either self owned or with a business partner).

Legitimate use cases for using proxies might be:
- Doing load/performance testing on your own infrastructure and websites over multiple countries to check that your load balancing and content distribution network works properly, which you can not properly simulate without multiple servers in different locations.
- More authentic client simulation for automated acceptance testing during development by simulating sessions over multiple requests with users from different locations and a varying spectrum of connection quality (since many public proxies are extremely slow and have flaky connections).

Legitimate use cases for port scanning might be:
- Finding backdoors and unexpectedly open services or computers in your local network to check if it matches your security standards.
- Mapping a network for available services and including them in a service registry if accepted by the service provider or to tell the provider about him mistakenly exposing a service he should rather close off.

## Overview

The following context diagram shows what components this project consists of:

<p align="center"><img src="https://github.com/subes/invesdwin-webproxy/raw/master/invesdwin-webproxy-parent/invesdwin-webproxy/doc/webproxy_context.png" alt="Context Diagram" width="60%" /></p>


## Modules

The following modules and tools are included in this project:

- **webproxy**: This is the actual `invesdwin-webproxy` module you use in your client applications to handle the downloads. Just inject the `IWebproxyService` into your spring beans (though it is possible to execute downloads without a spring context by calling the internal classes directly) and call the following provided methods:
	- `getString(GetStringConfig, URIs)`: to download via [HttpClient](http://hc.apache.org/httpclient-3.x/). You could parse the HTML string with [JSoup](https://jsoup.org/) to extract the required data or directly process a REST service result as CSV/XML/JSON. By giving multiple requests at the same time here, they will be processes in parallel for maximum performance. The `GetStringConfig` allows to configure things like browser agent, parallelity, proxy pool settings, proxy quality, retries, visited URI filtering and callbacks. Since proxies often are restricted or give garbage results, the `AProxyResponseCallback` can be provided with an implementation that verifies the payload of a proxy response against an expected result. For example you could check for a title string that should be in the given returned web site via `HtmlPageTitleProxyResponseCallback` and/or you could request the website to be downloaded twice with the same result by more than one proxy server via `DownloadTwiceProxyResponseCallback`. You can also provide a different kind of callback with `AStatisticsCallback` that allows you to measure statistics of the downloads. For example the `ConsoleReportStatisticsCallback` will print a nice summary to the console when calling its `logFinalReport()` method. You can even reuse the same statistics callback instance over multiple requests to measure aggregated statistics.
	- `getPage(GetPageConfig, URIs)`: to download via [HtmlUnit](http://htmlunit.sourceforge.net/) which provides an in-memory/headless web browser that supports Cookies, Javascript and CSS for more elaborate and dynamic parsing needs. Try the `page.asText()` method to parse the result as a TEXT/CSV file to get more robust code that does not rely on the actual HTML tags and structure. By giving multiple requests at the same time here, they will be processes in parallel for maximum performance.  The `GetPageConfig` allows configuration beyond what `GetStringConfig` allows to configure a threshold for page refresh (might be a redirect), to enable CSS and to enable Javascript. You can also provide an implementation of `JavascriptWaitCallback` to define how long the download should wait for Ajax updates for data loading or initial rendering to finish when working with complex websites.
	- `newWebClient(GetPageConfig)`: if you want to manage an HtmlUnit session over multiple page requests manually. This will give you an instance of a preconfigured `WebClient` instance that you can use yourself. You have to manage parallelization here yourself. It is not recommended to reuse the same `WebClient` instance in multiple threads, instead make sure to get a new instance for each thread. Also make sure to shut down the `WebClient` instance by calling `closeAllWindows()` after you are done with it.
	- `newProxy(GetStringConfig)`: if you want to fetch a specific proxy from the available pool to apply advanced filtering like timezone/country/quality/type and then setting it as a fixed proxy in `GetStringConfig`/`GetPageConfig`. The proxies don't have to be returned to the pool, since they will just rotate automatically. Proxies will automatically be verified and warmed up (maybe there is a welcome page hindering the first request) to make sure they actually work before they are given from the pool. This happens here in the same way as it is done internally by the `getString()`/`getPage()` methods. Though if a proxy does not work you have to manually call `IBrokerService.addToBeVerifiedProxies()` so the broker can update this information in its database and schedule a reverification of the proxy server (this normally happens automatically when using the other methods).
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
- **registry**: specifically this is the `invesdwin-context-integration-ws-registry` module as provided by [invesdwin-context-integration](https://github.com/subes/invesdwin-context-integration) to act as a mediator for looking up the `webproxy-broker` instance to fetch working proxies from. If you do not enable proxy support in the `invesdwin-webproxy` module, then you don't have to actually deploy a registry server (or a broker instance for that matter). You could even implement `IBrokerService` yourself and serve it as a spring bean to use an entirely different implementation. If using the standard implementation, then communication happens via a web service that is provided by the broker instance.
- **broker**: The `invesdwin-webproxy-broker` module mediates between the webproxy clients and the backend modules for proxy acquisition and database maintenance for keeping the information up to date about which proxies are working and what metadata (timezone, country, quality) they have. It schedules tasks for the crawler modules and keeps a database of working and raw proxies. The following system properties are available for advanced configuration:
```properties
de.invesdwin.webproxy.broker.internal.BrokerProperties.PROXY_DOWNTIME_TOLERANCE=18 HOURS
de.invesdwin.webproxy.broker.internal.BrokerProperties.MAX_SPECIFIC_TO_BE_SCANNED_PORTS=1000
de.invesdwin.webproxy.broker.internal.BrokerProperties.ADDITIONAL_RANDOM_TO_BE_SCANNED_PORTS_PERCENT=25
```
- **crawler**: The `invesdwin-webproxy-crawler` module provides a worker instance that you can host on multiple servers in order to distribute the workload of proxy acquisition and verification. When the broker requests new proxies, then the crawler can download raw proxy lists from your given `IProxyCrawlerSource` implementations. A raw proxy can also only contain an IP, in which case the port will be tried to be discovered automatically by checking the most common proxy ports. The `webproxy-crawler-tests` module provides some sample implementations, though they are included only for testing purposes here and should not be relied upon without asking the appropriate provider for permission if that is questionable. Though there are a few websites that specifically state that the given information is free for personal use. As an alternative you could buy a proxy subscription somewhere and parse the provided CSV file here doing some other form of integration. If you want to scan your network for open proxies, you could enable random proxy discovery by setting the system property `de.invesdwin.webproxy.crawler.CrawlerProperties.RANDOM_SCAN_ALLOWED=true` which will scan random IPs and check the most common proxy ports for discovering public proxies. Though be aware that it is not a good idea to use this to acquire proxies from the internet as it is quite inefficient (since lots of port scan TCP packets might slow down your network if it is not suitable for this; also the implementation might not be as efficient as it could be) and legally questionable depending on your country (though in most countries it is legal to discover and use public proxy servers to our knowledge, though we are no lawyers and can only advice against doing something like this). Multiple instances of the crawler will themselves ask the broker instance via the web service about the tasks they should perform. So the crawler instances don't have to be actually known by the broker server and it suffices to have the broker in the registry. The crawler also checks the quality of a proxy server and can differentiate the following classes:
	- TRANSPARENT: the proxy tells via request headers about what the original client IP is
	- ANONYMOUS: the proxy tells via request headers that it is a proxy server, but does not tell what the original client IP is
	- INVISIBLE: the proxy does not tell anything about being a proxy and acts as the actual client
- **portscan**: The `invesdwin-webproxy-portscan` module needs to be deployed next to your crawler instance on any given installation. It is separated from the crawler process (which should be running with restricted permission on the operating system since it talks to complex services on potentially other machines, notably the registry, broker and geolocation services) because it requires root permission on the operating system to send TCP packets for the SYN stealth scan (as described by the [Nmap documentation](https://nmap.org/book/man-port-scanning-techniques.html)). So to be a bit safer regarding security we decided to split this into a separate process and provide local communication via JMS (via an embedded local network of brokers as provided by `invesdwin-context-integration-jms`) or AMQP (via an installation of RabbitMQ that is connected to via `invesdwin-context-integration-amqp`). Internally the [pcap](https://en.wikipedia.org/wiki/Pcap) library is used via the java binding of [jpcapng](https://sourceforge.net/projects/jpcapng/). During startup it will automatically identify which network interface to use. Over that interface this module will execute tasks as requested by the crawler instance for doing ICMP requests (to check if a server is online), requests for specific TCP port scans on a given IP (to discover ports that actually have applications running) and requests for doing the TCP port scans on random IPs. The following system properties are available for advanced configuration:
```properties
#On the host port 80 must be open and a service has to be running on it. The host also has to answer pings so that the checks are successful
de.invesdwin.webproxy.portscan.internal.PortscanProperties.CHECK_HOST=google.de
de.invesdwin.webproxy.portscan.internal.PortscanProperties.LOCAL_BIND_PORT=44125
de.invesdwin.webproxy.portscan.internal.PortscanProperties.ICMP_RESPONSE_TIMEOUT=3 SECONDS
#For timings see: http://www.networkuptime.com/nmap/page09-09.shtml
de.invesdwin.webproxy.portscan.internal.PortscanProperties.UPLOAD_PAUSE_BETWEEN_PACKETS=0 MILLISECONDS
de.invesdwin.webproxy.portscan.internal.PortscanProperties.UPLOAD_PAUSE_BETWEEN_PACKETS_PER_HOST=0 MILLISECONDS
de.invesdwin.webproxy.portscan.internal.PortscanProperties.RESPONSE_TIMEOUT_BETWEEN_SYN_PACKETS_PER_HOST=500 MILLISECONDS
de.invesdwin.webproxy.portscan.internal.PortscanProperties.MAX_OPEN_ICMP_REQUESTS=25
de.invesdwin.webproxy.portscan.internal.PortscanProperties.MAX_OPEN_SYN_REQUESTS=10
```
- **geolocation**: This `invesdwin-webproxy-geolocation` module is not included in the context diagram but provides a web service that is used by the crawler instances to resolve metadata about the proxy servers. It uses the [GeoIP](https://github.com/maxmind/geoip-api-java) and [GeoNames](http://www.geonames.org/) databases to determine the location, country and timezone for a given proxy IP. You can also use this service to check any other IP (e.g. for customers in your web shop) to determine where it comes from. You could also buy a premium GeoIP subscription to increase the accuracy of the resulting coordinates that are measured against locations provided by GeoNames.

## Proxy Lifecycle

This is the process of how proxies are being discovered:
1. Crawler asks broker for new tasks, broker answers with a discovery request and/or a few proxies that should be reverified (it rechecks its database entries regularly to keep an up to date list of proxies, it also tasks proxies to be rechecked if they were reported to not be working by one of the clients). The list of most common ports to be scanned is also provided by the broker since they are derived from the actual database or a few hardcoded ports if the database is empty.
2. Crawler downloads raw proxies (either IP:port combinations or just IP) from the available `IProxyCrawlerSource` implementations and reports them back to the broker as raw proxies to be added to the database. Alternatively it tells its portscanner instance to do random scanning if that is enabled. Any potential raw proxy is reported back to the broker and will be received as scan requests on the next task request that the crawler does. By adding this round trip we can filter duplicate proxies on the broker before they are being scanned by potentially multiple crawler instances. Also we can distribute the load of scanning the raw proxies in the database over multiple crawlers, even if only one crawler discovered them.
3. Any scan requests for specific proxies the crawler received from the broker are passed to the portscanner. The first step is to do a ping request via ICMP.
4. Portscanner pings the given IP (or some random IP) and reports a success asynchronously back to the crawler.
5. Crawler receives IPs that were successfully pinged and requests the portscanner to do a port scan on the given host.
6. Portscanner executes the port scan on the given host and reports a success asynchronously back to the crawler.
7. The crawler now tries to use the given IP:port combination as a http and socks proxy against the rest API for proxy verification that the broker provides. The result of this request is either a download failure or a success with the information about what quality the proxy is of.
8. If a proxy was determined to be working, metadata is added via the geolocation service.
9. Discovered proxies are reported back to the broker as working to be added/updated in the database.
10. Client applications request proxies for a download and report back to the broker if a specific one did not work for it to be scheduled for reverification by a crawler instance.
11. Client applications will cycle through proxies and keep them on a cooldown to not overuse them on a given host.
