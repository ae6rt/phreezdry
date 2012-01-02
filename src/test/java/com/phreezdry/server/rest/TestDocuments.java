package com.phreezdry.server.rest;
/**
 * @author petrovic May 25, 2010 3:07:05 PM
 */

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;
import com.phreezdry.entity.EntityType;
import com.phreezdry.entity.User;
import com.phreezdry.util.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import java.io.IOException;

public class TestDocuments extends RestTestCase {

    private final String DRY_RESOURCE = "/services/dry";
    private final String FAUX_HOST = "http://foo.org";
    private final String dryUrl = String.format("%s%s", FAUX_HOST, DRY_RESOURCE);
    private final String TEST_DOCUMENT = "HELLO FREEZE";
    private final String TEST_USER = "biff@test.org";
    private final String TEST_PASSWORD = "tomtom";
    private final User testUser = new User(TEST_USER, TEST_PASSWORD);

    @Before
    public void init() throws Exception {
        pm.removeAll(EntityType.DOCUMENT);
    }

    @Test
    public void testPut() throws IOException, SAXException {
        PutRequest putRequest = new PutRequest(dryUrl, new Form("document", TEST_DOCUMENT));
        ServletUnitClient client = authenticatedClient(testUser);
        WebResponse response = client.getResponse(putRequest);
        Assert.assertEquals("expected 201 Created", Response.Status.CREATED.getStatusCode(), response.getResponseCode());

        GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(response.getHeaderField("Location"));
        response = client.getResponse(getMethodWebRequest);
        String entity = response.getText();
        Assert.assertEquals("expected documents to be equal", TEST_DOCUMENT, entity);
    }

    @Test
    public void testPutNoDocument() throws IOException, SAXException {
        PutRequest putRequest = new PutRequest(dryUrl, new Form("document", ""));
        ServletUnitClient client = authenticatedClient(testUser);
        expectFail(client, putRequest, Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPutDocumentTooLarge() throws IOException, SAXException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.MAXDOCSIZE + 1; ++i) {
            sb.append("a");
        }
        PutRequest putRequest = new PutRequest(dryUrl, new Form("document", sb.toString()));
        ServletUnitClient client = authenticatedClient(testUser);
        expectFail(client, putRequest, Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getNoDocument() throws IOException, SAXException {
        ServletUnitClient client = anonymousClient();
        GetMethodWebRequest get = new GetMethodWebRequest(dryUrl);
        expectFail(client, get, 405);
    }

    @Test
    public void getDocumentIdTooShort() throws IOException, SAXException {
        ServletUnitClient client = anonymousClient();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.SHA1_LENGTH - 1; ++i) {
            sb.append("a");
        }
        GetMethodWebRequest get = new GetMethodWebRequest(String.format("%s/%s", dryUrl, sb.toString()));
        expectFail(client, get, Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void getDocumentIdTooLong() throws IOException, SAXException {
        ServletUnitClient client = anonymousClient();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.SHA1_LENGTH + 1; ++i) {
            sb.append("a");
        }
        GetMethodWebRequest get = new GetMethodWebRequest(String.format("%s/%s", dryUrl, sb.toString()));
        expectFail(client, get, Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void getDocumentInvalidIdChar() throws IOException, SAXException {
        ServletUnitClient client = anonymousClient();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.SHA1_LENGTH - 1; ++i) {
            sb.append("a");
        }

        // append a character not in the valid-id regex
        sb.append('z');

        GetMethodWebRequest get = new GetMethodWebRequest(String.format("%s/%s", dryUrl, sb.toString()));
        expectFail(client, get, Response.Status.NOT_FOUND.getStatusCode());
    }

}
