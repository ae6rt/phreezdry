package com.phreezdry.persistence;
/**
 * @author petrovic May 28, 2010 10:00:52 PM
 */

import java.util.logging.*;

public class PersistenceException extends Exception {
    public PersistenceException(Exception e) {
        super(e);
    }
}
