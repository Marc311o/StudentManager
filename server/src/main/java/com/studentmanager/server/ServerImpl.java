package com.studentmanager.server;

import com.studentmanager.shared.*;
import javax.persistence.*;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Implementation of the remote {@link StudentService} interface.
 * <p>
 * This class serves as the RMI server backend, handling business logic and database interactions
 * via JPA (Hibernate). It manages the lifecycle of {@link Student}, {@link Course}, and {@link Grade} entities.
 * </p>
 * <p>
 * Database credentials are loaded dynamically from a {@code db.properties} file located in the
 * resources folder, ensuring sensitive data is not hardcoded in the {@code persistence.xml}.
 * </p>
 */
public class ServerImpl extends UnicastRemoteObject implements StudentService {

    /**
     * Factory for creating {@link EntityManager} instances to interact with the persistence context.
     */
    private EntityManagerFactory emf;

    /**
     * Constructs the server implementation and initializes the JPA EntityManagerFactory.
     * <p>
     * This constructor attempts to load database connection properties (URL, user, password)
     * from a {@code db.properties} file found in the classpath. These properties override
     * the default settings defined in {@code persistence.xml}.
     * </p>
     *
     * @throws RemoteException if the RMI object export fails.
     */
    protected ServerImpl() throws RemoteException {
        super();
        Properties fileProps = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("Nie znaleziono db.properties - upewnij się, że plik jest w resources.");
                return;
            }
            fileProps.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> jpaProps = new HashMap<>();


        jpaProps.put("javax.persistence.jdbc.url", fileProps.getProperty("db.url"));
        jpaProps.put("javax.persistence.jdbc.user", fileProps.getProperty("db.user"));
        jpaProps.put("javax.persistence.jdbc.password", fileProps.getProperty("db.password"));

        this.emf = Persistence.createEntityManagerFactory("StudentPU", jpaProps);
    }

    /**
     * Retrieves a list of all students currently stored in the database.
     * <p>
     * The method converts persistent {@link Student} entities into lightweight {@link StudentDTO}
     * objects to ensure safe transmission over RMI.
     * </p>
     *
     * @return a list of {@link StudentDTO} objects representing all students.
     * @throws RemoteException if a communication error occurs during the RMI call.
     */
    @Override
    public List<StudentDTO> getAllStudents() throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {

            List<Student> students = em.createQuery("SELECT s FROM Student s", Student.class)
                    .getResultList();
            
            return students.stream()
                    .map(s -> new StudentDTO(s.getId(), s.getFirstName(), s.getLastName(), s.getIndexNumber()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all grades assigned to a specific student.
     * <p>
     * This method uses a {@code JOIN FETCH} query to efficiently retrieve the associated
     * {@link Course} information for each grade in a single database query.
     * </p>
     *
     * @param studentId the unique identifier of the student.
     * @return a list of {@link GradeDTO} objects containing course names and grade values.
     * @throws RemoteException if a communication error occurs during the RMI call.
     */
    @Override
    public List<GradeDTO> getGradesForStudent(Long studentId) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
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

    /**
     * Adds a new student to the database.
     *
     * @param dto the {@link StudentDTO} object containing the new student's details.
     * @throws RemoteException if the transaction fails or a database error occurs (e.g., duplicate index number).
     */
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

    /**
     * Removes a student from the database based on their ID.
     * <p>
     * Due to the {@code CASCADE} settings in the entity model, removing a student
     * will also automatically remove all associated grades.
     * </p>
     *
     * @param studentId the unique identifier of the student to be removed.
     * @throws RemoteException if a communication or database error occurs.
     */
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

    /**
     * Adds a new course to the database if it does not already exist.
     * <p>
     * Checks for a course with the given name. If found, no action is taken.
     * If not found, a new {@link Course} entity is persisted.
     * </p>
     *
     * @param courseName the name of the course to add.
     * @throws RemoteException if a communication or database error occurs.
     */
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

    /**
     * Assigns a grade to a student for a specific course.
     * <p>
     * This method handles several logic steps:
     * <ol>
     * <li>Verifies the student exists.</li>
     * <li>Checks if the course exists; if not, it creates a new {@link Course} automatically.</li>
     * <li>Persists the new {@link Grade}.</li>
     * </ol>
     * </p>
     *
     * @param studentId   the unique identifier of the student.
     * @param courseName  the name of the course.
     * @param gradeValue  the numeric value of the grade.
     * @throws RemoteException if the student is not found, or if a unique constraint violation occurs (e.g., duplicate grade for the same course).
     * @throws IllegalArgumentException if the student with the given ID does not exist.
     */
    @Override
    public void addGrade(Long studentId, String courseName, int gradeValue) throws RemoteException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Student s = em.find(Student.class, studentId);
            if (s == null) throw new IllegalArgumentException("Student nie istnieje");

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

    /**
     * Removes a specific grade for a student based on the course name.
     *
     * @param studentId  the unique identifier of the student.
     * @param courseName the name of the course for which the grade should be removed.
     * @throws RemoteException if a communication or database error occurs.
     */
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