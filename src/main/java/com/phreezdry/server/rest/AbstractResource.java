package com.phreezdry.server.rest;
/**
 * @author petrovic May 21, 2010 7:19:57 PM
 */

import com.phreezdry.entity.User;
import com.phreezdry.persistence.PersistenceManager;
import com.phreezdry.server.Config;
import com.phreezdry.util.Constants;
import net.sf.ehcache.CacheManager;
import org.carpediem.util.StringUtilities;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.logging.Logger;


public class AbstractResource {
    private final Logger logger = Logger.getLogger(AbstractResource.class.getName());

    protected final ServletContext servletContext;
    protected final HttpServletRequest httpServletRequest;
    protected final UriInfo uriInfo;
    protected final User loginUser;
    protected final String loginUserName;
    protected final PersistenceManager persistenceManager;
    protected final Config config;
    protected final ApplicationContext appContext;

    protected AbstractResource(@Context UriInfo uriInfo, @Context ServletContext servletContext,
                               @Context HttpServletRequest httpServletRequest) {

        this.servletContext = servletContext;
        this.httpServletRequest = httpServletRequest;
        this.uriInfo = uriInfo;

        HttpSession session = httpServletRequest.getSession();
        if (session != null) {
            loginUserName = (String) session.getAttribute(Constants.LOGIN_USERNAME);
            loginUser = (loginUserName == null) ? null : (User) session.getAttribute(Constants.LOGIN_USER);
        } else {
            loginUserName = null;
            loginUser = null;
        }

        appContext = (ApplicationContext) servletContext.getAttribute(Constants.CONTEXT);
        persistenceManager = (PersistenceManager) appContext.getBean(Constants.PERSISTENCE_MGR);
        config = (Config) appContext.getBean(Constants.CONFIG);
    }

    protected User requireLoggedIn() throws WebApplicationException {
        if (loginUser == null)
            throw new WebApplicationException(unauthorized());
        return loginUser;
    }

    protected void requireValidDocument(String document) {
        if (StringUtilities.isEmpty(document)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Document has no content")
                            .build());
        }
        if (document.length() > Constants.MAXDOCSIZE) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(String.format("Document too long: %d.  Maximum length is %d", document.length(), Constants.MAXDOCSIZE))
                            .build());
        }
    }

    protected void requireValidDocumentId(String id) throws WebApplicationException {
        if (StringUtilities.isEmpty(id) || id.length() != Constants.SHA1_LENGTH) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    protected void requireNotEmpty(String in) throws WebApplicationException {
        if (StringUtilities.isEmpty(in)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("expected an input value").build());
        }
    }


    protected URI uri(String documentId) {
        return uriInfo.getBaseUriBuilder().path(DryResource.class).path(documentId).build();
    }

    protected Response unauthorized() {
        if (loginUser != null)
            return Response.status(Response.Status.UNAUTHORIZED).build();
        else
            return Response.status(Response.Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"phreezdry\"").build();
    }
}
