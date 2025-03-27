package model;

public class GradeEntry {
    private String studentId;
    private String studentName;
    private String courseCode;
    private String grade;

    public GradeEntry(String studentId, String studentName, String courseCode, String grade) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseCode = courseCode;
        this.grade = grade;
    }

    // Getters and setters
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getCourseCode() { return courseCode; }
    public String getGrade() { return grade; }

    public void setGrade(String grade) { this.grade = grade; }
}