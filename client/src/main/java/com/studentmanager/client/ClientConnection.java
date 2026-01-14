package com.studentmanager.client;

import com.studentmanager.shared.StudentService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Klasa zarządzająca połączeniem RMI z serwerem.
 * <p>
 * Odpowiada za wyszukanie zdalnego obiektu {@link StudentService} w rejestrze RMI.
 * </p>
 */
public class ClientConnection {
    
    private static StudentService service;

    /**
     * Nawiązuje połączenie z lokalnym rejestrem RMI na porcie 1099.
     * Metoda szuka usługi o nazwie "StudentService".
     */
    public static void connect() {
        try {
            // Wyszukujemy rejestr na localhost:1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            // Pobieramy stub (zdalny interfejs)
            service = (StudentService) registry.lookup("StudentService");
            
            System.out.println("Pomyślnie połączono z serwerem RMI.");
        } catch (Exception e) {
            System.err.println("Krytyczny błąd: Nie można połączyć z serwerem RMI.");
            e.printStackTrace();
            // Opcjonalnie: Rzucamy RuntimeException, aby zatrzymać aplikację jeśli serwer nie działa
            throw new RuntimeException("Brak połączenia z serwerem", e);
        }
    }

    /**
     * Zwraca instancję zdalnego serwisu.
     * <p>
     * Jeśli połączenie nie zostało wcześniej nawiązane (zmienna service jest null),
     * metoda automatycznie spróbuje wywołać {@link #connect()}.
     * </p>
     *
     * @return obiekt implementujący interfejs {@link StudentService}.
     */
    public static StudentService getService() {
        if (service == null) {
            System.out.println("Połączenie nieaktywne. Próba nawiązania połączenia...");
            connect();
        }
        return service;
    }
}