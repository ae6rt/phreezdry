package com.phreezdry.server.rest;
/**
 * @author petrovic May 21, 2010 7:20:21 PM
 */

import com.phreezdry.entity.Document;
import com.phreezdry.persistence.PersistenceException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

@Path("dry")
public class DryResource extends AbstractResource {
    private static final Logger logger = Logger.getLogger(DryResource.class.getName());
    private final String documentIdRegex = "{docId: [0-9a-fA-F]+}";

    public DryResource(@Context UriInfo uriInfo, @Context ServletContext servletContext,
                       @Context HttpServletRequest httpServletRequest) {
        super(uriInfo, servletContext, httpServletRequest);
    }

    /**
     * Retrieve a document.
     *
     * @param documentId
     * @return
     */
    @GET
    @Path(documentIdRegex)
    public Response getDocument(@PathParam("docId") String documentId) {
        requireValidDocumentId(documentId);

        try {
            Document document = persistenceManager.retrieve(documentId);
            if (document != null) {
                return Response.ok().header("Content-Length", document.getText().length()).entity(document.getText()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (PersistenceException e) {
            return Response.serverError().entity("error retrieving document").build();
        }
    }

    /**
     * Create a document.
     *
     * @param text
     * @return
     */
    @PUT
    public Response createDocument(@FormParam("document") @DefaultValue("") String text) {
        requireLoggedIn();
        requireValidDocument(text);
        try {
            Document document = persistenceManager.persist(text, loginUserName);
            return Response.created(uri(document.getId())).build();
        } catch (PersistenceException e) {
            return Response.serverError().entity(String.format("error creating document: %s %s", e.getCause(), e.getMessage())).build();
        }
    }

    /**
     * Delete document from cache or backing store or both
     *
     * @param documentId
     */
    @DELETE
    @Path(documentIdRegex)
    public void deleteDocument(@PathParam("docId") String documentId) {
        persistenceManager.delete(documentId);
    }
}
