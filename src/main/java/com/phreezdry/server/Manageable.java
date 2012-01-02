package com.phreezdry.server;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manageable
 */
public interface Manageable {
    public static Manageable[] EMPTY_ARRAY = new Manageable[0];

    void registerMBean(MBeanServer server) throws JMException;

    void unregisterMBean(MBeanServer server) throws JMException;

    static class Util {
        static final Logger logger = Logger.getLogger(Manageable.class.getName());

        public static void registerMBean(MBeanServer server, Manageable... beans) {
            for (Manageable bean : beans)
                registerMBean(server, bean);
        }

        public static void unregisterMBean(MBeanServer server, Manageable... beans) {
            for (Manageable bean : beans)
                unregisterMBean(server, bean);
        }

        public static void registerMBean(MBeanServer server, String name, Object mBean) {
            try {
                server.registerMBean(mBean, new ObjectName(name));
                logger.info("Registered JMX MBean " + mBean.getClass().getName() + "/" + name);
            }
            catch (JMException e) {
                logger.log(Level.WARNING, "Error registering MBean", e);
            }
        }

        public static void unregisterMBean(MBeanServer server, String name) {
            try {
                server.unregisterMBean(new ObjectName(name));
            }
            catch (JMException e) {
                logger.log(Level.WARNING, "Error unregistering MBean", e);
            }
        }

        public static void registerMBean(MBeanServer server, Manageable manageable) {
            try {
                manageable.registerMBean(server);
            }
            catch (JMException e) {
                logger.log(Level.WARNING, "Error registering MBean", e);
            }
        }

        public static void unregisterMBean(MBeanServer server, Manageable manageable) {
            try {
                manageable.unregisterMBean(server);
            }
            catch (JMException e) {
                logger.log(Level.WARNING, "Error unregistering MBean", e);
            }
        }
    }
}
