package de.invesdwin.webproxy.internal.proxypool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.InitializingBean;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.concurrent.pool.commons.ACommonsObjectPool;
import de.invesdwin.webproxy.WebproxyProperties;

@ThreadSafe
@Named
public class BrokerProxyObjectPool extends ACommonsObjectPool<PooledProxy> implements InitializingBean {

    private final WrappedExecutorService proxyCooldownMonitorExecutor = Executors
            .newFixedCallerRunsThreadPool(getClass().getSimpleName() + "_ProxyCooldown", 1);
    private final BlockingQueue<PooledProxy> proxyRotation = new LinkedBlockingQueue<PooledProxy>();
    /**
     * To make a bot detection less likely, the proxies get a cooldown time before they are used again. Working proxies
     * are spread better in usage like this, because not just the fastest ones are used the most.
     */
    private final List<PooledProxy> proxyCooldown = new CopyOnWriteArrayList<PooledProxy>();

    @Inject
    private BrokerProxyPoolableObjectFactory brokerProxyPoolableObjectFactory;

    public BrokerProxyObjectPool() {
        super(null);
        if (WebproxyProperties.PROXY_POOL_COOLDOWN_ALLOWED) {
            proxyCooldownMonitorExecutor.execute(new ProxyCooldownMonitor());
        }
    }

    @Override
    protected PooledProxy internalBorrowObject() {
        PooledProxy proxy = proxyRotation.poll();
        if (proxy == null) {
            proxy = factory.makeObject();
        }
        return proxy;
    }

    @Override
    public int getNumIdle() {
        return proxyRotation.size();
    }

    @Override
    public Collection<PooledProxy> internalClear() {
        final Collection<PooledProxy> removed = new ArrayList<PooledProxy>();
        while (proxyRotation.size() > 0) {
            removed.add(proxyRotation.remove());
        }
        return removed;
    }

    @Override
    protected PooledProxy internalAddObject() {
        final PooledProxy pooled = factory.makeObject();
        proxyRotation.add(factory.makeObject());
        return pooled;
    }

    @Override
    protected void internalReturnObject(final PooledProxy obj) {
        if (WebproxyProperties.PROXY_POOL_COOLDOWN_ALLOWED) {
            obj.startCoolingDown();
            proxyCooldown.add(obj);
        } else {
            proxyRotation.add(obj);
        }
    }

    @Override
    protected void internalInvalidateObject(final PooledProxy obj) {
        //Nothing happens
    }

    @Override
    protected void internalRemoveObject(final PooledProxy obj) {
        proxyRotation.remove(obj);
    }

    private class ProxyCooldownMonitor implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Threads.throwIfInterrupted();
                    TimeUnit.MILLISECONDS.sleep(100);
                    final List<PooledProxy> copy = new ArrayList<PooledProxy>(proxyCooldown);
                    for (final PooledProxy proxy : copy) {
                        if (proxy.isCooledDown()) {
                            Assertions.assertThat(proxyCooldown.remove(proxy)).isTrue();
                            proxyRotation.add(proxy);
                        }
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        setFactory(brokerProxyPoolableObjectFactory);
    }

}
