package com.phreezdry.server.servlet;


import com.phreezdry.entity.User;
import com.phreezdry.server.Caches;
import com.phreezdry.util.Constants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.carpediem.util.Base64;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mark Petrovic
 */
public class AuthFilter implements Filter {
    private final static Logger logger = Logger.getLogger(AuthFilter.class.getName());

    private S3Service s3Service;
    private S3Bucket userBucket;
    private Base64 base64;
    private Cache userCache;

    public void init(FilterConfig config) throws ServletException {
        s3Service = (S3Service) config.getServletContext().getAttribute(Constants.S3_SERVICE);
        userBucket = (S3Bucket) config.getServletContext().getAttribute(Constants.S3_BUCKET_USER);
        userCache = (Cache) config.getServletContext().getAttribute(Caches.USER.key);
        base64 = new Base64();
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader != null) {
            HttpSession session = httpRequest.getSession(true);
            String username = (String) session.getAttribute(Constants.LOGIN_USERNAME);
            if (username == null) {
                if (authHeader.startsWith("Basic ")) {
                    authHeader = authHeader.substring("Basic ".length());
                }
                String decoded = new String(base64.decode(authHeader));
                int ix = decoded.indexOf(':');
                String u = decoded.substring(0, ix);
                String p = decoded.substring(ix + 1);
                User user = getUser(u);

                if (user != null && user.getPassword().equals(p)) {
                    session.setAttribute(Constants.LOGIN_USERNAME, user.getEmail());
                    session.setAttribute(Constants.LOGIN_USER, user);
                    if (logger.isLoggable(Level.INFO)) {
                        logger.log(Level.INFO, "Authentication successful for " + u);
                    }
                } else {
                    if (user == null) {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.log(Level.INFO, String.format("Unknown user %s", u));
                        }
                    } else {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.log(Level.INFO, String.format("Authentication failed for user %s", u));
                        }
                    }
                }
            }
        }
        chain.doFilter(req, resp);
    }

    private User getUser(String u) throws IOException {
        // try cache first
        Element element = userCache.get(u);
        if (element != null) {
            return (User) element.getObjectValue();
        }

        try {
            S3Object userObject = s3Service.getObject(userBucket, u);
            String password = (String) userObject.getMetadata("password");
            userObject.closeDataInputStream();
            User user = new User(u, password);
            userCache.put(new Element(u, user));
            return user;
        } catch (S3ServiceException e) {
            return null;
        }
    }
}
