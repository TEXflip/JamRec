package com.tessari.jamrec.Codec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

public class WaveReader {
    private static final int WAV_HEADER_CHUNK_ID = 0x52494646;  // "RIFF"
    private static final int WAV_FORMAT = 0x57415645;  // "WAVE"
    private static final int WAV_FORMAT_CHUNK_ID = 0x666d7420; // "fmt "
    private static final int WAV_DATA_CHUNK_ID = 0x64617461; // "data"
    private static final int STREAM_BUFFER_SIZE = 4096;

    private File InFile;
    private BufferedInputStream InStream;

    private int SampleRate;
    private int Channels;
    private int SampleBits;
    private int FileSize;
    private int DataSize;

    public WaveReader(String path, String name) {
        this.InFile = new File(path + File.separator + name);
    }

    public WaveReader(File file) {
        this.InFile = file;
    }

    public void openWave() throws FileNotFoundException, InvalidPropertiesFormatException, IOException {
        FileInputStream fileStream = new FileInputStream(InFile);
        InStream = new BufferedInputStream(fileStream, STREAM_BUFFER_SIZE);

        int headerId = readUnsignedInt(InStream);
        if (headerId != WAV_HEADER_CHUNK_ID) {
            throw new InvalidPropertiesFormatException(String.format("Invalid WAVE header chunk ID: %d", headerId));
        }
        FileSize = readUnsignedIntLE(InStream);
        int format = readUnsignedInt(InStream);
        if (format != WAV_FORMAT) {
            throw new InvalidPropertiesFormatException("Invalid WAVE format");
        }

        int formatId = readUnsignedInt(InStream);
        if (formatId != WAV_FORMAT_CHUNK_ID) {
            throw new InvalidPropertiesFormatException("Invalid WAVE format chunk ID");
        }
        int formatSize = readUnsignedIntLE(InStream);
        if (formatSize != 16) {

        }
        int audioFormat = readUnsignedShortLE(InStream);
        if (audioFormat != 1) {
            throw new InvalidPropertiesFormatException("Not PCM WAVE format");
        }
        Channels = readUnsignedShortLE(InStream);
        SampleRate = readUnsignedIntLE(InStream);
        int byteRate = readUnsignedIntLE(InStream);
        int blockAlign = readUnsignedShortLE(InStream);
        SampleBits = readUnsignedShortLE(InStream);
        if(SampleBits != 16)
            throw new InvalidPropertiesFormatException("Only 16 bit samples supported");

        int dataId = readUnsignedInt(InStream);
        if (dataId != WAV_DATA_CHUNK_ID) {
            throw new InvalidPropertiesFormatException("Invalid WAVE data chunk ID");
        }
        DataSize = readUnsignedIntLE(InStream);
    }

    public int getSampleRate() {
        return SampleRate;
    }

    public int getChannels() {
        return Channels;
    }

    public int getPcmFormat() {
        return SampleBits;
    }

    public int getFileSize() {
        return FileSize + 8;
    }

    public int getDataSize() {
        return DataSize;
    }

    public int getLength() {
        if (SampleRate == 0 || Channels == 0 || (SampleBits + 7) / 8 == 0) {
            return 0;
        } else {
            return DataSize / (SampleRate * Channels * ((SampleBits + 7) / 8));
        }
    }

    public int read(short[] dst, int numSamples) throws IOException {
        if (Channels != 1) {
            return -1;
        }

        byte[] buf = new byte[numSamples * 2];
        int index = 0;
        int bytesRead = InStream.read(buf, 0, numSamples * 2);

        for (int i = 0; i < bytesRead; i+=2) {
            dst[index] = byteToShortLE(buf[i], buf[i+1]);
            index++;
        }

        return index;
    }

    public int read(short[] left, short[] right, int numSamples) throws IOException {
        if (Channels != 2) {
            return -1;
        }
        byte[] buf = new byte[numSamples * 4];
        int index = 0;
        int bytesRead = InStream.read(buf, 0, numSamples * 4);

        for (int i = 0; i < bytesRead; i+=2) {
            short val = byteToShortLE(buf[0], buf[i+1]);
            if (i % 4 == 0) {
                left[index] = val;
            } else {
                right[index] = val;
                index++;
            }
        }

        return index;
    }

    public void closeWaveFile() throws IOException {
        if (InStream != null) {
            InStream.close();
        }
    }

    private static short byteToShortLE(byte b1, byte b2) {
        return (short) (b1 & 0xFF | ((b2 & 0xFF) << 8));
    }

    private static int readUnsignedInt(BufferedInputStream in) throws IOException {
        int ret;
        byte[] buf = new byte[4];
        ret = in.read(buf);
        if (ret == -1) {
            return -1;
        } else {
            return (((buf[0] & 0xFF) << 24)
                    | ((buf[1] & 0xFF) << 16)
                    | ((buf[2] & 0xFF) << 8)
                    | (buf[3] & 0xFF));
        }
    }

    private static int readUnsignedIntLE(BufferedInputStream in) throws IOException {
        int ret;
        byte[] buf = new byte[4];
        ret = in.read(buf);
        if (ret == -1) {
            return -1;
        } else {
            return (buf[0] & 0xFF
                    | ((buf[1] & 0xFF) << 8)
                    | ((buf[2] & 0xFF) << 16)
                    | ((buf[3] & 0xFF) << 24));
        }
    }

    private static short readUnsignedShortLE(BufferedInputStream in) throws IOException {
        int ret;
        byte[] buf = new byte[2];
        ret = in.read(buf, 0, 2);
        if (ret == -1) {
            return -1;
        } else {
            return byteToShortLE(buf[0], buf[1]);
        }
    }
}
