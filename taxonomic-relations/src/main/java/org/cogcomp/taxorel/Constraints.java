package org.cogcomp.taxorel;

import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.Softmax;
import org.cogcomp.taxorel.lbjGen.AFRelationClassifier;

import java.io.BufferedReader;
import java.util.*;

/**
 * @author xuany
 */
public class Constraints {

    public static String PMI_FILE = "/Users/daniel/Dropbox/svn/JupiterData/pmi_value.txt";
    public static String CONSTRAINTS_WEIGHT = "/Users/daniel/Dropbox/svn/JupiterData/constraints.weight.txt";

    public final static String[] invalidCombinationRankedForward = new String[]{
            "1_3_3", "3_1_3", "3_2_3", "0_2_3", "3_2_0", "1_0_3", "2_2_2",
            "2_0_3", "1_3_0", "1_3_1", "3_0_0", "2_3_3", "2_1_0", "1_1_0",
            "0_3_3", "0_3_2", "3_3_1", "2_3_0", "3_3_0", "3_0_3"};

    public static double getLargestPMI(String pmiFile) {
        ArrayList<String> arrLines = DataHandler.readLines(pmiFile);
        String pmi = arrLines.get(0);
        pmi = pmi.trim();
        return Double.parseDouble(pmi);
    }

    public static Map<String, Double[]> classifySupportingInstances(
            ArrayList<Instance> arrSupportingInstances,
            AFRelationClassifier localClassifier) {

        Map<String, Double[]> mapResults = new HashMap<String, Double[]>();

        double largestPMI = getLargestPMI(PMI_FILE);

        for (Instance ins : arrSupportingInstances) {
            ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
        }

        Softmax sm = new Softmax();
        for (Instance ins : arrSupportingInstances) {

            String key = ins.entity1 + "___" + ins.entity2;

            if (mapResults.containsKey(key))
                continue;

            Double[] scores = new Double[5];
            String orgLabel = localClassifier.discreteValue(ins);
            ScoreSet scoreSet = localClassifier.scores(ins);
            sm.normalize(scoreSet);
            scores[0] = scoreSet.get("0");
            scores[1] = scoreSet.get("1");
            scores[2] = scoreSet.get("2");
            scores[3] = scoreSet.get("3");
            scores[4] = Double.parseDouble(orgLabel);
            mapResults.put(key, scores);
        }

        return mapResults;

    }

    public static void getSupportingConcept(int maxAnc, String[] concepts,
                                            Set<String> setSupportingConcepts) {
        int n = concepts.length;
        int i = 0;
        int count = 0;
        while (i < n && count < maxAnc) {
            if (setSupportingConcepts.contains(concepts[i])) {
                i++;
                continue;
            }
            setSupportingConcepts.add(concepts[i]);
            i++;
            count++;
        }
    }

    public static boolean violateConstraints(String key) {
        Set<String> setInvalidCombinations = new HashSet<String>();
        for (String k : invalidCombinationRankedForward) {
            setInvalidCombinations.add(k);
        }
        if (setInvalidCombinations.contains(key)) {
            return true;
        }
        return false;
    }

    public static void sortInferenceOutput(ArrayList<InferenceOutput> arrOutputs) {
        Collections.sort(arrOutputs, (o1, o2) -> Double.compare(o2.value, o1.value));
    }

    public static ArrayList<InferenceOutput> bruteforthConstraintSatisfactionInference(
            Double[] softmaxXYs, Double[] softmaxXZs, Double[] softmaxYZs,
            boolean debug) {

        ArrayList<InferenceOutput> arrOutputs = new ArrayList<InferenceOutput>();

        int sizeC1 = 4; // softmaxXYs.length;
        int sizeC2 = 4; // softmaxXZs.length;
        int sizeC3 = 4; // softmaxYZs.length;

        int n = 0;
        for (int i = 0; i < sizeC1; i++) {
            for (int j = 0; j < sizeC2; j++) {
                for (int k = 0; k < sizeC3; k++) {
                    String key = Integer.toString(i) + "_"
                            + Integer.toString(j) + "_" + Integer.toString(k);
                    if (!violateConstraints(key)) {
                        double value = softmaxXYs[i] + softmaxXZs[j]
                                + softmaxYZs[k];
                        InferenceOutput output = new InferenceOutput(key, value);
                        arrOutputs.add(output);
                        n++;
                        if (debug) {
                            System.out.println("\t" + key + ": valid" + " (" + value + ")");
                        }
                    } else {
                        double value = softmaxXYs[i] + softmaxXZs[j]
                                + softmaxYZs[k];
                        if (debug) {
                            System.out.println("\t" + key + ": invalid" + " ("
                                    + value + ")");
                        }
                    }
                }
            }
        }

        sortInferenceOutput(arrOutputs);

        return arrOutputs;
    }

