package net.chigita.vadlite;

import static net.chigita.vadlite.ConfigVad.*;

public class Vad {
    public static void displayVadConfiguration() {
        System.out.println("VAD Parameters");
        System.out.println("FRAME_SIZE_MS : " + FRAME_SIZE_MS);
        System.out.println("NO_OF_SECONDS : " + NO_OF_SECONDS);
        System.out.println("FREQUENCY : " + FREQUENCY);
        System.out.println("SAMPLES_PER_FRAME : " + SAMPLES_PER_FRAME);
        System.out.println("CLASSIFIC_DURATION_MS : " + CLASSIFICATION_DURATION_MS);
        System.out.println("NO_OF_WIN_PER_DURATION : " + NO_OF_WINDOWS_PER_DURATION);
        System.out.println("VOICE_THRESHOLD : " + VOICE_THRESHOLD);
        System.out.println("RMS_THRESHOLD : " + RMS_THRESHOLD);
    }

    /**
     * Classifies all frames
     *
     * @param buffer
     * @param windowSize
     * @return classification
     */
    public static int classifyFrame(short[] buffer, int windowSize) {
        int classification = 0;
        int voiced = 0;  //counts number of speech classifications in window

        //Extract features and classify for each frame
        for (int k = 0; k < windowSize; k += SAMPLES_PER_FRAME) {
            double[] features = FeatureExtractor.ComputeFeaturesForFrame(buffer, SAMPLES_PER_FRAME, k);
            try {
                if (Classifier.Classify(features)) {
                    voiced++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        voiceCount = voiced;

        //Check if number of samples classified as voiced is greater than threshold
        if (voiced >= VOICE_THRESHOLD) {
            classification = 1;
        }

        System.out.println("Voice Count " + voiced);

        return classification;
    }

    /**
     * Checks and returns if sound sample is silence
     *
     * @param buffer
     * @return isSilence
     */
    public static boolean isSilence(short[] buffer) {
        boolean isSilence = true;

        //Calculate energy
        double energy = calculateRMS(buffer);

        //Check if above threshold
        if (energy > RMS_THRESHOLD) {
            isSilence = false;
        }

        System.out.println("RMS : " + energy);
        System.out.println("Silence : " + isSilence);
        return isSilence;
    }

    /**
     * Estimate the RMS of the sound sample
     *
     * @param buffer
     * @return
     */
    public static double calculateRMS(short[] buffer) {

        double min = 1;
        double minRaw = Short.MAX_VALUE;
        double max = -1;
        double meanAbsolute = 0;
        double sumAbsolute = 0;
        double energy = 0;
        double mappedSample;
        int minIndex = -1;
        int i = 0;
        int length = 0;

        for (short sample : buffer) {
            mappedSample = (double) sample / Math.abs(Short.MIN_VALUE);

            if (Math.abs(mappedSample) > DEVICE_NOISE_LEVEL) {
                energy += mappedSample * mappedSample;
                sumAbsolute += Math.abs(mappedSample);

                if (mappedSample < min) {
                    minIndex = i;
                }

                min = Math.min(min, mappedSample);
                max = Math.max(max, mappedSample);
                minRaw = Math.min(minRaw, sample);

                length++;
            }

            i++;
        }

        if (length == 0) {
            return 0;
        }

        double rms = Math.sqrt(energy / length);
        return rms;
    }
}
