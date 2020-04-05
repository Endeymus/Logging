package com.deus.manager;

import com.deus.utils.ERROR;
import com.deus.utils.Loges;
import com.deus.utils.STATE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Stream;

public class FileManager implements Runnable {
    private Thread flowThread; //поток
    private String flowName; //имя потока

    final static private String DIR_FILE = "C:\\Users\\User\\IdeaProjects\\Logging\\src\\files";
    private static final String DIR_ERROR = "C:\\Users\\User\\IdeaProjects\\Logging\\src\\error";
    private static BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
    private static final String[] formats = {".txt",".rtf",".doc",".docx",".html",".pdf",".odt", ".log"};
    private static Random rand = new Random(47);
    private static Integer R = rand.ints(1, 6).findAny().getAsInt();
    private static Integer percent = 1;

    public FileManager(String flowName) {
        this.flowName = flowName;
    }

    private static String getLastLine(final Path path) {
        String line = null;
        String tmp;
        BufferedReader in;
        try
        {
            in = new BufferedReader(new FileReader(String.valueOf(path)));

            while ((tmp = in.readLine()) != null)
            {
                line = tmp;
            }
        } catch (IOException exception)
        {
            exception.printStackTrace();
        }


        return line;
    }
    private static void menu(){
        System.out.println("1. Создание файла");
        System.out.println("2. Изменение файла");
        System.out.println("3. Удаление файла");
        System.out.println("4. Вывод файлов каталога");
        System.out.println("5. Завершение работы");
    }

