package com.phreezdry.server.rest;
/**
 * @author petrovic May 21, 2010 7:19:57 PM
 */

import com.phreezdry.entity.User;
import com.phreezdry.server.Caches;
import com.phreezdry.server.Config;
import com.phreezdry.util.Constants;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.carpediem.util.Base64;
import org.carpediem.util.StringUtilities;
import org.jets3t.service.S3Service;
import org.jets3t.service.model.S3Bucket;

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

    private final String[] charMap = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
    private S3Service s3Service;
    private Base64 b64;
    private Cache documentCache;
    private CacheManager cacheManager;
    private S3Bucket bucket;
    private Config config;

    protected AbstractResource(@Context UriInfo uriInfo, @Context ServletContext servletContext,
                               @Context HttpServletRequest httpServletRequest) {
        this.servletContext = servletContext;
        this.httpServletRequest = httpServletRequest;
        HttpSession session = httpServletRequest.getSession();
        this.uriInfo = uriInfo;
        if (session != null) {
            loginUserName = (String) session.getAttribute(Constants.LOGIN_USERNAME);
            loginUser = (loginUserName == null) ? null : (User) session.getAttribute(Constants.LOGIN_USER);
        } else {
            loginUserName = null;
            loginUser = null;
        }
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

    protected S3Service getS3Service() {
        if (s3Service == null) {
            s3Service = (S3Service) servletContext.getAttribute(Constants.S3_SERVICE);
        }
        return s3Service;
    }

    protected Base64 getBase64() {
        if (b64 == null) {
            b64 = (Base64) servletContext.getAttribute(Constants.BASE64);
        }
        return b64;
    }

    public Cache getDocumentCache() {
        if (documentCache == null) {
            documentCache = (Cache) servletContext.getAttribute(Caches.DOCUMENT.key);
        }
        return documentCache;
    }

    public S3Bucket getBucket() {
        if (bucket == null) {
            bucket = (S3Bucket) servletContext.getAttribute(Constants.S3_BUCKET_DOC);
        }
        return bucket;
    }

    public CacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = (CacheManager) servletContext.getAttribute(Constants.CACHEMGR);
        }
        return cacheManager;
    }

    public Config getConfig() {
        if (config == null) {
            config = (Config) servletContext.getAttribute(Constants.CONFIG);
        }
        return config;
    }

}
