package com.phreezdry.server.rest;
/**
 * @author petrovic May 23, 2010 12:43:35 PM
 */

import com.phreezdry.entity.EntityType;
import com.phreezdry.persistence.PersistenceException;
import org.carpediem.util.StringUtilities;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Level;
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
            return String.format("SVN revision=%s\nbuild date=%s\n", config.getSvnRevision(), config.getBuildDate());
        }
        return "";
    }

    @PUT
    @Path("dumpstats")
    public String dumpStats(@FormParam("key") @DefaultValue("") String key) {
        if (!StringUtilities.isEmpty(key)) {
            return persistenceManager.cacheReport();
        }
        return "";
    }

    @PUT
    @Path("document/count")
    public String docCount(@FormParam("key") @DefaultValue("") String key) {
        if (!StringUtilities.isEmpty(key)) {
            try {
                return persistenceManager.count(EntityType.DOCUMENT);
            } catch (PersistenceException e) {
                logger.log(Level.WARNING, "error retrieving entity count", e);
            }
        }
        return "";
    }

}
