package com.phreezdry.server.servlet;

/**
 * @author petrovic May 21, 2010 11:53:39 PM
 */

import com.phreezdry.persistence.PersistenceManager;
import com.phreezdry.util.Constants;
import com.phreezdry.util.Platform;
import net.sf.ehcache.CacheManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

public class PhreezContextListener implements ServletContextListener {
    private final Logger logger = Logger.getLogger(PhreezContextListener.class.getName());
    private ClassPathXmlApplicationContext appContext;

    public PhreezContextListener() {
    }

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        appContext = new ClassPathXmlApplicationContext(Constants.SPRING_CONFIG);
        context.setAttribute(Constants.CONTEXT, appContext);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        /*
        A bit icky.  Don't shut the singleton cache manager down if unit testing.  Some sort of JUnit classloader issue.
         */
        if (!Platform.isTestEnvironment()) {
            PersistenceManager pm  = (PersistenceManager) appContext.getBean(Constants.PERSISTENCE_MGR);
            pm.shutdown();
        }
        appContext.close();
    }

}
