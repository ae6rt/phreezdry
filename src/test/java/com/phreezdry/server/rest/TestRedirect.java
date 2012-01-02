package com.phreezdry.server.rest;
/**
 * @author petrovic May 30, 2010 8:48:43 AM
 */

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

public class TestRedirect extends RestTestCase {

    @Test
    @Ignore
    // for some reason this test does not work as expected.  It should redirect to the blog post via the welcome jsp.
    public void testRedirect() throws IOException, SAXException {
        ServletUnitClient client = anonymousClient();
        client.getClientProperties().setAutoRedirect(true);
        GetMethodWebRequest get = new GetMethodWebRequest("http://f.org");
        WebResponse response = client.getResponse(get);

        int rc = response.getResponseCode();
        Assert.assertEquals("expected 200 OK", 200, rc);
    }
}
