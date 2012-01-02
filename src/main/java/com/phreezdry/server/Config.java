package com.phreezdry.server;
/**
 * @author petrovic May 22, 2010 2:26:22 PM
 */

public class Config {
    private String svnRevision;
    private String buildDate;
    private String redirectUrl;

    public Config() {
    }

    public void init() {
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getSvnRevision() {
        return svnRevision;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setSvnRevision(String svnRevision) {
        this.svnRevision = svnRevision;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }
}
