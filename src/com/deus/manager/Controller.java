package com.deus.manager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller implements Runnable {
    private Thread flowThread; //поток
    private final String flowName; //имя потока
    private FileManager fileManager;
    final static private String DIR_CONTROLLER = "C:\\Users\\User\\IdeaProjects\\Logging\\src\\error\\controllers_log.txt";
    final static private Path LOG_CONTROLLER = Paths.get(DIR_CONTROLLER);

    private static final String DIR_LOGES = "C:\\Users\\User\\IdeaProjects\\Logging\\src\\logs\\log.txt";
    private static final String DIR_FILE = "C:\\Users\\User\\IdeaProjects\\Logging\\src\\files";
    private static final Path LOG_Name = Paths.get(DIR_LOGES);
    private static final Path dir_PATH = Paths.get(DIR_FILE);
    private Integer capacity = 10;
    private Integer size = 0;
    private static final String[] formats = {".txt",".rtf",".doc",".docx",".html",".pdf",".odt", ".log"};
    private String[] line = new String[capacity];
    private static String regex = "\\S+\\.[a-zA-Z]{3,4}";
    private final static Pattern fileName = Pattern.compile(regex);

    public Controller(String flowName, FileManager fileManager) {
        this.flowName = flowName;
        this.fileManager = fileManager;
    }

    private Controller(String flowName) {
        this.flowName = flowName;
    }

    @Override
    public void run() {
        while(fileManager.getFlowThread().isAlive()) {
            getAllLines();
            controll();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("y.MM.dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        StringBuilder wr = new StringBuilder(simpleDateFormat.format(calendar.getTime()));
        wr.append(" ");
        int wrStart = 20;
        int colvo = 0;

        getAllLines();

        checkErrorLog(wr, wrStart);

        for (String s : line) {
            if (s != null) {
                colvo++;
            }
        }
        reverseCheckErrorLog(wr, wrStart, colvo);

        //============================Несанкционированное создание файла============================
        unauthorizedFileCreation(wr, wrStart);
        //==========================================================================================
        //=================================Несанкционированное создание файла=======================
        /*try {
            Stream<Path> stream;
            stream = Files.walk(dir_PATH);
            int finalColvo = colvo;
            stream.forEach(x -> {
                if (!x.getFileName().toString().equals("files")) {
                    boolean del = false;
                    boolean crt = false;
                    for (int i = finalColvo - 1; i >= 0; i--) {
                        Matcher matcherFile = fileName.matcher(line[i]);
                        if (matcherFile.find()) {
                            String file = line[i].substring(matcherFile.start(), matcherFile.end());
                            if (x.getFileName().toString().contains(file)) {
                                if (line[i].contains("[DELETED]")) {
                                    del = true;
                                }
                                if (line[i].contains("[CREATE]")) {
                                    crt = true;
                                }
                            }
                        }
                    }
                    if (!del && !crt) {
                        wr.append("Несанкционированное создание файла ").append(x.getFileName().toString());
                        try {
                            Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        wr.delete(wrStart, wr.length());
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //==========================================================================================
        wr.delete(wrStart, wr.length());
        //=================================Несанкционированное удаление файлов======================
        unauthorizedFileDeletion2(wr, wrStart);
        //==========================================================================================
    }
    private void unauthorizedFileDeletion2(StringBuilder wr, int wrStart) {
        Stream<Path> stream;
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < line.length && line[i] != null; i++) {
            Matcher matcherFile = fileName.matcher(line[i]);
            if (matcherFile.find() && !line[i].contains("[MODIFIED]")) {
                String file = line[i].substring(matcherFile.start(), matcherFile.end());
                if(!map.containsKey(file)){
                    map.put(file, 0);
                }
                if(line[i].contains("[CREATE]") && line[i].contains(file)){
                    map.put(file, (map.get(file) + 1));
                }
                if(line[i].contains("[DELETED]") && line[i].contains(file)){
                    map.put(file, (map.get(file) - 1));
                }
            }
        }
        try {
            stream = Files.walk(dir_PATH);
            List<Path> list = stream.collect(Collectors.toList());
            Map<String, Integer> newMap = new LinkedHashMap<String, Integer>(map);
            map.forEach((k, v) -> {
                if (list.contains( Paths.get(DIR_FILE + "\\" + k) )){
                    if (v > 0){
                        newMap.remove(k);
                    }
                }
            });
            newMap.forEach((k,v) -> {
                if (v > 0){
                    wr.append(" Несанкционированное удаление файла ").append(k);
                    try {
                        Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wr.delete(wrStart, wr.length());
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    //старый метод
    private void unauthorizedFileDeletion(StringBuilder wr, int wrStart) {
        Stream<Path> stream;
        for (int i = 0; i < line.length && line[i] != null; i++) {
            Matcher matcherFile = fileName.matcher(line[i]);
            boolean del = false;
            if (matcherFile.find() && !line[i].contains("[MODIFIED]")) {
                String file = line[i].substring(matcherFile.start(), matcherFile.end());
                for (int i1 = i + 1; i1 < line.length && line[i1] != null; i1++) {
                    if (line[i1].contains("[DELETED]") && line[i1].contains(file)) {
                        del = true;
                    }
                    if (line[i1].contains("[CREATE]") && line[i1].contains(file)) {
                        del = false;
                    }
                }
                if (!del) {
                    try {
                        stream = Files.walk(dir_PATH);
                        AtomicLong quat = new AtomicLong();
                        stream.forEach(x -> {
                            if (!x.getFileName().toString().equals("files")) {
                                if (!x.getFileName().toString().equals(file))
                                    quat.getAndIncrement();
                            }
                        });
                        if (quat.get() == quantity()) {
                            wr.append("Несанкционированное удаление файла ").append(file);
                            try {
                                Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            wr.delete(wrStart, wr.length());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void reverseCheckErrorLog(StringBuilder wr, int wrStart, int colvo) {
        for (int i = colvo - 1; i > 0; i--) {
            if (line[i].contains("был создан.")) {
                if (line[i - 1].contains("[INFO]  запрос на создание файла.")) {
                    continue;
                } else {
                    wr.append(" Создание файла без соответствующего запроса");
                    try {
                        Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wr.delete(wrStart, wr.length());
                }
            }
            else if (line[i].contains("был удален.")) {
                if (line[i - 1].contains("[INFO]  запрос на удаление файла.")) {
                    continue;
                } else {
                    wr.append(" Удаление файла без соответствующего запроса");
                    try {
                        Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wr.delete(wrStart, wr.length());
                }
            }
            else if (line[i].contains("был изменен.")) {
                if (line[i - 1].contains("[INFO]  запрос на изменение.")) {
                    continue;
                } else {
                    wr.append(" Изменение файла без соответствующего запроса");
                    try {
                        Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wr.delete(wrStart, wr.length());
                }
            }

        }
    }

    private void checkErrorLog(StringBuilder wr, int wrStart) {
        for (int i = 0; i < line.length - 1 && line[i + 1] != null; i++) {
            if (line[i].contains("[INFO]  запрос на создание файла.")) {
                if (line[i + 1].contains("был создан.") || line[i + 1].contains("отмена создания файла.")) {
                    continue;
                } else {
                    wr.append("Ошибка на ").append(i).append(" строчке журнала [Файл не был создан!]");
                    try {
                        Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wr.delete(wrStart, wr.length());
                }
            }
            else if (line[i].contains("[INFO]  запрос на удаление файла.") || line[i + 1].contains("отмена удаления файла.")) {
                if (line[i + 1].contains("был удален.")) {
                    continue;
                } else {
                    wr.append("Ошибка на ").append(i).append(" строчке журнала [Файл не был удален!]");
                    try {
                        Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wr.delete(wrStart, wr.length());
                }
            }
            else if (line[i].contains("[INFO]  запрос на изменение.") || line[i + 1].contains("отмена изменения файла.")) {
                if (line[i + 1].contains("был изменен.")) {
                    continue;
                } else {
                    wr.append("Ошибка на ").append(i).append(" строчке журнала [Файл не был изменен!]");
                    try {
                        Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wr.delete(wrStart, wr.length());
                }
            }

        }
    }
    //Старый метод
    private void unauthorizedFileCreation(StringBuilder wr, int wrStart, int colvo) {
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (int i = colvo - 1; i >= 0; i--) {
            Stream<Path> stream;
            AtomicBoolean del = new AtomicBoolean(false);
            Matcher matcherFile = fileName.matcher(line[i]);
            try {
                stream = Files.walk(dir_PATH);
                int finalI = i;
                if (matcherFile.find() && !line[i].contains("[MODIFIED]")) {
                    String file = line[finalI].substring(matcherFile.start(), matcherFile.end());
                    if (!map.containsKey(file)) {
                        map.put(file, 0);
                    }
                    stream.forEach(x -> {
                        if (x.getFileName().toString().equals(file)) {
                            if (line[finalI].contains("[DELETED]")) {
                                del.set(true);
                                map.put(file, map.get(file) - 1);
                            }
                            if (line[finalI].contains("[CREATE]")) {
                                del.set(false);
                                map.put(file, map.get(file) + 1);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        map.forEach((k, v) -> {
            if (v == 0) {
                wr.append("Несанкционированное создание файла ").append(k);
                try {
                    Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wr.delete(wrStart, wr.length());
            }
        });
        wr.delete(wrStart, wr.length());
    }

    private void unauthorizedFileCreation(StringBuilder wr, int wrStart) {
        Stream<Path> stream;
        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < line.length && line[i] != null; i++) {
            Matcher matcherFile = fileName.matcher(line[i]);
            if (matcherFile.find() && !line[i].contains("[MODIFIED]")) {
                String file = line[i].substring(matcherFile.start(), matcherFile.end());
                if(!map.containsKey(file)){
                    map.put(file, 0);
                }
                if(line[i].contains("[CREATE]") && line[i].contains(file)){
                    map.put(file, (map.get(file) + 1));
                }
                if(line[i].contains("[DELETED]") && line[i].contains(file)){
                    map.put(file, (map.get(file) - 1));
                }
            }
        }
        try {
            stream = Files.walk(dir_PATH);
            List<Path> list = stream.collect(Collectors.toList());
            list.forEach(x->{
                if (!x.getFileName().toString().equals("files")) {
                    if (map.containsKey(x.getFileName().toString())) {
                        if (map.get(x.getFileName().toString()) == 0) {
                            wr.append("Несанкционированное создание файла ").append(x.getFileName().toString());
                            try {
                                Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            wr.delete(wrStart, wr.length());
                        }
                    } else {
                        wr.append("Несанкционированное создание файла ").append(x.getFileName().toString());
                        try {
                            Files.write(LOG_CONTROLLER, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        wr.delete(wrStart, wr.length());
                    }
                }
            });

        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private long quantity(){
        Stream<Path> stream = null;
        long quan = 0; //Директория
        try{
            stream = Files.walk(dir_PATH);
/*            stream.forEach(x -> {
                System.out.println(x.getFileName());
            });*/
            quan = stream.count();
        } catch (IOException e){
            e.printStackTrace();
        }
        return --quan;
    }

    public void start() {
        System.out.println("Thread running " + flowName);
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
