package model;

/** Represents a user in the system (with role and hashed password). */
public class User {
    private String id;         // University ID (e.g., "60xxxxxx")
    private String name;
    private String email;
    private String role;       // "student", "instructor", or "admin"
    private String passwordHash;

    public User(String id, String name, String email, String role, String passwordHash) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    // Getters:
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }

    // Setters for mutable fields:
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(String role) { this.role = role; }
}