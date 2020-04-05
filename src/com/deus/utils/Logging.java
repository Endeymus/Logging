package com.deus.utils;

import com.deus.manager.FileManager;

import java.io.*;

public class Logging {
    public static void main(String[] args) {
        FileManager fileManager = new FileManager("fileManager");
        fileManager.start();
    }
}

