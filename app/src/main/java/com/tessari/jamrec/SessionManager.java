package com.tessari.jamrec;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ToggleButton;

import com.tessari.jamrec.Activity.FileSelectionDialog;
import com.tessari.jamrec.Codec.WaveReader;
import com.tessari.jamrec.CustomView.AudioWaves;
import com.tessari.jamrec.CustomView.Beatsline;
import com.tessari.jamrec.CustomView.MetrnomeVisualizer;
import com.tessari.jamrec.CustomView.SavesListView;
import com.tessari.jamrec.CustomView.Timeline;
import com.tessari.jamrec.Save.Savable;
import com.tessari.jamrec.Save.TrackSave;
import com.tessari.jamrec.Util.CustomToast;
import com.tessari.jamrec.Util.SupportMath;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class SessionManager implements NamedSession {
    public SessionGestureManager gestureManager;
    private SessionSaver sessionSaver;
    private String sessionName = null;

    private Activity context;
    SavesListView savedList;
    public AudioWaves audioWaves;
    Timeline timeline;
    Beatsline beatsline;
    private ToggleButton button_rec, button_play;
    private MetrnomeVisualizer metrnomeVisualizer;
    public Track track;
    public Recorder recorder;
    public Metronome metronome;
    long startTime, syncTime;
    private int bufferSize = 1024, sampleRate = 44100;
    private float pBPosFloat = 0;
    int offset = 0, trackViewWidth, precTick = -1;
    public long millis = 0;

    @SuppressLint("ClickableViewAccessibility")
    public SessionManager(final Activity context, final int sampleRate, int bufferSize, int audio_encoding, int audio_channel_in, int audio_channel_out) {
        //region set Views
        this.button_rec = context.findViewById(R.id.recButton);
        this.button_play = context.findViewById(R.id.playButton);
        this.audioWaves = context.findViewById(R.id.audioWaves);
        this.timeline = context.findViewById(R.id.timeline);
        this.beatsline = context.findViewById(R.id.beatsline);
        savedList = context.findViewById(R.id.saves_list);
        //endregion
        this.context = context;
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;

        metronome = new Metronome(MediaPlayer.create(context, R.raw.metronome2), MediaPlayer.create(context, R.raw.metronome));
        metrnomeVisualizer = context.findViewById(R.id.metrnomeVisualizer);

        gestureManager = new SessionGestureManager(this, context);

        track = new Track(sampleRate, bufferSize, audio_encoding, audio_channel_out);
        recorder = new Recorder(sampleRate, bufferSize, audio_encoding, audio_channel_in);
        audioWaves.setTrack(track);
        audioWaves.setSession(this);

        Savable[] objectsToSave = {track, metronome, recorder};
        sessionSaver = new SessionSaver(objectsToSave, this, context);
        savedList.setFiles(sessionSaver.getSavedFiles());


        //region set Listeners
        savedList.setOnFileActionChosenListener(new FileSelectionDialog.OnFileActionChosenListener() {
            @Override
            public void onOpen(File target) {

                sessionSaver.restoreSession(target);
                updateViews();
            }

            @Override
            public void onDelete(File target) {
                target.delete();
                updateSaveList();
            }
        });

        audioWaves.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureManager.onTouchAudioWavesEvent(motionEvent);
                return true;
            }
        });
        timeline.setSession(this);
        timeline.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureManager.onTouchTimebarEvent(motionEvent);
                return true;
            }
        });
        beatsline.setSession(this);
        beatsline.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureManager.onTouchBeatsbarEvent(motionEvent);
                return true;
            }
        });

        track.setTrackListener(new Track.TrackListener() {
            @Override
            public void onDelete() {
                updateViews();
            }

            @Override
            public void onResetAudio() {
                updateViews();
            }

            @Override
            public void onPause() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button_play.setChecked(false);
                    }
                });
            }

            @Override
            public void onSync() {
                syncTime = System.nanoTime() - startTime;
            }

            @Override
            public void onPlayerBufferIncrease(int playerBufferPosition, int samplesRead) {
                updateViews();
                int currTick = (int) metronome.fromSecToTicks(playerBufferPosition / (double) sampleRate);
                metrnomeVisualizer.setCurrentTick(currTick);
                if (precTick != currTick) {
                    metronome.tick(currTick);
                    precTick = currTick;
                }
            }

            @Override
            public void onRecBufferIncrese(int recBufferposition) {
                int currTick = (int) metronome.fromSecToTicks(recBufferposition / (double) sampleRate);
                metrnomeVisualizer.setCurrentTick(currTick);
                if (precTick != currTick) {
                    metronome.tick(currTick);
                    precTick = currTick;
                }
            }
        });

        recorder.setNewBufferReadListener(new Recorder.OnNewBufferReadListener() {
            @Override
            public void onRead(short[] data) {
                track.write(data);
                updateViews();
            }
        });

        metronome.setOnValueChangedListener(new Metronome.OnValueChangedListener() {
            @Override
            public void onTickPerBeatChanged(int tickPerBeat) {
                metrnomeVisualizer.setTickPerBeat(tickPerBeat);
                updateViews();
            }

            @Override
            public void onBpmChanged(int bpm) {
                updateViews();
            }

            @Override
            public void onDivChanged(int div) {
                updateViews();
            }
        });

        audioWaves.post(new Runnable() {
            @Override
            public void run() {
                trackViewWidth = sampleRate * 15;
                offset = trackViewWidth / 2 - sampleRate / 10;
                updateViews();
            }
        });
        //endregion
    }

    public void updateViews() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audioWaves.invalidate();
                timeline.invalidate();
                beatsline.invalidate();
                metrnomeVisualizer.invalidate();
            }
        });
    }

    private void updateSaveList() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                savedList.setFiles(sessionSaver.getSavedFiles());
            }
        });
    }

    public void startRec() {
        if (!isPlaying()) {
            recorder.startToRec();
            startTime = System.nanoTime();
            button_rec.setChecked(true);
        } else
            button_rec.setChecked(false);
    }

    public void stopRec() {
        recorder.stop();
        track.syncActivation = true;
        button_rec.setChecked(false);
    }

    public void startPlay() {
        if (!isRecording()) {
            track.play();
            button_play.setChecked(true);
        } else
            button_play.setChecked(false);
    }

    public void pausePlay() {
        track.pause();
    }

    public void restartPlay() {
        track.resetPlay();
        updateViews();
    }

    public void saveSession(String name) {
        sessionSaver.saveSession(name);
        updateSaveList();
    }

    /**
     * importa un file wav e lo carica nella track
     *
     * @param file
     */
    public void import_file(File file) {
        String filePath = file.getPath();
        if (!".wav".equals(filePath.substring(filePath.lastIndexOf(".")).trim().toLowerCase())) {
            CustomToast.showErrorToast(context, "Format Not Supported");
            return;
        }

        WaveReader waveReader = new WaveReader(file);

        try {
            waveReader.openWave();
        } catch (IOException e) {
            CustomToast.showErrorToast(context, e.getMessage());
            return;
        }

        if (waveReader.getChannels() != 1) {
            CustomToast.showErrorToast(context, "Stereo not supported");
            return;
        }

        TrackSave save = new TrackSave();
        save.setAudio_channel_out(AudioFormat.CHANNEL_OUT_MONO);
        save.setAudio_encoding(AudioFormat.ENCODING_PCM_16BIT);
        save.setBufferSize(bufferSize);
        save.setSampleRate(waveReader.getSampleRate());
        save.setData(new short[bufferSize]);

        Vector<short[]> trackSamples = new Vector<>();
        for (int i = 0; i < waveReader.getDataSize() / bufferSize; i++) {
            short[] chunk = new short[bufferSize];

            try {
                waveReader.read(chunk, bufferSize);
            } catch (IOException e) {
                CustomToast.showErrorToast(context, "Error reading the file");
            }
            trackSamples.add(chunk);
        }
        int remaning = waveReader.getDataSize() % bufferSize;
        if (remaning != 0) {
            short[] chunk = new short[remaning];

            try {
                waveReader.read(chunk, remaning);
            } catch (IOException e) {
                CustomToast.showErrorToast(context, "Error reading the file");
            }
            trackSamples.add(chunk);
        }

        save.setTrackSamples(trackSamples);
        save.setMaxRecPos(waveReader.getDataSize());

        track.restore(save);
    }

    public boolean isRecording() {
        return recorder.isRecording();
    }

    public boolean isPlaying() {
        return track.isPlaying();
    }

    public int getOffset() {
        return offset;
    }

    public int getOffsetAt0() {
        return offset - trackViewWidth / 2;
    }

    public int getTrackViewWidth() {
        return trackViewWidth;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getPlayBarPos() {
        return track.getPlayerBufferPos();
    }

    public int getRecBarPos() {
        return track.getVisualRecPos();
    }

    public float getViewsRatio() {
        return ((float) trackViewWidth / (float) audioWaves.getWidth());
    }

    /**
     * allarga o diminuisce la dimensione della trackViewWidth
     * min = viewWidth, max = Integer.MAX
     *
     * @param x quantitá di zoom
     */
    public void sumTrackViewWidth(double x) {
        if (trackViewWidth - x < audioWaves.getWidth())
            trackViewWidth = audioWaves.getWidth();
        else
            trackViewWidth -= x;
        updateViews();
    }

    public void sumOffset(float x) {
        sumOffsetNotRel((int) (x * getViewsRatio()));
    }

    public void sumOffsetNotRel(int x) {
        offset += x;
        updateViews();
    }

    void sumPlayBarPos(float x) {
        track.setPlayerBufferPos((int) (track.getPlayerBufferPos() + x * getViewsRatio()));
    }

    void sumRecPos(float x) {
        track.setRecPos((int) (track.getRecPos() + x * getViewsRatio()));
    }

    /**
     * converte una posizione dell'asse X in pixel e restituisce la posizione in samples
     * applicando lo zoom e l'offset
     *
     * @param i
     * @param width
     * @return
     */
    public int fromViewIndexToSamplesIndex(int i, int width) {
        // il rapporto dev'essere approssimato per difetto
        int widthRatio = SupportMath.floorDiv(trackViewWidth, width);

        // viene preso il valore dell'offset piú vicino ad un multiplo del widthRatio
        // per mantenere gli stessi valori durante lo slide della traccia
        int offsetMod = SupportMath.floorMod(offset, widthRatio);

        // centro l'offset per avere uno zoom centrale durante la ScaleGesture
        int start2 = (offsetMod - trackViewWidth / 2);

        // quando é molto zoommato l'approssimazione del widthRatio rende lo zoom scattoso, in questo modo si aggira il problema
        float retWidthRatio = widthRatio <= 18 ? ((float) trackViewWidth / (float) width) : widthRatio;

        //retWidthRatio = ((float) trackViewWidth / (float) width);
        return start2 + (int) (i * retWidthRatio);
    }

    /**
     * prende la posizione di un sample e restituisce la posizione in pixel nella view
     * applicando zoom e offset
     *
     * @param i
     * @param width
     * @return
     */
    public int fromSamplesIndexToViewIndex(int i, int width) {
        double start1 = offset - trackViewWidth / 2f;
        return (int) (((i - start1) / trackViewWidth) * width);
    }

    @Override
    public void setSessionName(String name) {
        sessionName = name;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.setTitle(sessionName);
            }
        });
    }

    @Override
    public String getSessionName() {
        return sessionName;
    }
}