    private void exec() throws IOException, InterruptedException{
        Path error = Paths.get(DIR_ERROR);
        String main_args = "0";
        if (Files.notExists(error)){
            Files.createDirectory(error);
        }
        if (Files.notExists(error.resolve("error.txt"))){
            Files.createFile(error.resolve("error.txt"));
        } else {
            String line = getLastLine(error.resolve("error.txt"));
            if (line != null){
                main_args = line;
            }
            else {
                main_args = "0";
            }
        }
        Loges loges = new Loges();
        Path dir_PATH = Paths.get(DIR_FILE);
        String fileName;
        String choise;
        String y;
        //System.out.println(main_args);
        switch (main_args){
            case "1": {
                System.out.println("Хотите ли продолжить создание файла? (y / n)");
                y = buffer.readLine();
                if (y.equals("y")){
                    System.out.print("Введите имя файла: ");
                    fileName = buffer.readLine();
                    if (Files.notExists(dir_PATH.resolve(fileName))) {
                        try {
                            Files.createFile(dir_PATH.resolve(fileName));
                            loges.create(STATE.CREATE, fileName);
                            System.out.println(fileName + " был успешно создан");
                        } catch (IOException e) {
                            loges.create(STATE.ALREADY_EXIST, fileName);
                            System.out.println(fileName + " не был создан");
                        }
                    }
                }
                else {
                    loges.create(ERROR.NOT_CREATE);
                }
                break;
            }
            case "2": {
                System.out.println("Хотите ли продолжить изменение файла? (y / n)");
                y = buffer.readLine();
                boolean flag = false;
                if (y.equals("y")){
                    System.out.print("Введите имя файла: ");
                    fileName = buffer.readLine();
                    for (String format : formats) {
                        if (fileName.contains(format)) {
                            if (Files.exists(dir_PATH.resolve(fileName))) {
                                Process proc = Runtime.getRuntime().exec("notepad.exe " + dir_PATH.resolve(fileName));
                                proc.waitFor();
                                loges.create(STATE.MODIFIED, fileName);
                                proc.destroy();
                                //System.out.println(proc.exitValue());
                                flag = true;
                                System.out.println(fileName + " успешно изменен.");
                                break;
                            } else {
                                loges.create(STATE.NOT_EXITS, fileName);
                                loges.create(STATE.NOT_MODIFIED, fileName);
                                System.out.println(fileName + " не существует");
                                System.out.println(fileName + " не был изменен");
                                break;
                            }
                        }

                    }
                    if (!flag) {
                        loges.create(STATE.NOT_MODIFIED, fileName);
                        System.out.println(fileName + " не был изменен (может быть проблема в формате)");
                    }
                }else {
                    loges.create(ERROR.NOT_MODIFIED);
                }
                break;
            }
            case "3": {
                System.out.println("Хотите ли продолжить удаление файла? (y / n)");
                y = buffer.readLine();
                if (y.equals("y")){
                    System.out.print("Введите имя файла: ");
                    fileName = buffer.readLine();
                    try {
                        Files.delete(dir_PATH.resolve(fileName));
                        loges.create(STATE.DELETED, fileName);
                        System.out.println(fileName + " успешно удален.");
                    } catch (IOException e) {
                        if (Files.notExists(dir_PATH.resolve(fileName))) {
                            loges.create(STATE.NOT_EXITS, fileName);
                            System.out.println(fileName + " не существует");
                        }
                        loges.create(STATE.NOT_DELETED, fileName);
                        System.out.println(fileName + " не был удален");
                    }
                }
                else {
                    loges.create(ERROR.NOT_DELETED);
                }
                break;
            }
            case "0": {
                break;
            }
        }
        //----------------------------------Заускам контроллер------------------------------------------
        Controller controller = new Controller("controller", this);
        controller.start();
        //----------------------------------------------------------------------------------------------
        do {
            menu();
            System.out.print("Ваш выбор -> ");
            choise = buffer.readLine();
            switch (choise) {
                case "1": {
                    loges.create(STATE.TRY_CREATE);
                    //==========================================================
                    try{
                        R = rand.ints(0, 6).findAny().getAsInt();
                        if (percent.equals(R)){
                            throw new IOException();
                        }
                    }
                    catch (IOException e){
                        Files.write(error.resolve("error.txt"), Collections.singleton(new StringBuilder("1")), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        Thread.sleep(3000);
                        System.exit(1);
                    }
                    //==========================================================
                    System.out.print("Введите имя файла: ");
                    fileName = buffer.readLine();
                    if (Files.notExists(dir_PATH.resolve(fileName))) {
                        try {
                            Files.createFile(dir_PATH.resolve(fileName));
                            loges.create(STATE.CREATE, fileName);
                            System.out.println(fileName + " был успешно создан");
                        } catch (IOException e) {
                            loges.create(STATE.ALREADY_EXIST, fileName);
                            System.out.println(fileName + " не был создан");
                        }
                    }
                    break;
                }
                case "2": {
                    boolean flag = false;
                    loges.create(STATE.TRY_MODIFIED);
                    //==========================================================
                    try{
                        R = rand.ints(0, 6).findAny().getAsInt();
                        if (percent.equals(R)){
                            throw new IOException();
                        }
                    }
                    catch (IOException e){
                        Files.write(error.resolve("error.txt"), Collections.singleton(new StringBuilder("2")), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        Thread.sleep(3000);
                        System.exit(2);
                    }
                    //==========================================================
                    System.out.print("Введите имя файла: ");
                    fileName = buffer.readLine();
                    for (String format : formats) {
                        if (fileName.contains(format)) {
                            if (Files.exists(dir_PATH.resolve(fileName))) {
                                Process proc = Runtime.getRuntime().exec("notepad.exe " + dir_PATH.resolve(fileName));
                                proc.waitFor();
                                loges.create(STATE.MODIFIED, fileName);
                                proc.destroy();
                                //System.out.println(proc.exitValue());
                                flag = true;
                                System.out.println(fileName + " успешно изменен.");
                                break;
                            } else {
                                loges.create(STATE.NOT_EXITS, fileName);
                                loges.create(STATE.NOT_MODIFIED, fileName);
                                System.out.println(fileName + " не существует");
                                System.out.println(fileName + " не был изменен");
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        loges.create(STATE.NOT_MODIFIED, fileName);
                        System.out.println(fileName + " не был изменен (может быть проблема в формате)");
                    }
                    break;
                }
                case "3": {
                    loges.create(STATE.TRY_DELETE);
                    //==========================================================
                    try{
                        R = rand.ints(0, 6).findAny().getAsInt();
                        if (percent.equals(R)){
                            throw new IOException();
                        }
                    }
                    catch (IOException e){
                        Files.write(error.resolve("error.txt"), Collections.singleton(new StringBuilder("3")), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                        Thread.sleep(3000);
                        System.exit(3);
                    }
                    //==========================================================
                    System.out.print("Введите имя файла: ");
                    fileName = buffer.readLine();
                    try {
                        Files.delete(dir_PATH.resolve(fileName));
                        loges.create(STATE.DELETED, fileName);
                        System.out.println(fileName + " успешно удален.");
                    } catch (IOException e) {
                        if (Files.notExists(dir_PATH.resolve(fileName))) {
                            loges.create(STATE.NOT_EXITS, fileName);
                            System.out.println(fileName + " не существует");
                        }
                        loges.create(STATE.NOT_DELETED, fileName);
                        System.out.println(fileName + " не был удален");
                    }
                    break;
                }
                case "4": {
                    System.out.println("|===================================================================|");
                    Stream<Path> stream = Files.walk(dir_PATH);
                    stream.forEach(x -> {
                        System.out.println(x.getFileName());
                    });
                    System.out.println("|===================================================================|");
                }
                case "5": {
                    Files.write(error.resolve("error.txt"), Collections.singleton(new StringBuilder("0")), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                    break;
                }
            }
        } while (!choise.equals("5"));
    }


    public Thread getFlowThread() {
        return flowThread;
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
}
