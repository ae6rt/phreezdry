package com.phreezdry.server.rest;
/**
 * @author petrovic May 22, 2010 9:44:25 AM
 */

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

@Path("user")
public class UserResource extends AbstractResource {
    private final Logger logger = Logger.getLogger(UserResource.class.getName());

    public UserResource(@Context UriInfo uriInfo, @Context ServletContext servletContext, @Context HttpServletRequest httpServletRequest) {
        super(uriInfo, servletContext, httpServletRequest);
    }

    @PUT
    @Path("register")
    public Response register(@FormParam("email") @DefaultValue("") String email) {
        return Response.ok().build();
    }

    @PUT
    @Path("confirm")
    public Response confirm(@FormParam("confirm") @DefaultValue("") String confirm) {
        return Response.created(null).build();
    }

}
