/**
 *
 */
package org.cogcomp.taxorel;

import java.util.ArrayList;
import java.util.Vector;

/**
 * @author dxquang Apr 22, 2009
 */
public class CosineSimilarity {

    public static double getSimilarity(Vector<Double> vector1,
                                       Vector<Double> vector2) throws Exception {

        if (vector2.size() != vector1.size()) {
            throw new Exception();
        }

        double nominator = dotProduct(vector1, vector2);

        double denominator = magnitude(vector1) * magnitude(vector2);

        if (denominator == 0.0)
            return 0.0;

        double similarity = nominator / denominator;

        return similarity;

    }

    public static double getSimilarity(ArrayList<Integer> vector1,
                                       ArrayList<Integer> vector2) throws Exception {

        if (vector2.size() != vector1.size()) {
            throw new Exception();
        }

        double nominator = dotProduct(vector1, vector2);

        double denominator = magnitude(vector1) * magnitude(vector2);

        if (denominator == 0.0)
            return 0.0;

        double similarity = nominator / denominator;

        return similarity;
    }

    /**
     * @param vector1
     * @return
     */
    private static double magnitude(ArrayList<Integer> vector) {

        int sum = 0;
        for (Integer i : vector) {
            sum += i * i;
        }

        return Math.sqrt((double) sum);
    }

    /**
     * @param vector
     * @return
     */
    private static double magnitude(Vector<Double> vector) {

        double sum = 0;
        for (Double i : vector) {
            sum += i * i;
        }

        return Math.sqrt(sum);
    }

    /**
     * @param vector1
     * @param vector2
     * @return
     */
    private static double dotProduct(ArrayList<Integer> vector1,
                                     ArrayList<Integer> vector2) {

        int n = vector1.size();

        int sum = 0;

        for (int i = 0; i < n; i++) {
            sum += vector1.get(i) * vector2.get(i);
        }

        return (double) sum;
    }

    /**
     * @param vector1
     * @param vector2
     * @return
     */
    private static double dotProduct(Vector<Double> vector1,
                                     Vector<Double> vector2) {

        int n = vector1.size();

        double sum = 0;

        for (int i = 0; i < n; i++) {
            sum += vector1.get(i) * vector2.get(i);
        }

        return sum;
    }
}