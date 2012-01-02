package com.phreezdry.server.rest;
/**
 * @author petrovic May 25, 2010 3:07:05 PM
 */

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;
import com.phreezdry.util.Constants;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

public class TestDocuments extends RestTestCase {

    private final String DRY_RESOURCE = "/services/dry";
    private final String FAUX_HOST = "http://foo.org";
    private final String dryUrl = String.format("%s%s", FAUX_HOST, DRY_RESOURCE);
    private final String TEST_DOCUMENT = "HELLO FREEZE";

    @Before
    public void init() throws S3ServiceException {
        String testBucketName = String.format("test.%s", config.getDocBucketName());
        S3Object[] objects = s3Service.listObjects(new S3Bucket(testBucketName));
        for (S3Object o : objects) {
            s3Service.deleteObject(testBucketName, o.getKey());
        }
        s3Service.getOrCreateBucket(testBucketName);
    }

    @Test
    public void testPut() throws IOException, SAXException {
        PutRequest putRequest = new PutRequest(dryUrl, new Form("document", TEST_DOCUMENT));
        ServletUnitClient client = authenticatedClient("biff@test.org", "tomtom");
        WebResponse response = client.getResponse(putRequest);
        Assert.assertEquals("expected 201 Created", 201, response.getResponseCode());

        GetMethodWebRequest getMethodWebRequest = new GetMethodWebRequest(response.getHeaderField("Location"));
        response = client.getResponse(getMethodWebRequest);
        String entity = response.getText();
        Assert.assertEquals("expected documents to be equal", TEST_DOCUMENT, entity);
    }

    @Test
    public void testPutNoDocument() throws IOException, SAXException {
        PutRequest putRequest = new PutRequest(dryUrl, new Form("document", ""));
        ServletUnitClient client = authenticatedClient("biff@test.org", "tomtom");
        expectFail(client, putRequest, 400);
    }

    @Test
    public void testPutDocumentTooLarge() throws IOException, SAXException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Constants.MAXDOCSIZE + 1; ++i) {
            sb.append("a");
        }
        PutRequest putRequest = new PutRequest(dryUrl, new Form("document", sb.toString()));
        ServletUnitClient client = authenticatedClient("biff@test.org", "tomtom");
        expectFail(client, putRequest, 400);
    }

}
