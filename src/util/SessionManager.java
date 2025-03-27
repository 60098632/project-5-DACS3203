package util;

/** Manages the session info for the currently logged-in user. */
public class SessionManager {
    private static String userName;
    private static String userRole;
    private static String studentId;

    /** Sets the current user's name, role, and ID in session. */
    public static void setCurrentUser(String name, String role, String id) {
        userName = name;
        userRole = role;
        studentId = id;
    }

    public static String getUserName()   { return userName; }
    public static String getUserRole()   { return userRole; }
    public static String getStudentId()  { return studentId; }

    /** Clears the session (e.g., on logout). */
    public static void clearSession() {
        userName = null;
        userRole = null;
        studentId = null;
    }
}