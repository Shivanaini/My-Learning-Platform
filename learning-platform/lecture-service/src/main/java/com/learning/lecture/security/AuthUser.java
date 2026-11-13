package com.learning.lecture.security;

/** The logged-in user, extracted from the JWT. */
public record AuthUser(String username, String fullName, String role) {

    public static final String REQUEST_ATTRIBUTE = "authUser";

    public boolean canManageLectures() {
        return "INSTRUCTOR".equals(role);
    }
}
