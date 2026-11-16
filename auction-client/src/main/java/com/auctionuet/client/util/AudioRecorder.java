package com.auctionuet.client.util;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import java.io.File;
import java.io.IOException;

/**
 * Ghi âm microphone ra WAV 16 kHz, 16-bit, mono cho nút nói trong phòng đấu giá.
 */
public class AudioRecorder {
    private final File audioFile;
    private final AudioFormat format;
    private TargetDataLine line;
    private Thread recordThread;

    public AudioRecorder(File outputFile) {
        this.audioFile = outputFile;
        this.format = new AudioFormat(16000, 16, 1, true, false);
    }

    public void startRecording() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone không hỗ trợ định dạng 16 kHz mono.");
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        recordThread = new Thread(() -> {
            try (AudioInputStream input = new AudioInputStream(line)) {
                AudioSystem.write(input, AudioFileFormat.Type.WAVE, audioFile);
            } catch (IOException ignored) {
                // Luồng ghi kết thúc khi microphone được đóng.
            }
        });
        recordThread.setName("Audio-Recorder-Thread");
        recordThread.start();
    }

    public void stopRecording() {
        if (line != null) {
            line.stop();
            line.close();
        }
        if (recordThread != null && recordThread.isAlive()) {
            try {
                recordThread.join(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
