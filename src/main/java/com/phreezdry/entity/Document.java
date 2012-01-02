package com.phreezdry.entity;
/**
 * @author petrovic May 28, 2010 7:43:26 PM
 */

import java.util.logging.Logger;

public class Document {
    private final Logger logger = Logger.getLogger(Document.class.getName());

    private String text;
    private String id;

    public Document() {
    }

    public Document(String text, String id) {
        this.text = text;
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
