package com.phreezdry.server;
/**
 * @author petrovic May 23, 2010 11:27:06 AM
 */

import com.phreezdry.util.Constants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import javax.management.JMException;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Metrics implements Manageable {
    private final Logger logger = Logger.getLogger(Metrics.class.getName());

    private final CacheManager mgr;

    public Metrics() {
        mgr = CacheManager.create(getClass().getResource("/ehcache.xml"));
        try {
            registerMBean(ManagementFactory.getPlatformMBeanServer());
        } catch (JMException e) {
            logger.log(Level.WARNING, null, e);
        }
    }

    @Override
    public void registerMBean(MBeanServer server) throws JMException {
        Manageable.Util.registerMBean(server, Constants.JMX_PREFIX + "type=Metrics", new Mgt());
    }

    @Override
    public void unregisterMBean(MBeanServer server) throws JMException {
        Manageable.Util.unregisterMBean(server, Constants.JMX_PREFIX + "type=Metrics");
    }

    public class Mgt implements MgtMBean {

        @Override
        public void dumpCacheStats() {
            for (Caches t : Caches.values()) {
                Cache cache = mgr.getCache(t.key);
                logger.info(cache.getStatistics().toString());
            }
        }
    }

    public interface MgtMBean {
        public void dumpCacheStats();
    }
}
