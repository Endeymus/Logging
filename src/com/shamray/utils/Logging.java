package com.shamray.utils;

import com.shamray.manager.FileManager;

public class Logging {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager("fileManager");
        fileManager.start();
        System.out.println("yay");
    }
}
