package com.phreezdry.server.rest;
/**
 * @author petrovic May 23, 2010 12:43:35 PM
 */

import com.phreezdry.server.Caches;
import com.phreezdry.util.Constants;
import net.sf.ehcache.Cache;
import org.carpediem.util.StringUtilities;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

@Path("admin")
public class AdminResource extends AbstractResource {
    private final Logger logger = Logger.getLogger(AdminResource.class.getName());

    public AdminResource(@Context UriInfo uriInfo, @Context ServletContext servletContext, @Context HttpServletRequest httpServletRequest) {
        super(uriInfo, servletContext, httpServletRequest);
    }

    @PUT
    @Path("buildInfo")
    public String buildInfo(@FormParam("key") @DefaultValue("") String key) {
        if (!StringUtilities.isEmpty(key)) {
            return String.format("SVN revision=%s\nbuild date=%s\n", getConfig().getSvnRevision(), getConfig().getBuildDate());
        }
        return "";
    }

    @PUT
    @Path("dumpstats")
    public String dumpStats(@FormParam("key") @DefaultValue("") String key) {
        if (!StringUtilities.isEmpty(key)) {
            StringBuilder sb = new StringBuilder();
            for (Caches t : Caches.values()) {
                Cache cache = getCacheManager().getCache(t.key);
                sb.append(cache.getStatistics().toString()).append('\n');
            }
            return sb.toString();
        }
        return "";
    }

    @PUT
    @Path("dumpDocumentInfo")
    public String dumpDocumentInfo(@FormParam("key") @DefaultValue("") String key) throws S3ServiceException {
        if (!StringUtilities.isEmpty(key)) {
            S3Service s3Service = getS3Service();
            S3Bucket bucket = new S3Bucket(getConfig().getDocBucketName());
            S3Object[] objects = s3Service.listObjects(bucket);
            StringBuilder r = new StringBuilder();

            for (S3Object o : objects) {
                StringBuilder sb = new StringBuilder();
                S3Object s3Object = s3Service.getObjectDetails(bucket, o.getKey());
                sb.append(s3Object.getMetadata(Constants.LOGIN_USERNAME));
                sb.append(' ');
                sb.append(s3Object.getKey());
                sb.append(' ');
                sb.append(Boolean.valueOf((String) s3Object.getMetadata(Constants.COMPRESSED)) ? "compressed" : "uncompressed");
                r.append(sb).append('\n');
            }
            return r.toString();
        }
        return "";
    }

}
