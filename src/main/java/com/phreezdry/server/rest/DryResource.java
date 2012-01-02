package com.phreezdry.server.rest;
/**
 * @author petrovic May 21, 2010 7:20:21 PM
 */

import com.phreezdry.util.Constants;
import net.sf.ehcache.Element;
import org.carpediem.util.Digests;
import org.carpediem.util.Gzip;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Object;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Requests by default go to <bucketname>.s3.amazonaws.com
 */
@Path("dry")
public class DryResource extends AbstractResource {
    private static final Logger logger = Logger.getLogger(DryResource.class.getName());
    private final String documentIdRegex = "{docId: [0-9a-fA-F]+}";

    public DryResource(@Context UriInfo uriInfo, @Context ServletContext servletContext,
                       @Context HttpServletRequest httpServletRequest) {
        super(uriInfo, servletContext, httpServletRequest);
    }

    @GET
    @Path(documentIdRegex)
    public Response getDocument(@PathParam("docId") String documentId) {
        documentId = documentId.toLowerCase();

        String document = fromCache(documentId);
        if (document != null) {
            return Response.ok().header("Content-Length", document.length()).entity(document).build();
        }

        try {
            S3Object s3Object = getS3Service().getObject(getBucket(), documentId);
            String raw = extractRawDocument(s3Object);
            Boolean isCompressed = Boolean.valueOf((String) s3Object.getMetadata(Constants.COMPRESSED));
            document = isCompressed
                    ? new String(Gzip.gunzip(getBase64().decode(raw), Constants.MAXDOCSIZE), Constants.CHARSET)
                    : raw;
            cache(documentId, document);
            return Response.ok().header("Content-Length", document.length()).entity(document).build();
        } catch (S3ServiceException e) {
            return Response.status(e.getResponseCode()).entity(e.getResponseStatus()).build();
        } catch (IOException e) {
            return Response.serverError().entity("error retrieving document").build();
        }
    }

    @PUT
    public Response createDocument(@FormParam("document") @DefaultValue("") String document) {
        requireLoggedIn();
        requireValidDocument(document);

        String documentId = id(document);

        if (fromCache(documentId) != null) {
            return Response.created(uri(documentId)).build();
        }

        try {
            S3Object object = makeS3Object(document, documentId);
            if (object != null) {
                getS3Service().putObject(getBucket(), object);
                cache(documentId, document);
                return Response.created(uri(documentId)).build();
            } else {
                return Response.serverError().entity("could not create document").build();
            }
        }
        catch (S3ServiceException e) {
            return Response.status(e.getResponseCode()).entity(e.getResponseStatus()).build();
        }
        catch (Exception e) {
            return Response.serverError().entity(String.format("Error creating document: %s", e.getMessage())).build();
        }
    }

    @DELETE
    @Path(documentIdRegex)
    public void deleteDocument(@PathParam("docId") String documentId) {
        documentId = documentId.toLowerCase();
        Element element = elementFromCache(documentId);
        if (element != null) {
            getDocumentCache().remove(element.getKey());
        }
    }

    private String id(String document) {
        return Digests.byteToHex(Digests.sha1(document, "UTF-8"));
    }

    private String extractRawDocument(S3Object s3Object) throws S3ServiceException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getDataInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private S3Object makeS3Object(String document, String documentId) {
        try {
            boolean compress = document.length() > Constants.COMPPRESS_THRESHOLD;
            S3Object object = new S3Object(documentId, compress ? getBase64().encode(Gzip.gzip(document, "UTF-8")) : document);
            object.addMetadata(Constants.LOGIN_USERNAME, loginUserName);
            object.addMetadata(Constants.COMPRESSED, Boolean.valueOf(compress).toString());
            return object;
        } catch (NoSuchAlgorithmException wontHappen) {
            logger.log(Level.WARNING, null, wontHappen);
        } catch (IOException e) {
            logger.log(Level.WARNING, null, e);
        }
        return null;
    }

    private Element elementFromCache(String documentId) {
        return getDocumentCache().get(documentId);
    }

    private String fromCache(String documentId) {
        Element el = getDocumentCache().get(documentId);
        return el != null ? (String) el.getObjectValue() : null;
    }

    private void cache(String documentId, String document) {
        getDocumentCache().put(new Element(documentId, document));
    }
}
