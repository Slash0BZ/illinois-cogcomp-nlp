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
        /*
        for (Instance instance : arrInputInstances) {
            ArrayList<Instance> arrOutputInstances = new ArrayList<Instance>();

            System.out.println(i - 1 + "/" + totalSize + " done.");
            System.out.println("Starting: " + instance.entity1 + " - "
                    + instance.entity2);
            if (i > startIdx) {
                featureExtractor.extractInstance(instance);
                arrOutputInstances.add(instance);
                ArrayList<String> arrStringInstances = DataHandler.makeStringInstances(
                        arrOutputInstances, Constants.INPUT_TYPE_INTERMEDIATE);
                DataHandler.writeLines(arrStringInstances, output);
            }
            i++;
        }
        */
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
                DataHandler.writeLines(arrStringInstances, "data/jupiter/DataI/train.new.afterchanges.errors");
            }
            System.out.println("Current Acc: " + (double)correct / (double)count);
            System.out.println();

        }

    }

    public static void main(String[] args) {
        //simpleClassifierTest();
        //testWithConstraints();
        //featureExtractionTest();
        //FeatureExtractor.mostMatch("A", "V-C");

        FeatureExtractor featureExtractor = new FeatureExtractor();
        System.out.println(featureExtractor._LLMSim.compare("Gnosticism", "Platonism"));
        //System.out.println(English.plural("actors", 2));
        //NameConverter nameConverter = new DefaultNameConverter();
        //System.out.println(featureExtractor._wordSim.compare("countries", "country").score);
        //System.out.println(featureExtractor._wordSim.compare("actor", "actresses").score);
        //System.out.println(WikiHandler.getContentByTitle("List_of_rivers_by_discharge"));
        //System.out.println(WikiHandler.getParentCategory("Capitals"));
        //System.out.println(WikiHandler.getInfoFromTitle("Marguerite Zorach").categories);
        //List<String> test = new ArrayList<>();
        //test.add("Multiple myeloma");
        //System.out.println(featureExtractor.extract(test, 0, 0));
        Instance i = new Instance("newspaper", "news frankfurt");
        //System.out.println(featureExtractor.settleEntity(i.entity1, i.entity2, new ArrayList<>(), new ArrayList<>()));
        try {
            generateIntermediateFile("data/jupiter/DataI/train", "data/jupiter/DataI/train.new.2.inter", 430);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
