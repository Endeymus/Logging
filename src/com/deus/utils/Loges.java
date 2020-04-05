package com.deus.utils;

import com.deus.utils.STATE;

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
    final static private String DIR_LOG = "C:\\Users\\User\\IdeaProjects\\Logging\\src\\logs\\log.txt";
    final static private Path LOG_Name = Paths.get(DIR_LOG);

    public Loges() {
        if(Files.notExists(LOG_Name)) {
            try {
                Files.createFile(LOG_Name);
            } catch (IOException e){
                e.printStackTrace();
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
            case MODIFIED:
                wr.append(" [MODIFIED] ").append(fileName).append(" был изменен.");
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
        }
        try {
            Files.write(LOG_Name, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public void create(STATE state){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("y.MM.dd HH:mm:ss");
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
                wr.append(" [INFO] ").append(" запрос на изменение.");
                break;
            case TREE:
                wr.append(" [INFO] ").append(" вывод дерева файлов.");
        }
        try {
            Files.write(LOG_Name, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }

    }
    public void create(ERROR error){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("y.MM.dd HH:mm:ss");
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
        }
        try {
            Files.write(LOG_Name, Collections.singleton(wr), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
