package model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PaymentEntry {
    private final StringProperty courseCode;
    private final IntegerProperty creditHours;
    private final DoubleProperty cost;  // creditHours * 975

    public PaymentEntry(String courseCode, int creditHours, double cost) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.creditHours = new SimpleIntegerProperty(creditHours);
        this.cost = new SimpleDoubleProperty(cost);
    }

    public String getCourseCode() {
        return courseCode.get();
    }
    public StringProperty courseCodeProperty() {
        return courseCode;
    }

    public int getCreditHours() {
        return creditHours.get();
    }
    public IntegerProperty creditHoursProperty() {
        return creditHours;
    }

    public double getCost() {
        return cost.get();
    }
    public DoubleProperty costProperty() {
        return cost;
    }
}