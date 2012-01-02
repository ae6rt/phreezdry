package com.phreezdry.server;
/**
 * @author petrovic May 22, 2010 2:26:22 PM
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    private final Logger logger = Logger.getLogger(Config.class.getName());
    private final String phreezResource = "/phreez.properties";
    private final String buildResource = "/build.properties";
    private String awsKey;
    private String awsSecret;
    private String docBucketName;
    private String userBucketName;
    private String svnRevision;
    private String buildDate;
    private String redirectUrl;

    public Config() {
        Properties props = new Properties();
        try {
            InputStream is = getClass().getResourceAsStream(phreezResource);
            props.load(is);
            awsKey = props.getProperty("key");
            awsSecret = props.getProperty("secret");
            userBucketName = props.getProperty("bucket.user");
            docBucketName = props.getProperty("bucket.doc");
            redirectUrl = props.getProperty("redirectUrl");
            is.close();

            is = getClass().getResourceAsStream(buildResource);
            props.load(is);
            svnRevision = props.getProperty("svn.revision");
            buildDate = props.getProperty("build.date");
            is.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("cannot load properties"), e);
        }
    }

    public String getAwsKey() {
        return awsKey;
    }

    public String getAwsSecret() {
        return awsSecret;
    }

    public String getDocBucketName() {
        return docBucketName;
    }

    public String getUserBucketName() {
        return userBucketName;
    }

    public String getSvnRevision() {
        return svnRevision;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