    public static double classifyOriginalInstances(ArrayList<Instance> arrInstances,
                                                   Map<String, Double[]> mapSupportingPrediction,
                                                   AFRelationClassifier localClassifier, int maxAnc, int maxSib,
                                                   int maxChi, boolean debug) {

        double largestPMI = getLargestPMI(PMI_FILE);

        Map<String, Double> mapConstraintProbs = new HashMap<>();
        BufferedReader reader = DataHandler.openReader(CONSTRAINTS_WEIGHT);
        String line2;
        try {
            while ((line2 = reader.readLine()) != null) {
                line2 = line2.trim();
                String chunks[] = line2.split("\\t+");

                if (chunks.length != 4)
                    continue;

                String prob = chunks[0].trim();
                String logProb = chunks[1].trim();
                String negLogProb = chunks[2].trim();

                String combination = chunks[3].trim();

                mapConstraintProbs.put(combination, Double.parseDouble(prob));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataHandler.closeReader(reader);

        for (Instance ins : arrInstances) {
            ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
        }

        ArrayList<String> truePrediction = new ArrayList<>();
        ArrayList<String> falsePrediction = new ArrayList<>();
        int count = 0;
        int labeled = 0;
        int predicted = 0;
        int correct = 0;

        Softmax sm = new Softmax();
        int n = arrInstances.size();
        for (int i = 0; i < n; i++) {
            Instance ins = arrInstances.get(i);

            String line = ins.textLine;

            String[] parts = line.split("\\t+");

            String conceptX = parts[2];
            String conceptY = parts[3];

            if (debug) {
                System.out.println();
                System.out.println("ConceptX: " + conceptX);
                System.out.println("ConceptY: " + conceptY);
            }

            if (!ins.entity1.equals(conceptX) || !ins.entity2.equals(conceptY)) {
                System.out.println("ERROR: Different concepts.");
                System.out.println("ins.entity1: " + ins.entity1);
                System.out.println("ins.entity2: " + ins.entity2);
                System.out.println("conceptX: " + conceptX);
                System.out.println("conceptY: " + conceptY);
                System.exit(1);
            }

            String ancX = parts[12];
            String[] ancXs = ancX.split("_");
            String sibX = parts[13];
            String[] sibXs = sibX.split("_");
            String chiX = parts[14];
            String[] chiXs = chiX.split("_");

            String ancY = parts[15];
            String[] ancYs = ancY.split("_");
            String sibY = parts[16];
            String[] sibYs = sibY.split("_");
            String chiY = parts[17];
            String[] chiYs = chiY.split("_");

            Set<String> setSupportingConcepts = new HashSet<>();

            getSupportingConcept(maxAnc, ancXs, setSupportingConcepts);
            getSupportingConcept(maxAnc, ancYs, setSupportingConcepts);
            getSupportingConcept(maxSib, sibXs, setSupportingConcepts);
            getSupportingConcept(maxSib, sibYs, setSupportingConcepts);
            getSupportingConcept(maxChi, chiXs, setSupportingConcepts);
            getSupportingConcept(maxChi, chiYs, setSupportingConcepts);

            Double[] scoreXYs = new Double[4];
            String orgLabel = localClassifier.discreteValue(ins);
            ScoreSet scoreSet = localClassifier.scores(ins);
            sm.normalize(scoreSet);
            scoreXYs[0] = scoreSet.get("0");
            scoreXYs[1] = scoreSet.get("1");
            scoreXYs[2] = scoreSet.get("2");
            scoreXYs[3] = scoreSet.get("3");

            if (debug) {
                System.out.println("Original label: " + orgLabel);
                System.out.println("0: " + scoreXYs[0]);
                System.out.println("1: " + scoreXYs[1]);
                System.out.println("2: " + scoreXYs[2]);
                System.out.println("3: " + scoreXYs[3]);
            }

            Map<String, Double> mapClassScore = new HashMap<String, Double>();
            Map<String, Integer> mapClassFreq = new HashMap<String, Integer>();

            for (String z : setSupportingConcepts) {

                if (debug) {
                    System.out.println("\tConcept Z: " + z);
                }

                String key = ins.entity1 + "___" + z;
                if (!mapSupportingPrediction.containsKey(key)) {
                    continue;
                }
                Double[] scoreXZs = mapSupportingPrediction.get(key);

                if (debug) {
                    System.out.println("\t*" + key);
                    for (int k = 0; k < scoreXZs.length; k++) {
                        System.out.println("\t\t" + k + ":" + scoreXZs[k]);
                    }
                }

                key = ins.entity2 + "___" + z;
                if (!mapSupportingPrediction.containsKey(key)) {
                    continue;
                }
                Double[] scoreYZs = mapSupportingPrediction.get(key);

                if (debug) {
                    System.out.println("\t*" + key);
                    for (int k = 0; k < scoreYZs.length; k++) {
                        System.out.println("\t\t" + k + ":" + scoreYZs[k]);
                    }
                }

                if (scoreXZs[4] == 0.0 && scoreYZs[4] == 0.0) {
                    if (debug) {
                        System.out.println("\tBoth scores are 0.0");
                    }
                    continue;
                }

                ArrayList<InferenceOutput> arrInferenceOutputs = bruteforthConstraintSatisfactionInference(
                        scoreXYs, scoreXZs, scoreYZs, debug);

                // Incorporate soft constraints
                for (InferenceOutput output : arrInferenceOutputs) {
                    String s = output.key;
                    output.value = output.value * mapConstraintProbs.get(s);
                }

                if (arrInferenceOutputs.size() > 0) {
                    InferenceOutput output = arrInferenceOutputs.get(0);
                    String combination = output.key;
                    String relation = combination.substring(0, 1);
                    double score = output.value;
                    if (mapClassScore.containsKey(relation)) {
                        double total = mapClassScore.get(relation) + score;
                        mapClassScore.put(relation, total);
                        int freq = mapClassFreq.get(relation) + 1;
                        mapClassFreq.put(relation, freq);
                    } else {
                        mapClassScore.put(relation, score);
                        mapClassFreq.put(relation, 1);
                    }
                    for (int k = 0; (k < arrInferenceOutputs.size() && k < 5); k++) {
                        InferenceOutput inferenceOutput = arrInferenceOutputs
                                .get(k);
                        if (debug) {
                            System.out.println(inferenceOutput.key + " ("
                                    + inferenceOutput.value + ")");
                        }
                    }
                }
            }

            Set<String> setRelations = mapClassScore.keySet();

            for (String relation : setRelations) {
                int freq = mapClassFreq.get(relation);
                double score = mapClassScore.get(relation);
                double newScore = (score) / ((double) freq);
                mapClassScore.put(relation, newScore);

                if (debug) {
                    System.out.println(relation + ": " + score + " " + freq
                            + " - " + newScore);
                }
            }

            String maxRelation = orgLabel;
            double maxScore = -100000;
            for (String relation : setRelations) {
                double score = mapClassScore.get(relation);
                if (score > maxScore) {
                    maxRelation = relation;
                    maxScore = score;
                }
            }

            int p = Integer.parseInt(maxRelation);
            if (p != 0) {
                predicted++;
            }
            if (ins.relation != 0) {
                labeled++;
            }
            if (p == ins.relation) {
                String out = "T" + "\t" + p + "\t" + orgLabel + "\t"
                        + ins.toString();
                truePrediction.add(out);
                count++;
                if (p != 0) {
                    correct++;
                }

                if (debug) {
                    System.out.println(out);
                }

            } else {
                String out = "F" + "\t" + p + "\t" + orgLabel + "\t"
                        + ins.toString();
                falsePrediction.add(out);

                if (debug) {
                    System.out.println(out);
                }
            }

        }

        if (debug) {
            for (String out : truePrediction)
                System.out.println(out);
            for (String out : falsePrediction)
                System.out.println(out);

            System.out.println("Correct: " + count);
            System.out.println("Total: " + n);
        }

        double acc = 0.0;
        if (arrInstances.size() > 0)
            acc = (double) count / (double) n;

        System.out.println("Accuracy: " + acc);

        double p = (double) correct / (double) predicted;
        double r = (double) correct / (double) labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        return acc;

    }
}
