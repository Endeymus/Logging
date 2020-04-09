package com.shamray.utils;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;

public class Loges {
    final static private String DIR_LOG = "logs";
    final static private Path LOG_Name = Paths.get(DIR_LOG);
    private static final String file_Name = "log.txt";
    private static final Path FILE_PATH = LOG_Name.resolve(file_Name);

    public Loges() {

        if(Files.notExists(LOG_Name)) {
            try {
                Files.createDirectory(LOG_Name);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if (Files.notExists(FILE_PATH)){
            try {
                Files.createFile(FILE_PATH);
            } catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    public void create(STATE state, String fileName){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.y HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        StringBuilder wr = new StringBuilder(simpleDateFormat.format(calendar.getTime()));
        switch (state){
            case CREATE:
                wr.append(" [CREATE] ").append(fileName).append(" был создан.");
                break;
            case OPEN:
                wr.append(" [OPEN] ").append(fileName).append(" был открыт.");
                break;
            case DELETED:
                wr.append(" [DELETED] ").append(fileName).append(" был удален.");
                break;
            case ALREADY_EXIST:
                wr.append(" [ERROR] ").append(fileName).append(" уже существует.");
                break;
            case NOT_EXITS:
                wr.append(" [ERROR] ").append(fileName).append(" не существует.");
                break;
            case NOT_CREATE:
                wr.append(" [ERROR] ").append(fileName).append(" не удалось создать.");
                break;
            case NOT_DELETED:
                wr.append(" [ERROR] ").append(fileName).append(" не удалось удалить.");
                break;
            case NOT_MODIFIED:
                wr.append(" [ERROR] ").append(fileName).append(" не удалось изменить.");
                break;
            case NOT_OPEN:
                wr.append(" [ERROR] ").append(fileName).append(" не удалось открыть.");
                break;
            case NOT_RENAME:
                wr.append(" [ERROR] ").append(fileName).append(" не удалось переименовать.");
                break;


        }
        try {
            Files.write(FILE_PATH, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void create(String fileName, String content) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.y HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        StringBuilder wr = new StringBuilder(simpleDateFormat.format(calendar.getTime()));

        wr.append(" [CONTENT] ").append(fileName).append(" был успешно изменен ").append("\n").append(content);
        try {
            Files.write(FILE_PATH, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void create(STATE state, String fileName0, String fileName1){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.y HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        StringBuilder wr = new StringBuilder(simpleDateFormat.format(calendar.getTime()));
        switch (state){
            case RENAME: {
                wr.append(" [RENAME] ").append(fileName0).append(" был переиментован в ").append(fileName1);
                break;
            }
        }
        try {
            Files.write(FILE_PATH, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void create(STATE state){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.y HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        StringBuilder wr = new StringBuilder(simpleDateFormat.format(calendar.getTime()));
        switch (state){
            case TRY_CREATE:
                wr.append(" [INFO] ").append(" запрос на создание файла.");
                break;
            case TRY_DELETE:
                wr.append(" [INFO] ").append(" запрос на удаление файла.");
                break;
            case TRY_MODIFIED:
                wr.append(" [INFO] ").append(" запрос на изменение файла.");
                break;
            case TREE:
                wr.append(" [INFO] ").append(" вывод дерева файлов.");
                break;
            case TRY_RENAME:
                wr.append(" [INFO] ").append(" запрос на переименование файла.");
                break;
            case TRY_OPEN:
                wr.append(" [INFO] ").append(" запрос на открытие файла.");
                break;
        }
        try {
            Files.write(FILE_PATH, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }

    }
    public void create(ERROR error){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.y HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        StringBuilder wr = new StringBuilder(simpleDateFormat.format(calendar.getTime()));
        switch (error){
            case NOT_CREATE:
                wr.append(" [ERROR] ").append(" отмена создания файла.");
                break;
            case NOT_DELETED:
                wr.append(" [ERROR] ").append(" отмена удаления файла.");
                break;
            case NOT_MODIFIED:
                wr.append(" [ERROR] ").append(" отмена изменения файла.");
                break;
            case NOT_OPEN:
                wr.append(" [ERROR] ").append(" отмена открытия файла.");
                break;
            case NOT_RENAME:
                wr.append(" [ERROR] ").append(" отмена переименовывания файла.");
        }
        try {
            Files.write(FILE_PATH, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
