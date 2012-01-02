package com.phreezdry.server.rest;
/**
 * @author petrovic May 25, 2010 6:16:12 PM
 */

import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

public class TestAdmin extends RestTestCase {

    private final String ADMIN_RESOURCE = "/services/admin";
    private final String adminUrl = String.format("%s%s", FAUX_HOST, ADMIN_RESOURCE);

    @Test
    public void testDumpCache() throws IOException, SAXException {
        PutRequest putRequest = new PutRequest(String.format("%s/dumpstats", adminUrl), new Form("key", "whatever"));
        ServletUnitClient client = this.anonymousClient();
        WebResponse response = client.getResponse(putRequest);
        Assert.assertEquals("expected 200 OK", 200, response.getResponseCode());
        String entity = response.getText();
        Assert.assertTrue("contains cacheHits expected", entity.contains("cacheHits"));
    }

    @Test
    public void testBuildInfo() throws IOException, SAXException {
        PutRequest putRequest = new PutRequest(String.format("%s/buildInfo", adminUrl), new Form("key", "whatever"));
        ServletUnitClient client = this.anonymousClient();
        WebResponse response = client.getResponse(putRequest);
        Assert.assertEquals("expected 200 OK", 200, response.getResponseCode());
        String entity = response.getText();
        Assert.assertTrue("contains SVN expected", entity.contains("SVN"));
    }

}
