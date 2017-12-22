/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.taxorel;

import org.cogcomp.taxorel.lbjGen.AFRelationClassifier;
import org.cogcomp.taxorel.lbjGen.Label;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
                List<Instance> trainingExamples = DataHandler.readTrainingInstances("data/jupiter/DataI/train.tmp.inter", Constants.INPUT_TYPE_INTERMEDIATE);

                //TODO: Modify Me to correct local path of '20000.new.last12000.shuffled.inter'!
                List<Instance> testingExamples = DataHandler.readTestingInstances("data/jupiter/DataI/test.tmp.inter", Constants.INPUT_TYPE_INTERMEDIATE, DataHandler.READ_ONLY_WIKI);

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

    public static void featureExtractionTest(){
        Instance instance = new Instance("newspaper", "tobago news");
        FeatureExtractor featureExtractor = new FeatureExtractor();
        featureExtractor.extractInstance(instance);
        System.out.println("ratio_ttlcat: " + instance.ratio_TtlCat);
        System.out.println("ratio_catttl: " + instance.ratio_CatTtl);
        System.out.println("ratio_catcat: " + instance.ratio_CatCat);
        System.out.println("pmi: " + instance.scorePmi_E1E2);
        System.out.println("abscat: " + instance.scoreCos_AbsCat);
        System.out.println("catabs: " + instance.scoreCos_CatAbs);
        System.out.println("catcat: " + instance.scoreCos_CatCat);
        System.out.println("absabs: " + instance.scoreCos_AbsAbs);
    }

    public static void generateIntermediateFile(String input, String output, int startIdx) throws Exception{
        ArrayList<Instance> arrInputInstances = DataHandler
                .readTrainingInstances(input,
                        Constants.INPUT_TYPE_GOLD);



        int totalSize = arrInputInstances.size();

        int i = 1;

        FeatureExtractor featureExtractor = new FeatureExtractor();
        int count = 0;
        int correct = 0;
        for (Instance instance : arrInputInstances){
            i++;
            if (i < startIdx){
                continue;
            }
            String prediction = featureExtractor.settleEntity(instance.entity1, instance.entity2, new ArrayList<>(), new ArrayList<>());

            int intPrediction = Integer.parseInt(prediction);
            if (intPrediction == -1){
                continue;
            }
            count ++;
            if (intPrediction == instance.relation){
                correct ++;
            }
            else {
                System.out.println("[WRONG]: " + instance.entity1 + "-" + instance.entity2 + " " + prediction);
                ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();
                arrOutputInstances.add(instance);
                ArrayList<String> arrStringInstances = DataHandler.makeStringInstances(
                        arrOutputInstances, Constants.INPUT_TYPE_INTERMEDIATE);
                DataHandler.writeLines(arrStringInstances, output);
            }
            System.out.println("Current Acc: " + (double)correct / (double)count);
            System.out.println();

        }

    }

    public static void testFIGER() throws IOException{
        MentionReader mentionReader = MentionReader.getMentionReader("data/FIGER/train.data.gz");
        EntityProtos.Mention m;
        FeatureExtractor featureExtractor = new FeatureExtractor();
        Random random = new Random();
        while ((m = mentionReader.readMention()) != null){
            if (random.nextDouble() > 0.01){
                continue;
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("data/FIGER/improved.3.out.txt", true));
            bufferedWriter.write(m.getEntityName() + " : " + featureExtractor.typer(m.getEntityName()) + " : " + m.getLabelsList() + "\n");
            bufferedWriter.close();
        }
    }

    public static void main(String[] args) {
        //simpleClassifierTest();
        //testWithConstraints();
        //featureExtractionTest();
        try {
            //testFIGER();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        //FeatureExtractor featureExtractor = new FeatureExtractor();
        //System.out.println(featureExtractor.settleEntity("Lebron James", "athlete", new ArrayList<>(), new ArrayList<>()));
        //System.out.println(featureExtractor.typer("Lebron James"));
        //WikiHandler.exportToMapDB();
        try {
            generateIntermediateFile("data/jupiter/DataII/test", "data/jupiter/DataII/test.errors", 0);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
