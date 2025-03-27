package model;

public class Course {
    private String courseCode;
    private String courseName;
    private int creditHours;
    private String instructorName;
    private String courseDescription;

    public Course(String courseCode, String courseName, int creditHours,
                  String instructorName, String courseDescription) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.creditHours = creditHours;
        this.instructorName = instructorName;
        this.courseDescription = courseDescription;
    }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCreditHours() { return creditHours; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}