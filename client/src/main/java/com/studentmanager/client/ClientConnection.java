package com.studentmanager.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.studentmanager.shared.StudentService;

public class ClientConnection {
    private static StudentService service;

    public static void connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        service = (StudentService) registry.lookup("StudentService");
    }

    public static StudentService getService() {
        return service;
    }
}
