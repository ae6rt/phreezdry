package com.phreezdry.server.servlet;


import com.phreezdry.entity.User;
import com.phreezdry.persistence.PersistenceManager;
import com.phreezdry.util.Constants;
import org.carpediem.util.Base64;
import org.springframework.context.ApplicationContext;

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

    private Base64 base64;
    private PersistenceManager pm;

    public void init(FilterConfig config) throws ServletException {
        base64 = new Base64();
        ApplicationContext appContext = (ApplicationContext) config.getServletContext().getAttribute(Constants.CONTEXT);
        pm = (PersistenceManager) appContext.getBean(Constants.PERSISTENCE_MGR);
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
                User user = pm.getUser(u);

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

}
