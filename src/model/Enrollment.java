package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Enrollment {
    private final IntegerProperty enrollmentId = new SimpleIntegerProperty();
    private final StringProperty studentId;
    private final StringProperty courseCode;
    private final StringProperty semester;
    private final StringProperty grade;

    public Enrollment() {
        this("", "", "", "");
    }

    public Enrollment(String studentId, String courseCode, String semester, String grade) {
        this.studentId = new SimpleStringProperty(studentId);
        this.courseCode = new SimpleStringProperty(courseCode);
        this.semester = new SimpleStringProperty(semester);
        this.grade = new SimpleStringProperty(grade);
    }

    public int getEnrollmentId() {
        return enrollmentId.get();
    }

    public void setEnrollmentId(int id) {
        this.enrollmentId.set(id);
    }

    public IntegerProperty enrollmentIdProperty() {
        return enrollmentId;
    }

    public String getStudentId() {
        return studentId.get();
    }

    public void setStudentId(String studentId) {
        this.studentId.set(studentId);
    }

    public StringProperty studentIdProperty() {
        return studentId;
    }

    public String getCourseCode() {
        return courseCode.get();
    }

    public void setCourseCode(String courseCode) {
        this.courseCode.set(courseCode);
    }

    public StringProperty courseCodeProperty() {
        return courseCode;
    }

    public String getSemester() {
        return semester.get();
    }

    public void setSemester(String semester) {
        this.semester.set(semester);
    }

    public StringProperty semesterProperty() {
        return semester;
    }

    public String getGrade() {
        return grade.get();
    }

    public void setGrade(String grade) {
        this.grade.set(grade);
    }

    public StringProperty gradeProperty() {
        return grade;
    }
}
