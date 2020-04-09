package com.shamray.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements Runnable {

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static boolean FATAL_ERROR = false;

    public static boolean isFatalError() {
        return FATAL_ERROR;
    }

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    public Thread getFlowThread() {
        return flowThread;
    }

    private Thread flowThread; //поток
    private final String flowName; //имя потока
    private FileManager fileManager;

    private static final String DIR_LOGES = "logs\\main_log.txt";
    private static final String DIR_FILE = "files";
    private static final String DIR_BACKUP = "backup";
    private static final Path LOG_Name = Paths.get(DIR_LOGES);
    private static final Path dir_PATH = Paths.get(DIR_FILE);
    private static final Path BACKUP = Paths.get(DIR_BACKUP);
    private Integer capacity = 10;
    private Integer size = 0;
    private static final String[] formats = {".txt",".rtf",".doc",".docx",".html",".pdf",".odt", ".log"};
    private String[] line = new String[capacity];
    private static String regex = "\\S+\\.[a-zA-Z]{3,4}";
    private final static Pattern fileName = Pattern.compile(regex);
    private final BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

    public Controller(String flowName, FileManager fileManager) {
        this.flowName = flowName;
        this.fileManager = fileManager;
    }

    public Controller(String flowName) {
        this.flowName = flowName;
    }

    @Override
    public void run() {
//        while(fileManager.getFlowThread().isAlive()) {
            getAllLines();
            controll();
            if (FATAL_ERROR){
                System.out.println("НЕСАНКЦИОНИРОВАННЫЕ ИЗМЕНЕНИЕ ФАЙЛОВ");
                System.out.println("Желаете восстановить файлы из бэкапа?");
                System.out.println("( y / n ) ");
                try {
                    String y = buffer.readLine();
                    if (y.equals("y")){
                        Stream<Path> files = Files.walk(dir_PATH);
                        Stream<Path> backup = Files.walk(BACKUP);
                        files.forEach(x->{
                            if (!x.getFileName().toString().equals("files")){
                                try {
                                    Files.delete(x);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        backup.forEach(x -> {
                            try {
                                if (!x.getFileName().toString().equals("backup")) {
                                    Files.copy(x, dir_PATH.resolve(x.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                FATAL_ERROR = false;
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        }
    }
    private void getAllLines() {
        this.size = 0;
        String tmp;
        BufferedReader in;
        try
        {
            in = new BufferedReader(new FileReader(String.valueOf(Controller.LOG_Name)));
            while ((tmp = in.readLine()) != null)
            {
                if (size + 1 >= capacity){
                    resize();
                }
                this.line[size++] = tmp;
            }
        } catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    private void resize(){
        Integer newCapacity = capacity + 10;
        String[] newLine = new String[newCapacity];
        System.arraycopy(line, 0, newLine, 0, line.length);
        capacity = newCapacity;
        line = newLine;
    }

    private void controll() {
        getAllLines();
        //============================Несанкционированное создание файла============================
        unauthorizedFileCreation();
        //==========================================================================================
        //=================================Несанкционированное удаление файлов======================
        unauthorizedFileDeletion();
        //==========================================================================================
    }
    private void unauthorizedFileDeletion() {
        Stream<Path> stream;
        Map<String, Integer> map = new LinkedHashMap<>();
        checkLog(map);
        try {
            stream = Files.walk(dir_PATH);
            List<Path> list = stream.collect(Collectors.toList());
            Map<String, Integer> newMap = new LinkedHashMap<>(map);
            map.forEach((k, v) -> {
                if (list.contains( Paths.get(DIR_FILE + "\\" + k) )){
                    if (v > 0){
                        newMap.remove(k);
                    }
                }
            });
            newMap.forEach((k,v) -> {
                if (v > 0){
                    System.out.println("unauthorizedFileDeletion ----->> " + k + " : " + v);
                    FATAL_ERROR = true;
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private void unauthorizedFileCreation() {
        Stream<Path> stream;
        Map<String, Integer> map = new LinkedHashMap<>();
        checkLog(map);
        try {
            stream = Files.walk(dir_PATH);
            stream.forEach(x->{
                if (!x.getFileName().toString().equals("files")) {
                    if (map.containsKey(x.getFileName().toString())) {
                        if (map.get(x.getFileName().toString()) == 0) {
                            System.out.println("unauthorizedFileCreation ----->> " + x);
                            FATAL_ERROR = true;
                        }
                    }
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        try {
            stream = Files.walk(dir_PATH);
            stream.forEach(x->{
                if (!x.getFileName().toString().equals("files")) {
                    for (int i = 0; i < line.length && line[i] != null; i++) {
                        if (line[i].contains(x.getFileName().toString())) {
                            flag.set(true);
                        }
                    }
                    if (!flag.get()) {
                        System.out.println("unauthorizedFileCreation ----->> " + x);
                        FATAL_ERROR = true;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void checkLog(Map<String, Integer> map) {
        for (int i = 0; i < line.length && line[i] != null; i++) {
            if (line[i].contains("[CREATE]")) {
                Matcher matcherFile = fileName.matcher(line[i]);
                if (matcherFile.find()) {
                    String file = line[i].substring(matcherFile.start(), matcherFile.end());
                    if (!map.containsKey(file)) {
                        map.put(file, 1);
                    }
                }
            } else if (line[i].contains("[DELETED]")) {
                Matcher matcherFile = fileName.matcher(line[i]);
                if (matcherFile.find()) {
                    String file = line[i].substring(matcherFile.start(), matcherFile.end());
                    map.put(file, (map.get(file) - 1));
                }
            } else if (line[i].contains("[RENAME]")) {
                Matcher matcherFile = fileName.matcher(line[i]);
                String[] file = new String[2];
                int iterator = 0;
                while (matcherFile.find()) {
                    file[iterator++] = line[i].substring(matcherFile.start(), matcherFile.end());
                }
                map.remove(file[0]);
                map.put(file[1], 1);
            }
        }
    }

    public void start() {
        System.out.println("Старт потока " + flowName);
        //если поток не создан, то создаем и запускаем
        if (flowThread == null) {
            flowThread = new Thread(this, flowName);
            flowThread.start();
        }
    }

/*    public static void main(String[] args) {
        Controller controller = new Controller("Control");
        controller.start();
    }*/
}
