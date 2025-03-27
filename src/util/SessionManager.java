package util;

/**
 * A simple session management class to store the currently logged-in user's info.
 * Used globally across controllers to check role and identity.
 */
public class SessionManager {

    // Static fields to hold current session info
    private static String userName;
    private static String userRole;
    private static String studentId;

    /**
     * Sets the current logged-in user's info.
     * @param name The user's full name.
     * @param role The user's role (e.g. student, admin, instructor).
     * @param id   The user's student ID.
     */
    public static void setCurrentUser(String name, String role, String id) {
        userName = name;
        userRole = role;
        studentId = id;
    }



    /**
     * @return Current user's name.
     */
    public static String getUserName() {
        return userName;
    }

    /**
     * @return Current user's role (e.g. student, admin, instructor).
     */
    public static String getUserRole() {
        return userRole;
    }

    /**
     * @return Current user's student ID.
     */
    public static String getStudentId() {
        return studentId;
    }

    /**
     * Clears the session (used on logout).
     */
    public static void clearSession() {
        userName = null;
        userRole = null;
        studentId = null;
    }

}