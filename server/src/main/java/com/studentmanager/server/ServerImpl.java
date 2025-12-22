package com.studentmanager.server;

import com.studentmanager.shared.*;
import javax.persistence.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.stream.Collectors;

public class ServerImpl extends UnicastRemoteObject implements StudentService {
    private EntityManagerFactory emf;

    protected ServerImpl() throws RemoteException {
        super();
        this.emf = Persistence.createEntityManagerFactory("StudentPU");
    }

    @Override
    public List<StudentDTO> getAllStudents() throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            // Pobieramy czystą listę studentów bez joinowania ocen (bo Student nie ma pola grades)
            List<Student> students = em.createQuery("SELECT s FROM Student s", Student.class)
                    .getResultList();
            
            return students.stream()
                    .map(s -> new StudentDTO(s.getId(), s.getFirstName(), s.getLastName(), s.getIndexNumber()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<GradeDTO> getGradesForStudent(Long studentId) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            // Pobieramy oceny, gdzie student.id == studentId
            // Używamy JOIN FETCH g.course, aby od razu mieć nazwę przedmiotu
            TypedQuery<Grade> query = em.createQuery(
                "SELECT g FROM Grade g JOIN FETCH g.course WHERE g.student.id = :sid", Grade.class);
            query.setParameter("sid", studentId);
            List<Grade> grades = query.getResultList();

            return grades.stream()
                    .map(g -> new GradeDTO(g.getId(), g.getCourse().getName(), g.getValue()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public void addStudent(StudentDTO dto) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Student student = new Student(dto.getFirstName(), dto.getLastName(), dto.getIndexNumber());
            em.persist(student);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Błąd dodawania studenta: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public void removeStudent(Long studentId) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Student s = em.find(Student.class, studentId);
            if (s != null) {
                em.remove(s);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void addCourse(String courseName) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<Course> existing = em.createQuery("SELECT c FROM Course c WHERE c.name = :name", Course.class)
                    .setParameter("name", courseName)
                    .getResultList();
            
            if (existing.isEmpty()) {
                em.persist(new Course(courseName));
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public void addGrade(Long studentId, String courseName, int gradeValue) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Student s = em.find(Student.class, studentId);
            if (s == null) throw new IllegalArgumentException("Student nie istnieje");

            // Znajdź lub stwórz kurs
            Course course;
            try {
                course = em.createQuery("SELECT c FROM Course c WHERE c.name = :name", Course.class)
                        .setParameter("name", courseName)
                        .getSingleResult();
            } catch (NoResultException e) {
                course = new Course(courseName);
                em.persist(course);
            }

            Grade grade = new Grade((double) gradeValue, s, course);
            em.persist(grade);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RemoteException("Błąd dodawania oceny (możliwy duplikat): " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public void removeGrade(Long studentId, String courseName) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("DELETE FROM Grade g WHERE g.student.id = :sid AND g.course.name = :cname");
            q.setParameter("sid", studentId);
            q.setParameter("cname", courseName);
            q.executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}