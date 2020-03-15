package net.chigita.vadlite;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class Main {
    private static int speechCount = 0;
    private static int noiseCount = 0;
    private static int totalCount = 0;

    public static void main(String[] args) {
        Vad.displayVadConfiguration();

        String filePath = args[0];

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("InputFile -> " + filePath);

        int read;
        byte[] buff = new byte[1024];
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] audioBytes = out.toByteArray();
        ShortBuffer sbuf = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] audioShorts = new short[sbuf.capacity()];
        sbuf.get(audioShorts);
        executeVadLite(audioShorts);
    }

    public static void executeVadLite(short[] data) {
        try {
            int bufferSize = ConfigVad.FREQUENCY * ConfigVad.NO_OF_SECONDS;
            short[] buffer = new short[bufferSize];
            int offset = 0;
            for (int i = 0; i < data.length / bufferSize; i++) {
                for (int j = 0; j < bufferSize; ++j) {
                    buffer[j] = data[i * bufferSize + j];
                }
                executeVadLiteOneSegment(buffer, bufferSize);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executeVadLiteOneSegment(short[] buffer, int windowSize) {
        int classification = 2;
        boolean checkSilence = Vad.isSilence(buffer);

        if (!checkSilence) {
            System.out.println("Classify Buffer length " + buffer.length);
            totalCount++;
            classification = Vad.classifyFrame(buffer, windowSize);
        } else {
            ConfigVad.voiceCount = -1;
        }

        if (classification == 1) {
            System.out.println("Speaking");
            speechCount++;
        } else if (classification == 0) {
            System.out.println("Noise");
            noiseCount++;
        } else {
            System.out.println("Silence");
        }

        System.out.println("Speech Count: " + speechCount + " Perc: " + (100 * speechCount) / totalCount + "%");
        System.out.println("Noise Count: " + noiseCount + " Perc: " + (100 * noiseCount) / totalCount + "%");

    }
}
