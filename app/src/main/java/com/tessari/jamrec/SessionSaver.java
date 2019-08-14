package com.tessari.jamrec;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.tessari.jamrec.Save.Savable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SessionSaver {

    private Context context;
    private Map<String, Savable> savableObjects;
    private Gson json;
    private File savePath;

    public SessionSaver(Savable[] savableObjects, Context context) {
        this.context = context;
        this.savableObjects = new HashMap<>();
        for (Savable s : savableObjects)
            this.savableObjects.put(s.getName(), s);
//        savePath = context.getDir("saves", Context.MODE_PRIVATE);
        savePath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC), "JamRec");
        savePath.mkdirs();
        json = new Gson();
    }

    public File[] getSavedFiles(){
        return savePath.listFiles();
    }

    public void saveSession(String name) {
        Map<String, Object> saving = new HashMap<>();
        for (String s : savableObjects.keySet())
            saving.put(s, savableObjects.get(s).save());
        String jsonString = json.toJson(saving);
        wroteToFile(name + ".json", jsonString);
    }

    public void restoreSession(File restoreFile) {
        String jsonString = readFromFile(restoreFile);
        Map saving = json.fromJson(jsonString, Map.class);

        for (String s : savableObjects.keySet())
            savableObjects.get(s).restore(saving.get(s));
    }

    private String readFromFile(File file) {
        try {
            StringBuilder text = new StringBuilder();
            FileInputStream fileInputStream = new FileInputStream(file);
            int c;
            while ((c = fileInputStream.read()) > 0)
                text.append(c);
            return text.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File wroteToFile(String filename, String jsonString) {
        try {
            File path = new File(savePath, filename);
            FileOutputStream outFile = new FileOutputStream(path);
            outFile.write(jsonString.getBytes());
            outFile.flush();
            outFile.close();
            return path;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
