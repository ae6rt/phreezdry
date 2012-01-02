package com.phreezdry.util;

/**
 * @author petrovic May 25, 2010 4:24:34 PM
 */

public class Platform {

    private static final String UNIT_TEST = "unit.test";

    public static boolean isUnitTest() {
        return Boolean.valueOf(System.getProperty(UNIT_TEST, "false"));
    }

    public static void setUnitTest(boolean f) {
        System.setProperty(UNIT_TEST, Boolean.toString(f));
    }
}
