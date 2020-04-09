package com.shamray.manager;

import com.shamray.utils.Loges;
import com.shamray.utils.STATE;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FileManager implements Runnable {
    private Thread flowThread; //поток
    private String flowName; //имя потока

    private static final String DIR_LOGES = "logs\\log.txt";
    private static final String DIR_LOGES_MAIN = "logs\\main_log.txt";
    private static final String DIR_FILE = "files";
    private static final String DIR_BACKUP = "backup";
    private static final Path LOG_Name = Paths.get(DIR_LOGES);
    private static final Path LOG_Name_MAIN = Paths.get(DIR_LOGES_MAIN);
    private static final Path dir_PATH = Paths.get(DIR_FILE);
    private static final Path BACKUP = Paths.get(DIR_BACKUP);


    private static String regex = "\\S+\\.[a-zA-Z]{3,4}";
    private final static Pattern fileName = Pattern.compile(regex);
    private static BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
    private static final String[] formats = {".txt",".rtf",".doc",".docx",".html",".pdf",".odt", ".log"};
    private static Random rand = new Random(47);
    private static Integer R = rand.ints(0, 7).findAny().getAsInt();
    private static Integer percent = 1;

    public FileManager(String flowName) {
        this.flowName = flowName;
    }

    private static void menu(){
        System.out.println("1. Создать файл");
        System.out.println("2. Переименовать файл");
        System.out.println("3. Добавить текст в файл");
        System.out.println("4. Отобразить содержимое файла");
        System.out.println("5. Удалить файл");
        System.out.println("6. Завершение работы");
    }

    private void exec() throws IOException, InterruptedException{
        //------------------------TEST---------------------{

        //------------------------TEST---------------------}
        //----------------------------------Заускам контроллер------------------------------------------{
        Controller controller = new Controller("controller", this);
        controller.start();
        //----------------------------------------------------------------------------------------------}
        if (Files.notExists(dir_PATH)){
            Files.createDirectory(dir_PATH);
        }
        if (Files.notExists(BACKUP)){
            Files.createDirectory(BACKUP);
        }
        if(Files.notExists(LOG_Name)) {
            try {
                Files.createFile(LOG_Name);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if (Files.notExists(LOG_Name_MAIN)){
            try {
                Files.createFile(LOG_Name_MAIN);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if (Files.notExists(dir_PATH)){
            try {
                Files.createDirectory(dir_PATH);
            } catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
        controller.getFlowThread().join();

        Loges loges = new Loges();
        Thread.sleep(1000);
        String choose;
        String y;
        String lastLine = getLastLine();
        if (lastLine != null) {
            if (lastLine.contains("запрос на создание файла.")) {
                System.out.println("Продолжить работу с последнего незамершенного действия?");
                System.out.println("( y / n )");
                y = buffer.readLine();
                if ( y.equals("y")){
                    System.out.println("Последнее незавершенное действие: Создание файла.");
                    createLog(loges);
                }

            }
            else if (lastLine.contains("запрос на удаление файла.")) {
                System.out.println("Продолжить работу с последнего незамершенного действия?");
                System.out.println("( y / n )");
                y = buffer.readLine();
                if ( y.equals("y")){
                    System.out.println("Последнее незавершенное действие: Удаление файла.");
                    deleteLog(loges);
                }
            }
            else if (lastLine.contains("запрос на изменение файла.")) {
                System.out.println("Продолжить работу с последнего незамершенного действия?");
                System.out.println("( y / n )");
                y = buffer.readLine();
                if ( y.equals("y")){
                    System.out.println("Последнее незавершенное действие: Изменение файла.");
                    modifiedLog(loges);
                }
            }
            else if (lastLine.contains("запрос на переименование файла.")) {
                System.out.println("Продолжить работу с последнего незамершенного действия?");
                System.out.println("( y / n )");
                y = buffer.readLine();
                if ( y.equals("y")){
                    System.out.println("Последнее незавершенное действие: Переименовывание файла");
                    renameLog(loges);
                }
            }
            else if (lastLine.contains("запрос на открытие файла.")) {
                System.out.println("Продолжить работу с последнего незамершенного действия?");
                System.out.println("( y / n )");
                y = buffer.readLine();
                if ( y.equals("y")){
                    System.out.println("Последнее незавершенное действие: Вывод содержимого файла.");
                    contentLog(loges);
                }
            }
        }


        do {
            menu();
            System.out.print("Ваш выбор -> ");
            choose = buffer.readLine();
            switch (choose) {
                case "1": {
                    loges.create(STATE.TRY_CREATE);
                    //======================CREATE==============================
                    createLog(loges);
                    //==========================================================
                    break;
                }
                case "2": {
                    loges.create(STATE.TRY_RENAME);
                    //======================RENAME==============================
                    renameLog(loges);
                    //==========================================================
                    break;
                }
                case "3": {
                    loges.create(STATE.TRY_MODIFIED);
                    //======================MODIFIED============================
                    modifiedLog(loges);
                    //==========================================================
                    break;
                }
                case "4": {
                    loges.create(STATE.TRY_OPEN);
                    //======================CONTENT=============================
                    contentLog(loges);
                    //==========================================================
                    break;
                }
                case "5": {
                    loges.create(STATE.TRY_DELETE);
                    //======================DELETE==============================
                    deleteLog(loges);
                    //==========================================================
                    break;
                }
                case "6": {
                    String[] lines = getAllLines(LOG_Name);
                    StringBuilder append;
                    for (int i = 0; i < lines.length && lines[i] != null; i++) {
                        append = new StringBuilder(lines[i]);
                        Files.write(LOG_Name_MAIN, Collections.singleton(append), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        if (lines[i].contains("[CREATE]")){
                            Matcher matcherFile = fileName.matcher(lines[i]);
                            if (matcherFile.find()){
                                String file = lines[i].substring(matcherFile.start(), matcherFile.end());
                                createFile(dir_PATH.resolve(file));
                            }
                        }
                        else if (lines[i].contains("[DELETED]")){
                            Matcher matcherFile = fileName.matcher(lines[i]);
                            if (matcherFile.find()){
                                String file = lines[i].substring(matcherFile.start(), matcherFile.end());
                                deleteFile(dir_PATH.resolve(file));
                            }
                        }
                        else if (lines[i].contains("[RENAME]")){
                            Matcher matcherFile = fileName.matcher(lines[i]);
                            String[] file = new String[2];
                            int iterator = 0;
                            while (matcherFile.find()){
                                file[iterator++] = lines[i].substring(matcherFile.start(), matcherFile.end());
                            }
                                renameFile(dir_PATH.resolve(file[0]), dir_PATH.resolve(file[1]));
                        }
                        else if (lines[i].contains("[CONTENT]")){
                            Matcher matcherFile = fileName.matcher(lines[i]);
                            if (matcherFile.find()){
                                String file = lines[i].substring(matcherFile.start(), matcherFile.end());
                                modifiedFile(dir_PATH.resolve(file), lines[i + 1]);
                            }
                        }
                    };
                    Files.write(LOG_Name, "".getBytes());
                    Stream<Path> files = Files.walk(dir_PATH);
                    Stream<Path> backup = Files.walk(BACKUP);
                    backup.forEach(x->{
                        if (!x.getFileName().toString().equals("backup")){
                            try {
                                Files.delete(x);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    files.forEach(x -> {
                        try {
                            if (!x.getFileName().toString().equals("files")) {
                                Files.copy(x, BACKUP.resolve(x.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    break;
                }
            }
        } while (!choose.equals("6"));
    }

    private void deleteLog(Loges loges) throws IOException, InterruptedException {
        String fileName;
        //==========================================================
        System.out.print("Введите имя файла: ");
        fileName = buffer.readLine();
        //==================Задержка, чтобы успеть сымитировать закрытие программы===============
        System.out.println("Пожалуйства подождите.");
        Thread.sleep(2000);
        //========================================================================================
        loges.create(STATE.DELETED, fileName);
        System.out.println(fileName + " успешно удален.");
    }

    private void deleteFile(Path path){
        try {
            Files.delete(path);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void contentLog(Loges loges) throws IOException, InterruptedException {
        String fileName;
        Path file = Paths.get(DIR_FILE);
        System.out.print("Введите имя файла: ");
        fileName = buffer.readLine();
        if (Files.notExists(dir_PATH.resolve(fileName))){
            System.out.println("Такого файла не существует!");
            loges.create(STATE.NOT_EXITS, fileName);
        } else {
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            if (chanceToFailure()) {
                System.out.println("Не удалось открыть файл!");
                loges.create(STATE.NOT_OPEN, fileName);
            } else {
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //==================Задержка, чтобы успеть сымитировать закрытие программы===============
                System.out.println("Пожалуйства подождите.");
                Thread.sleep(2000);
                //========================================================================================
                String[] lines = getAllLines(file.resolve(fileName));
                System.out.println("Содержимое файла " + fileName + ":");
                for (String line : lines) {
                    if (line != null) {
                        System.out.println(line);
                    }
                }
                loges.create(STATE.OPEN, fileName);
            }
        }
    }

    private void modifiedLog(Loges loges) throws IOException, InterruptedException {
        String fileName;
        System.out.print("Введите имя файла: ");
        fileName = buffer.readLine();
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (chanceToFailure()){
            System.out.println("Не удалось открыть файл!");
            loges.create(STATE.NOT_MODIFIED, fileName);
        } else {
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            if (Files.notExists(dir_PATH.resolve(fileName))){
                System.out.println("Такого файла не существует!");
                loges.create(STATE.NOT_EXITS, fileName);
            } else {
                System.out.println("Введите содержимое: ");
                String content = buffer.readLine();
                //==================Задержка, чтобы успеть сымитировать закрытие программы===============
                System.out.println("Пожалуйства подождите.");
                Thread.sleep(2000);
                //========================================================================================
                loges.create(fileName, content);
                System.out.println(fileName + " успешно изменен!");
            }
        }
    }

    private void modifiedFile(Path path, String content){
        StringBuilder wr = new StringBuilder(content);
        try {
            Files.write(path, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void renameLog(Loges loges) throws IOException, InterruptedException {
        String fileName;
        System.out.println("Введите имя файла: ");
        fileName = buffer.readLine();
        if (Files.notExists(dir_PATH.resolve(fileName))){
            System.out.println("Такого файла не существует!");
            loges.create(STATE.NOT_EXITS, fileName);
        } else {
            System.out.println("Введите новое имя файла: ");
            String newFileName = buffer.readLine();
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            if (chanceToFailure()) {
                System.out.println("Не удалось переименовать файл!");
                loges.create(STATE.NOT_RENAME, fileName);
            } else {
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //==================Задержка, чтобы успеть сымитировать закрытие программы===============
                System.out.println("Пожалуйства подождите.");
                Thread.sleep(2000);
                //========================================================================================

                System.out.println(fileName + " успешно переименован в " + newFileName);
                loges.create(STATE.RENAME, fileName, newFileName);
            }
        }
    }

    private void renameFile(Path path0, Path path1) {
        try {
        Files.move(path0, path1);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void createLog(Loges loges) throws IOException, InterruptedException {
        String fileName;
        //==========================================================
        System.out.print("Введите имя файла: ");
        fileName = buffer.readLine();
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (chanceToFailure()){
            System.out.println("Не удалось создать файл!");
            loges.create(STATE.NOT_CREATE, fileName);
        } else {
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //==================Задержка, чтобы успеть сымитировать закрытие программы===============
            System.out.println("Пожалуйства подождите.");
            Thread.sleep(2000);
            //========================================================================================
            if (Files.notExists(FileManager.dir_PATH.resolve(fileName))) {
                loges.create(STATE.CREATE, fileName);
                System.out.println(fileName + " успешно создан.");
            } else {
                loges.create(STATE.ALREADY_EXIST, fileName);
                System.out.println(fileName + " уже существует.");
            }
        }
    }

    private void createFile(Path path) {
        try {
            Files.createFile(path);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean chanceToFailure(){
        R = rand.ints(0, 7).findAny().getAsInt();
        return percent.equals(R);
    }
    @Override
    public void run(){
        try {
            exec();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        System.out.println("Старт потока  " + flowName);
        //если поток не создан, то создаем и запускаем
        if (flowThread == null) {
            flowThread = new Thread(this, flowName);
            flowThread.start();
        }
    }

    private String getLastLine() {
        String[] lines = getAllLines(FileManager.LOG_Name);
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i] != null) {
                return lines[i];
            }
        }
        return null;
    }
    private String[] getAllLines(Path path) {
        int size = 0;
        int capacity = 10;
        String[] line = new String[capacity];
        String tmp;
        BufferedReader in;
        try
        {
            in = new BufferedReader(new FileReader(String.valueOf(path)));
            while ((tmp = in.readLine()) != null)
            {
                if (size + 1 >= capacity){
                    line = resize(capacity, line);
                    capacity += 10;
                }
                line[size++] = tmp;
            }
        } catch (IOException exception)
        {
            exception.printStackTrace();
        }
        return line;
    }
    private String[] resize(int capacity, String[] line){
        int newCapacity = capacity + 10;
        String[] newLine = new String[newCapacity];
        System.arraycopy(line, 0, newLine, 0, line.length);
        return newLine;
    }
}
