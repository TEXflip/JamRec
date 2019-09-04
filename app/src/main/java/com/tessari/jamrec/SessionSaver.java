package com.tessari.jamrec;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.tessari.jamrec.Save.MetronomeSave;
import com.tessari.jamrec.Save.RecorderSave;
import com.tessari.jamrec.Save.Savable;
import com.tessari.jamrec.Save.TrackSave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * modulo che si occupa del salvataggio della sessione
 */
public class SessionSaver {

    private Context context;
    private Map<String, Savable> savableObjects;
    private Gson json;
    private File savePath;
    private NamedSession namedSession;

    public SessionSaver(Savable[] savableObjects, NamedSession namedSession, Context context) {
        this.context = context;
        this.namedSession = namedSession;
        this.savableObjects = new HashMap<>();
        for (Savable s : savableObjects) {
            this.savableObjects.put(s.getClass().getSimpleName(), s);
        }
        savePath = context.getDir("saves", Context.MODE_PRIVATE);
//        savePath = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_MUSIC), "JamRec");
        savePath.mkdirs();
        json = new Gson();
    }

    public File[] getSavedFiles() {
        File[] files = savePath.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return -(int)(file.lastModified() - t1.lastModified());
            }
        });
        return files;
    }

    public void saveSession(String name) {
        if (name == null)
            if (namedSession.getSessionName() != null)
                name = namedSession.getSessionName();
            else
                name = android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString();
        namedSession.setSessionName(name);
        Map<String, Object> saving = new HashMap<>();
        for (String s : savableObjects.keySet())
            saving.put(s, savableObjects.get(s).save());
        String jsonString = json.toJson(saving);
        wroteToFile(name + ".json", jsonString);
    }

    public void restoreSession(File restoreFile) {
        String sessionName = restoreFile.getName();
        sessionName = sessionName.substring(0, sessionName.lastIndexOf('.'));
        namedSession.setSessionName(sessionName);
        String jsonString = readFromFile(restoreFile);
        Log.e("RESTORE SESSION", jsonString);
        ObjectSaver saving = json.fromJson(jsonString, ObjectSaver.class);
        try {
            for (Field f : ObjectSaver.class.getFields()) {
                if (savableObjects.containsKey(f.getName()))
                    savableObjects.get(f.getName()).restore(f.get(saving));
//            savableObjects.get(s).restore(saving.get(s));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private String readFromFile(File file) {
        try {
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
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

    public class ObjectSaver {
        public MetronomeSave Metronome;
        public TrackSave Track;
        public RecorderSave Recorder;

        public ObjectSaver() {
        }

        public MetronomeSave getMetronome() {
            return Metronome;
        }

        public void setMetronome(MetronomeSave metronome) {
            Metronome = metronome;
        }

        public TrackSave getTrack() {
            return Track;
        }

        public void setTrack(TrackSave track) {
            Track = track;
        }

        public RecorderSave getRecorder() {
            return Recorder;
        }

        public void setRecorder(RecorderSave recorder) {
            Recorder = recorder;
        }
    }
}
