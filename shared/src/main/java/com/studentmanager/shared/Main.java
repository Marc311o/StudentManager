package com.studentmanager.shared;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {
    public static void main(String[] args) {
        // 1. Uruchomienie JPA
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("StudentPU");
        EntityManager em = emf.createEntityManager();

        try {
            // --- TEST 1: Dodawanie danych ---
            System.out.println("\n=== TEST 1: Dodawanie danych ===");
            em.getTransaction().begin();

            Student s1 = new Student("Jan", "Kowalski", "s12345");
            Course c1 = new Course("Matematyka");
            Course c2 = new Course("Fizyka");

            // Najpierw zapisujemy obiekty niezależne
            em.persist(s1);
            em.persist(c1);
            em.persist(c2);

            // Tworzymy ocenę
            Grade g1 = new Grade(4.5, s1, c1);
            Grade g2 = new Grade(3.5, s1, c2);
            em.persist(g1); // Zapisujemy ocenę
            em.persist(g2);

            em.getTransaction().commit();
            System.out.println("-> Udało się dodać studenta i pierwszą ocenę.");


            // --- TEST 2: Próba dodania DUPLIKATU oceny (powinien być błąd) ---
            System.out.println("\n=== TEST 2: Sprawdzenie UniqueConstraint ===");
            em.getTransaction().begin();
            try {
                // Próbujemy dodać DRUGĄ ocenę z Matematyki dla tego samego Jana
                Grade gDuplicate = new Grade(3.0, s1, c1); 
                em.persist(gDuplicate);
                em.getTransaction().commit();
                
                System.out.println("BŁĄD! Baza pozwoliła dodać duplikat!");
            } catch (Exception e) {
                System.out.println("-> SUKCES! Baza zablokowała duplikat. Komunikat: " + e.getMessage());
                // Musimy zrollbackować transakcję, bo wystąpił błąd
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}