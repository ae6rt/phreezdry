package com.phreezdry.server.rest;
/**
 * @author petrovic May 25, 2010 3:17:08 PM
 */

import com.meterware.httpunit.*;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.meterware.servletunit.WebApplication;
import com.phreezdry.entity.User;
import com.phreezdry.persistence.PersistenceManager;
import com.phreezdry.server.Config;
import com.phreezdry.util.Constants;
import com.phreezdry.util.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class RestTestCase {
    private final Logger logger = Logger.getLogger(RestTestCase.class.getName());

    protected ServletRunner servletRunner;
    protected ServletContext servletContext;
    protected Config config;
    protected final static String APPLICATION_FORMENCODED = "application/x-www-form-urlencoded";
    protected final String FAUX_HOST = "http://foo.org";
    protected ApplicationContext appContext;
    protected PersistenceManager pm;

    private String originalTmpDir;
    private final String TMPDIR_PROPERTY = "java.io.tmpdir";
    private String tmpDir;

    @Before
    public void setUp() throws Exception {
        originalTmpDir = System.getProperty(TMPDIR_PROPERTY);
        Platform.setTestEnvironment(true);

        // Force ehcache to occupy a unique per-test directory
        tmpDir = String.format("%s/%s-%d", originalTmpDir, "phreezdry-unittest-", System.nanoTime());
//        System.out.println("tmpDir: " + tmpDir);
        System.setProperty(TMPDIR_PROPERTY, tmpDir);
        servletRunner = makeServletRunner();
        config = (Config) servletContext.getAttribute(Constants.CONFIG);
        appContext = (ApplicationContext) servletContext.getAttribute(Constants.CONTEXT);
        pm = (PersistenceManager) appContext.getBean(Constants.PERSISTENCE_MGR);
    }

    @After
    public void shutDown() {
        servletRunner.shutDown();
        System.setProperty(TMPDIR_PROPERTY, originalTmpDir);
        try {
            FileUtils.deleteDirectory(new File(tmpDir));
        } catch (IOException e) {
            logger.log(Level.WARNING, "cannot delete tmpDir", e);
        }
        Platform.setTestEnvironment(false);
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

    protected ServletUnitClient authenticatedClient(User u) {
        return authenticatedClient(u.getEmail(), u.getPassword());
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
