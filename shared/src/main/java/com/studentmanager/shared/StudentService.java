package com.studentmanager.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface StudentService extends Remote {
    List<StudentDTO> getAllStudents() throws RemoteException;
    
    // Metoda do pobierania ocen dla konkretnego studenta
    List<GradeDTO> getGradesForStudent(Long studentId) throws RemoteException;

    void addStudent(StudentDTO student) throws RemoteException;
    void removeStudent(Long studentId) throws RemoteException;
    
    void addCourse(String courseName) throws RemoteException;
    void addGrade(Long studentId, String courseName, int gradeValue) throws RemoteException;
    void removeGrade(Long studentId, String courseName) throws RemoteException;
}