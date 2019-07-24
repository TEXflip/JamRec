package com.tessari.jamrec.Codec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class WaveWriter implements Progressable{
    private final int LONGINT = 4;
    private final int SMALLINT = 2;
    private final int INTEGER = 4;
    private final int ID_STRING_SIZE = 4;
    private final int WAV_RIFF_SIZE = LONGINT + ID_STRING_SIZE;
    private final int WAV_FMT_SIZE = (4 * SMALLINT) + (INTEGER * 2) + LONGINT + ID_STRING_SIZE;
    private final int WAV_DATA_SIZE = ID_STRING_SIZE + LONGINT;
    private final int WAV_HDR_SIZE = WAV_RIFF_SIZE + ID_STRING_SIZE + WAV_FMT_SIZE + WAV_DATA_SIZE;
    private final short PCM = 1;
    private final int SAMPLE_SIZE = 2;
    int cursor, nSamples, sampleRate, bufferSize;
    short nChannels;
    Vector<short[]> data;
    byte[] output;
    OnProgressChangedListener progressListener;

    public WaveWriter(int sampleRate, short nChannels, Vector<short[]> data, int bufferSize) {
        nSamples = bufferSize * data.size() + 1;
        cursor = 0;
        output = new byte[nSamples * SMALLINT + WAV_HDR_SIZE];
        this.sampleRate = sampleRate;
        this.nChannels = nChannels;
        this.bufferSize = bufferSize;
        this.data = data;
    }

    private void buildHeader(int sampleRate, short nChannels) {
        write("RIFF");
        write(output.length);
        write("WAVE");
        writeFormat(sampleRate, nChannels);
    }

    public void writeFormat(int sampleRate, short nChannels) {
        write("fmt ");
        write(WAV_FMT_SIZE - WAV_DATA_SIZE);
        write(PCM);
        write(nChannels);
        write(sampleRate);
        write(nChannels * sampleRate * SAMPLE_SIZE);
        write((short) (nChannels * SAMPLE_SIZE));
        write((short) 16);
    }

    public void writeData(Vector<short[]> data, int bufferSize) {
        write("data");
        write(nSamples * SMALLINT);
        int size = data.size();
        for (int v = 0; v < size; v++) {
            short[] bufferData = data.get(v);
            for (int i = 0; i < bufferSize; i++)
                write(bufferData[i]);
            progressListener.onPorgressChanged((v+1)/(float)size);
        }

    }

    private void write(byte b) {
        output[cursor++] = b;
    }

    private void write(String id) {
        if (id.length() != ID_STRING_SIZE)
            ;//Log.e("Export Error", "String " + id + " must have four characters.");
        else {
            for (int i = 0; i < ID_STRING_SIZE; ++i) write((byte) id.charAt(i));
        }
    }

    private void write(int i) {
        write((byte) (i & 0xFF));
        i >>= 8;
        write((byte) (i & 0xFF));
        i >>= 8;
        write((byte) (i & 0xFF));
        i >>= 8;
        write((byte) (i & 0xFF));
    }

    private void write(short i) {
        write((byte) (i & 0xFF));
        i >>= 8;
        write((byte) (i & 0xFF));
    }

    public boolean wroteToFile(String filename, File exportPath) {
        buildHeader(sampleRate, nChannels);
        writeData(data, bufferSize);
        boolean ok = false;

        try {
            File path = new File(exportPath, filename);
            FileOutputStream outFile = new FileOutputStream(path);
            outFile.write(output);
            outFile.flush();
            outFile.close();
            ok = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ok = false;
        } catch (IOException e) {
            ok = false;
            e.printStackTrace();
        }
        return ok;
    }

    @Override
    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        progressListener = listener;
    }
}
