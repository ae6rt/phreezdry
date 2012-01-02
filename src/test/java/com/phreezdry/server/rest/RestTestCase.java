package com.phreezdry.server.rest;
/**
 * @author petrovic May 25, 2010 3:17:08 PM
 */

import com.meterware.httpunit.*;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.meterware.servletunit.WebApplication;
import com.phreezdry.server.Config;
import com.phreezdry.util.Constants;
import com.phreezdry.util.Platform;
import org.apache.commons.io.IOUtils;
import org.jets3t.service.S3Service;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class RestTestCase {
    protected ServletRunner servletRunner;
    protected ServletContext servletContext;
    protected Config config;
    protected final static String APPLICATION_FORMENCODED = "application/x-www-form-urlencoded";
    protected final String FAUX_HOST = "http://foo.org";

    protected S3Service s3Service;
    private String originalTmpDir;
    private final String tmpDirProperty = "java.io.tmpdir";

    @Before
    public void setUp() throws Exception {
        originalTmpDir = System.getProperty(tmpDirProperty);
        Platform.setUnitTest(true);

        // Force ehcache to occupy a unique per-test directory
        System.setProperty(tmpDirProperty, String.format("%s-%d", originalTmpDir, System.nanoTime()));
        new File(System.getProperty(tmpDirProperty)).deleteOnExit();
        servletRunner = makeServletRunner();
        config = (Config) servletContext.getAttribute(Constants.CONFIG);
        s3Service = (S3Service) servletContext.getAttribute(Constants.S3_SERVICE);
    }

    @After
    public void shutDown() {
        servletRunner.shutDown();
        System.setProperty(tmpDirProperty, originalTmpDir);
        Platform.setUnitTest(false);
    }

    public ServletRunner makeServletRunner() throws IOException, SAXException {
        String webXmlString = IOUtils.toString(RestTestCase.class.getResourceAsStream("/servletunit-web.xml"));
        ServletRunner servletRunner = new ServletRunner(new ByteArrayInputStream(webXmlString.getBytes()));
        servletContext = servletRunner.getContext()._servletContext;
        WebApplication wa = servletRunner.getApplication();
        wa._useBasicAuthentication = true;
        return servletRunner;
    }

    protected ServletUnitClient anonymousClient() {
        return servletRunner.newClient();
    }

    protected ServletUnitClient authenticatedClient(String username, String password) {
        ServletUnitClient client = anonymousClient();
        client.setAuthorization(username, password);
        return client;
    }

    protected class PutRequest extends PutMethodWebRequest {
        public PutRequest(String url, Form f) {
            super(url, f.inputStream(), APPLICATION_FORMENCODED);
        }

        public PutRequest(String url) {
            this(url, new Form());
        }
    }

    protected class GetDocumentRequest extends GetMethodWebRequest {
        public GetDocumentRequest(String key) {
            super(String.format("http://foo.bar.com/services/dry/%s", key));
        }
    }

    protected void expectFail(ServletUnitClient sc, WebRequest request, int responseCode) throws IOException, SAXException {
        try {
            WebResponse response = sc.getResponse(request);
            String r = response.getText();
            System.out.println("@@@ this should not be printed: " + r);
            Assert.assertTrue(false);
        } catch (HttpException e) {
            Assert.assertEquals("Expecting response code", responseCode, e.getResponseCode());
            Assert.assertTrue(true);
        }
    }

    protected class Form {
        Map<String, String> params = new HashMap<String, String>();

        public Form() {

        }

        public Form(String key, String value) {
            params.put(key, value);
        }

        public void add(String key, String value) {
            params.put(key, value);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String key : params.keySet()) {
                sb.append(key).append('=').append(params.get(key)).append('&');
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }

        public InputStream inputStream() {
            return new ByteArrayInputStream(toString().getBytes());
        }
    }
}
