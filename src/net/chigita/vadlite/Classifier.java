package net.chigita.vadlite;

import static net.chigita.vadlite.ConfigVad.*;

public class Classifier {

    //Perform classification
    public static boolean Classify(double[] features) {
        boolean classification = false;
        double coeff, y;
        double wx = 0; //sum of coeefficient x features

        if (shouldNormalize == true) {
            features = normalizeFeatures(features);
        }

        //
        //Implement decision function of linear SVM: y = wx +b
        for (int i = 0; i < COEFFICIENTS.length; i++) {
            coeff = COEFFICIENTS[i];
            wx += (coeff * features[i]);
        }

        y = wx + INTERCEPT;

        if (y > 0) {
            classification = true;
        }

        //Return result
        return classification;
    }


    public static double[] normalizeFeatures(double[] features) {
        double[] normFeatures = new double[features.length - 1];

        for (int i = 0; i < features.length - 1; i++) {
            normFeatures[i] = (features[i + 1] - MEAN[i]) / SCALER[i];
        }

        return normFeatures;
    }
}

