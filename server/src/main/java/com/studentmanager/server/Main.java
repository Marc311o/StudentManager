package com.studentmanager.server;

import com.studentmanager.shared.StudentService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * The entry point for the Student Manager Server application.
 * <p>
 * This class is responsible for bootstrapping the RMI infrastructure. It initializes the RMI registry,
 * instantiates the service implementation, and binds the remote object so it can be discovered
 * by client applications.
 * </p>
 */
public class Main {

    /**
     * The hostname or IP address that the RMI server will export to clients.
     */
    final private static String ip = "localhost";
    /**
     * The network port on which the RMI registry listens for incoming requests.
     * <p>
     * The default RMI port is 1099.
     * </p>
     */
    final private static int port = 1099;

    /**
     * The main execution method that starts the server.
     * <p>
     * This method performs the following initialization steps:
     * <ol>
     * <li>Sets the {@code java.rmi.server.hostname} system property to ensure correct callback addressing.</li>
     * <li>Creates a new RMI registry on the configured port.</li>
     * <li>Instantiates the {@link ServerImpl}, which triggers the database connection via JPA.</li>
     * <li>Binds (or rebinds) the service instance to the name "StudentService" in the registry.</li>
     * </ol>
     * </p>
     */
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", ip);
            Registry registry = LocateRegistry.createRegistry(port);
            StudentService service = new ServerImpl();
            registry.rebind("StudentService", service);
            System.out.println("Serwer gotowy i nasłuchuje na porcie 1099...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}