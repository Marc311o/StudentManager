package com.studentmanager.server;

import com.studentmanager.shared.StudentService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "localhost");
            Registry registry = LocateRegistry.createRegistry(1099);
            StudentService service = new ServerImpl();
            registry.rebind("StudentService", service);
            System.out.println("Serwer gotowy i nasłuchuje na porcie 1099...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}