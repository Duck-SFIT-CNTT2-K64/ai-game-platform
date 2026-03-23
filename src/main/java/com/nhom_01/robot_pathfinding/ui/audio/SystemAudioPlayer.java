package com.nhom_01.robot_pathfinding.ui.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Uses Java Sound API (javax.sound.sampled) as alternative to JavaFX media,
 * with better MP3 support via mp3spi library.
 */
public class SystemAudioPlayer {
    private static Clip currentClip;
    private static Thread loopThread;
    private static volatile boolean shouldLoop;
    private static float volume = 1.0f;
    private static String currentResourcePath;

    public static synchronized void playLooping(String resourcePath) {
        System.out.println("[SystemAudio] Attempting to load and play: " + resourcePath);

        // Keep playback continuous when navigating between menu pages.
        if (isCurrentTrackPlaying(resourcePath)) {
            setVolume(volume);
            if (!currentClip.isRunning()) {
                currentClip.start();
            }
            System.out.println("[SystemAudio] Same track already active, continuing without restart");
            return;
        }

        stopLooping();

        try (InputStream is = SystemAudioPlayer.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.out.println("[SystemAudio] Resource not found: " + resourcePath);
                return;
            }

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);
            
            AudioFormat format = audioStream.getFormat();
            System.out.println("[SystemAudio] Original format: " + format.getEncoding() + 
                             ", SampleRate: " + format.getSampleRate() + 
                             ", Channels: " + format.getChannels() +
                             ", Bits: " + format.getSampleSizeInBits());

            // Try to find a supported format, or convert if needed
            AudioInputStream targetStream = audioStream;
            AudioFormat targetFormat = format;
            
            try {
                // Try to get a compatible PCM format (16-bit is most universally supported)
                targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,  // 16-bit instead of 24-bit
                    format.getChannels(),
                    format.getChannels() * 2,  // frame size: 2 bytes per sample per channel
                    format.getSampleRate(),
                    false  // little-endian
                );
                
                DataLine.Info info = new DataLine.Info(Clip.class, targetFormat);
                
                if (AudioSystem.isLineSupported(info)) {
                    System.out.println("[SystemAudio] Converting to 16-bit PCM...");
                    targetStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                    currentClip = (Clip) AudioSystem.getLine(info);
                    currentClip.open(targetStream);
                } else {
                    // If 16-bit still not supported, try big-endian
                    targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        format.getSampleRate(),
                        16,
                        format.getChannels(),
                        format.getChannels() * 2,
                        format.getSampleRate(),
                        true  // big-endian
                    );
                    info = new DataLine.Info(Clip.class, targetFormat);
                    if (AudioSystem.isLineSupported(info)) {
                        System.out.println("[SystemAudio] Converting to 16-bit PCM (big-endian)...");
                        targetStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                        currentClip = (Clip) AudioSystem.getLine(info);
                        currentClip.open(targetStream);
                    } else {
                        // Last resort: use any available format
                        System.out.println("[SystemAudio] Trying standard PCM_SIGNED format...");
                        targetFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            44100.0f,
                            16,
                            2,
                            4,
                            44100.0f,
                            false
                        );
                        DataLine.Info fallbackInfo = new DataLine.Info(Clip.class, targetFormat);
                        targetStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                        currentClip = (Clip) AudioSystem.getLine(fallbackInfo);
                        currentClip.open(targetStream);
                    }
                }
            } catch (Exception e1) {
                System.out.println("[SystemAudio] Format conversion failed, trying original: " + e1.getMessage());
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                currentClip = (Clip) AudioSystem.getLine(info);
                currentClip.open(audioStream);
            }

            setVolume(volume);
            currentClip.start();
            currentResourcePath = resourcePath;
            
            System.out.println("[SystemAudio] Playback started successfully");
            
            // Start loop thread
            shouldLoop = true;
            loopThread = new Thread(() -> {
                try {
                    while (shouldLoop) {
                        if (currentClip != null && !currentClip.isRunning() && !currentClip.isActive()) {
                            Thread.sleep(100);
                            if (shouldLoop && currentClip != null) {
                                currentClip.setFramePosition(0);
                                currentClip.start();
                                System.out.println("[SystemAudio] Loop restarted");
                            }
                        } else {
                            Thread.sleep(500);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            loopThread.setDaemon(true);
            loopThread.start();

        } catch (UnsupportedAudioFileException ex) {
            System.out.println("[SystemAudio] Unsupported audio format: " + ex.getMessage());
            ex.printStackTrace();
        } catch (LineUnavailableException ex) {
            System.out.println("[SystemAudio] Audio line unavailable: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("[SystemAudio] IO Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static synchronized void setVolume(float vol) {
        volume = Math.max(0.0f, Math.min(1.0f, vol));
        
        if (currentClip != null) {
            FloatControl volumeControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            // Convert volume [0.0, 1.0] to dB range
            float dB = 20.0f * (float) Math.log10(volume);
            dB = Math.max(volumeControl.getMinimum(), Math.min(volumeControl.getMaximum(), dB));
            volumeControl.setValue(dB);
            System.out.println("[SystemAudio] Volume set to: " + volume + " (" + dB + " dB)");
        }
    }

    public static synchronized void stopLooping() {
        shouldLoop = false;
        
        if (currentClip != null) {
            try {
                currentClip.stop();
                currentClip.close();
            } catch (Exception ex) {
                System.out.println("[SystemAudio] Error closing clip: " + ex.getMessage());
            }
            currentClip = null;
        }

        currentResourcePath = null;
        
        if (loopThread != null && loopThread.isAlive()) {
            try {
                loopThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static boolean isCurrentTrackPlaying(String resourcePath) {
        return currentClip != null
            && shouldLoop
            && resourcePath != null
            && resourcePath.equals(currentResourcePath)
            && currentClip.isOpen();
    }
}
