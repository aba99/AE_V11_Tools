package com.automic.api;

/**
 * Required credentials for logging in to the Automation Engine
 */
public class AeCredentials {

    private final int client;
    private final String password;
    private final String department;
    private final String user;

    public AeCredentials(int client, String user, String department, String password) {
        this.client = client;
        this.user = user.toUpperCase();
        this.department = department.toUpperCase();
        this.password = password;
    }

    public int getClient() {
        return client;
    }

    public String getPassword() {
        return password;
    }

    public String getUserAndDepartment() {
        return user + "/" + department;
    }

    public String getUser() {
        return user;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AeCredentials)) {
            return false;
        }
        AeCredentials that = (AeCredentials) o;
        return client == that.client &&
                department.equals(that.department) &&
                password.equals(that.password) &&
                user.equals(that.user);
    }

    @Override
    public int hashCode() {
        int result = client;
        result = 31 * result + password.hashCode();
        result = 31 * result + department.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }
}
