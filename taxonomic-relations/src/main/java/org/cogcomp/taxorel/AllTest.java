package org.cogcomp.taxorel;

import org.cogcomp.taxorel.lbjGen.AFRelationClassifier;
import org.cogcomp.taxorel.lbjGen.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xuany
 */
public class AllTest {
    public static void simpleClassifierTest() {
        int correct = 0;
        int predicted = 0;
        int labeled = 0;

        int acc = 0;
        int total = 0;
        try {
            //Ignore this. This is for CV
            for (int fold = 1; fold < 2; fold++) {

                //TODO: Modify Me to correct local path of '20000.new.first8000.shuffled.inter'!
                List<Instance> trainingExamples = DataHandler.readTrainingInstances("/Users/daniel/Dropbox/svn/JupiterData/20000.new.first8000.shuffled.inter", Constants.INPUT_TYPE_INTERMEDIATE);

                //TODO: Modify Me to correct local path of '20000.new.last12000.shuffled.inter'!
                List<Instance> testingExamples = DataHandler.readTestingInstances("/Users/daniel/Dropbox/svn/JupiterData/20000.new.last12000.shuffled.inter", Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ALL);

                AFRelationClassifier afRelationClassifier = new AFRelationClassifier();
                Label judge = new Label();
                double largestPMI = 0.0;
                for (Instance ins : trainingExamples) {
                    if (ins.scorePmi_E1E2 > largestPMI)
                        largestPMI = ins.scorePmi_E1E2;
                }
                for (Instance ins : trainingExamples) {
                    ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
                }
                for (Instance ins : testingExamples) {
                    ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
                }
                for (int i = 0; i < 5000; i++) {
                    for (int ii = 0; ii < trainingExamples.size(); ii++) {
                        Instance ins = trainingExamples.get(ii);
                        afRelationClassifier.learn(ins);
                    }
                }
                afRelationClassifier.doneLearning();
                for (Instance instance : testingExamples) {
                    total++;
                    String tag = afRelationClassifier.discreteValue(instance);
                    if (tag.equals(judge.discreteValue(instance))) {
                        acc++;
                        if (!tag.equals("0")) {
                            correct++;
                        }
                    }
                    if (!tag.equals("0")) {
                        predicted++;
                    }
                    if (!judge.discreteValue(instance).equals("0")) {
                        labeled++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double p = (double) correct / (double) predicted;
        double r = (double) correct / (double) labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Accuracy: " + (double) acc / (double) total);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
    }

    public static void testWithConstraints() {

        //TODO: Modify Me to correct local path of '20000.new.last12000.beyondwiki.shuffled.relatedconcept.555.inter'!
        String supportingInterFile = "data/jupiter/data/www10/K3/20000.new.last12000.beyondwiki.shuffled.relatedconcept.555.inter";

        //TODO: Modify Me to correct local path of '20000.new.last12000.beyondwiki.shuffled.expanded.inter'!
        String interFile = "data/jupiter/data/www10/K3/20000.new.last12000.beyondwiki.shuffled.expanded.inter";

        //TODO: Modify Me to correct local path of '20000.new.first8000.shuffled.inter'!
        String trainFile = "data/jupiter/data/www10/K3/20000.new.first8000.shuffled.inter";

        try {
            AFRelationClassifier afRelationClassifier = new AFRelationClassifier();
            List<Instance> trainingExamples = DataHandler.readTrainingInstances(trainFile, Constants.INPUT_TYPE_INTERMEDIATE);
            double largestPMI = 0.0;
            for (Instance ins : trainingExamples) {
                if (ins.scorePmi_E1E2 > largestPMI)
                    largestPMI = ins.scorePmi_E1E2;
            }
            for (Instance ins : trainingExamples) {
                ins.scorePmi_E1E2 = ins.scorePmi_E1E2 / largestPMI;
            }
            for (int i = 0; i < 5000; i++) {
                for (Instance instance : trainingExamples) {
                    afRelationClassifier.learn(instance);
                }
            }
            String pmi = Double.toString(largestPMI);
            DataHandler.writeContent(pmi, Constraints.PMI_FILE);
            ArrayList<Instance> arrSupportingInstances = DataHandler
                    .readTestingInstances(supportingInterFile,
                            Constants.INPUT_TYPE_INTERMEDIATE,
                            DataHandler.READ_ALL);

            Map<String, Double[]> mapSupportingPrediction = Constraints.classifySupportingInstances(
                    arrSupportingInstances, afRelationClassifier);

            ArrayList<Instance> arrInstances = DataHandler
                    .readExtendedTestingInstances(interFile,
                            Constants.INPUT_TYPE_INTERMEDIATE,
                            DataHandler.READ_ALL);

            double result = Constraints.classifyOriginalInstances(arrInstances,
                    mapSupportingPrediction, afRelationClassifier, 4, 5,
                    4, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        simpleClassifierTest();
        //testWithConstraints();
    }
}
