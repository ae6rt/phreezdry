package com.phreezdry.server.servlet;

/**
 * @author petrovic May 21, 2010 11:53:39 PM
 */

import com.phreezdry.server.Caches;
import com.phreezdry.server.Config;
import com.phreezdry.server.Metrics;
import com.phreezdry.util.Constants;
import com.phreezdry.util.Platform;
import net.sf.ehcache.CacheManager;
import org.carpediem.util.Base64;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.security.AWSCredentials;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartupListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(StartupListener.class.getName());
    private final Metrics metrics = null;

    private CacheManager mgr;

    public StartupListener() {
//        metrics = new Metrics();
    }

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        mgr = CacheManager.create(getClass().getResource("/ehcache.xml"));
        logger.info("ehcache disk store path: " + mgr.getDiskStorePath());

        context.setAttribute(Caches.RATELIMIT.key, mgr.getCache(Caches.RATELIMIT.key));
        context.setAttribute(Caches.DOCUMENT.key, mgr.getCache(Caches.DOCUMENT.key));
        context.setAttribute(Caches.USER.key, mgr.getCache(Caches.USER.key));
        context.setAttribute(Constants.CACHEMGR, mgr);
        context.setAttribute(Constants.BASE64, new Base64());
        try {
            Config config = new Config();
            context.setAttribute(Constants.CONFIG, config);
            context.setAttribute(Constants.S3_SERVICE, new RestS3Service(new AWSCredentials(config.getAwsKey(), config.getAwsSecret())));

            String bucketPrefix = Platform.isUnitTest() ? "test." : "";
            context.setAttribute(Constants.S3_BUCKET_USER, new S3Bucket(String.format("%s%s", bucketPrefix, config.getUserBucketName())));
            context.setAttribute(Constants.S3_BUCKET_DOC, new S3Bucket(String.format("%s%s", bucketPrefix, config.getDocBucketName())));
        } catch (S3ServiceException e) {
            logger.log(Level.SEVERE, "cannot instantiate S3 service", e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        mgr.shutdown();
    }

}
