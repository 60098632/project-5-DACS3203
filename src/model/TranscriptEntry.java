package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TranscriptEntry {
    private final StringProperty courseCode;
    private final StringProperty courseName;
    private final IntegerProperty creditHours;
    private final StringProperty grade;

    public TranscriptEntry(String courseCode, String courseName, int creditHours, String grade) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.courseName = new SimpleStringProperty(courseName);
        this.creditHours = new SimpleIntegerProperty(creditHours);
        this.grade = new SimpleStringProperty(grade);
    }

    public String getCourseCode() {
        return courseCode.get();
    }
    public StringProperty courseCodeProperty() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName.get();
    }
    public StringProperty courseNameProperty() {
        return courseName;
    }

    public int getCreditHours() {
        return creditHours.get();
    }
    public IntegerProperty creditHoursProperty() {
        return creditHours;
    }

    public String getGrade() {
        return grade.get();
    }
    public StringProperty gradeProperty() {
        return grade;
    }
}