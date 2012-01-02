package com.phreezdry.entity;

/**
 * @author petrovic May 21, 2010 8:43:18 PM
 */

public class User {
    private String email;
    private String password;

    public User(String u, String password) {
        this.email = u;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
