package com.phreezdry.server.servlet;

import com.phreezdry.server.Caches;
import com.phreezdry.util.Constants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

/**
 * @author petrovic May 21, 2010 11:18:37 PM
 */

public class RateLimitFilter implements Filter {
    private Cache cache;

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws ServletException, IOException {
        String remoteAddr = req.getRemoteAddr();
        String path = ((HttpServletRequest) req).getPathInfo();
        String queryString = ((HttpServletRequest) req).getQueryString();
        if (!exempt(path)) {
            String key = new StringBuilder(remoteAddr).append(":").append(path).append(queryString == null ? "" : queryString).toString();
            Element element = cache.get(key);
            if (element == null) {
                element = new Element(key, new ConnectionBag());
                cache.put(element);
                filterChain.doFilter(req, resp);
            } else {
                ConnectionBag bag = (ConnectionBag) element.getObjectValue();
                if (bag.ok()) {
                    filterChain.doFilter(req, resp);
                } else {
                    HttpServletResponse httpR = (HttpServletResponse) resp;
                    httpR.setStatus(HttpURLConnection.HTTP_GONE);
                    httpR.getWriter().write("rate exceeded");
                }
            }
        } else {
            filterChain.doFilter(req, resp);
        }
    }

    private boolean exempt(String path) {
        // exempt some paths
        return false;
    }

    public void init(FilterConfig config) throws ServletException {
        CacheManager cm = CacheManager.create(RateLimitFilter.class.getResource(Constants.CACHE_CONFIG));
        cache = cm.getCache(Caches.RATELIMIT.key);
    }

    public void destroy() {
    }

    private class ConnectionBag {
        // all times are in milliseconds
        private long stamp = 0;
        private long delta = 2000;
        private int hitCount = 0;
        private final long start_delta = 2000;
        private final long max_delta = 1200000;
        private final long safePeriod = 300000;
        private final int threshold = 10;

        public ConnectionBag() {
        }

        public boolean ok() {
            boolean limitReached = false;
            long now = new Date().getTime();
            if (delta > max_delta) {
                delta = max_delta;
            }
            if (now <= stamp + delta) {
                ++hitCount;
                if (hitCount >= threshold) {
                    limitReached = true;
                    delta += delta;
                }
            } else {
                hitCount = 0;
            }
            if (now > stamp + safePeriod) {
                delta = start_delta;
                hitCount = 0;
            }
            stamp = now;
            return !limitReached;
        }
    }

}
