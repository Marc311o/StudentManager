package com.studentmanager.shared;

import java.io.Serializable;

public class GradeDTO implements Serializable {
    private Long id;
    private String courseName;
    private Double value;

    public GradeDTO(Long id, String courseName, Double value) {
        this.id = id;
        this.courseName = courseName;
        this.value = value;
    }

    public Long getId() { return id; }
    public String getCourseName() { return courseName; }
    public Double getValue() { return value; }
    
    @Override
    public String toString() {
        return courseName + ": " + value;
    }
}
