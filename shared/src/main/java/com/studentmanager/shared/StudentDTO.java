package com.studentmanager.shared;

import java.io.Serializable;

public class StudentDTO implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String indexNumber;

    public StudentDTO() {}

    public StudentDTO(Long id, String firstName, String lastName, String indexNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.indexNumber = indexNumber;
    }

    // Gettery i Settery
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getIndexNumber() { return indexNumber; }
    
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
