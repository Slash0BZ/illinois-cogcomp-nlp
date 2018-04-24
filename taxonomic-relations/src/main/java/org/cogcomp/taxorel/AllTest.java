/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.taxorel;

import org.cogcomp.taxorel.lbjGen.AFRelationClassifier;
import org.cogcomp.taxorel.lbjGen.FIGERTypingClassifier;
import org.cogcomp.taxorel.lbjGen.Label;
import org.cogcomp.taxorel.lbjGen.TypingClassifier;

import java.io.*;
import java.util.*;

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
                //continue;
            }
            System.out.println(m.getEntityName());
            //System.out.println(m.getDefaultInstanceForType());
            System.out.println(m.getLabelsList());
            System.out.println(m.getFileid());
            System.out.println();
            //BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("data/FIGER/examples.txt", true));
            //bufferedWriter.write(m.getEntityName() + " : " + featureExtractor.typer(m.getEntityName()) + " : " + m.getLabelsList() + "\n");
            //bufferedWriter.write(m.getEntityName() + "\n");
            //bufferedWriter.close();
        }
    }

    public static void testConll() throws Exception{
        int train_size = 200;
        FeatureExtractor featureExtractor = new FeatureExtractor();
        TypingClassifier classifier = new TypingClassifier();
        WikiMentionContainer wikiMentionContainer = WikiMentionIOHelper.serializeDataIn("data/conll/cachedWikiMentions");
        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < train_size; i++) {
                WikiMention train_instance = wikiMentionContainer.mentions.get(i);
                classifier.learn(train_instance);
            }
            classifier.doneWithRound();
        }
        classifier.doneLearning();

        int[] correct = new int[3];
        int[] predicted = new int[3];
        int[] labeled = new int[3];

        for (int i = 0; i < 3; i++){
            correct[i] = 0;
            predicted[i] = 0;
            labeled[i] = 0;
        }

        for (int i = train_size; i < wikiMentionContainer.mentions.size(); i++){
            WikiMention test_instance = wikiMentionContainer.mentions.get(i);
            String predictedTag = classifier.discreteValue(test_instance);
            if (test_instance._type.equals("PER")){
                labeled[0] ++;
            }
            if (predictedTag.equals("PER")){
                predicted[0] ++;
                if (test_instance._type.equals(predictedTag)) {
                    correct[0] ++;
                }
            }
            if (test_instance._type.equals("ORG")){
                labeled[1] ++;
            }
            if (predictedTag.equals("ORG")){
                predicted[1] ++;
                if (test_instance._type.equals(predictedTag)) {
                    correct[1] ++;
                }
            }
            if (test_instance._type.equals("LOC")){
                labeled[2] ++;
            }
            if (predictedTag.equals("LOC")){
                predicted[2] ++;
                if (test_instance._type.equals(predictedTag)) {
                    correct[2] ++;
                }
            }
            //System.out.println(test_instance._title + ", [GOLD]" + test_instance._type + ", [SUPERVISED]" + predictedTag + ", [RECURSIVE]" + featureExtractor.typer(test_instance.categories));
        }
        double totalCorrect = 0;
        for (int i = 0; i < 3; i++){
            totalCorrect += (double)correct[i];
            double p = (double)correct[i] / (double)predicted[i];
            double r = (double)correct[i] / (double)labeled[i];
            double f = p * r * 2 / (p + r);
            System.out.println(p + ", " + r + ", " + f);
        }
        System.out.println("Accuracy: " + totalCorrect / (double)(wikiMentionContainer.mentions.size() - train_size));
    }


    public static void testMajorVote() throws Exception{
        int[] correct = new int[3];
        int[] predicted = new int[3];
        int[] labeled = new int[3];

        for (int i = 0; i < 3; i++){
            correct[i] = 0;
            predicted[i] = 0;
            labeled[i] = 0;
        }
        File conllGold = new File("data/conll/majorvote.output");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(conllGold));
        String line;
        while ((line = bufferedReader.readLine()) != null){
            String[] lineGroup = line.split(",");
            String goldTag = lineGroup[1].substring(7);
            String predictedTag = lineGroup[3].substring(12);
            if (goldTag.equals("PER")){
                labeled[0] ++;
            }
            if (predictedTag.equals("Person:")){
                predicted[0] ++;
                if (goldTag.equals("PER")) {
                    correct[0] ++;
                }
            }
            if (goldTag.equals("ORG")){
                labeled[1] ++;
            }
            if (predictedTag.equals("Organizations:")){
                predicted[1] ++;
                if (goldTag.equals("ORG")) {
                    correct[1] ++;
                }
            }
            if (goldTag.equals("LOC")){
                labeled[2] ++;
            }
            if (predictedTag.equals("Places:") || predicted.equals("Culture:")){
                predicted[2] ++;
                if (goldTag.equals("LOC")) {
                    correct[2] ++;
                }
            }
        }
        double totalCorrect = 0;
        double totalLabeled = 0;
        for (int i = 0; i < 3; i++){
            totalCorrect += (double)correct[i];
            totalLabeled += (double)labeled[i];
            double p = (double)correct[i] / (double)predicted[i];
            double r = (double)correct[i] / (double)labeled[i];
            double f = p * r * 2 / (p + r);
            System.out.println(p + ", " + r + ", " + f);
        }
        System.out.println("Accuracy: " + totalCorrect / totalLabeled);
    }

    public static void produceTitles(){
        MentionReader mentionReader = MentionReader.getMentionReader("data/FIGER/train.data.gz");
        EntityProtos.Mention m;
        int count = 0;
        while ((m = mentionReader.readMention()) != null) {
            count ++;
        }
        System.out.println(count);
    }

    public static void testSupervisedFiger(){

        Set<String> uniqueTypes = new HashSet<>();
        try {
            FileReader fileReader = new FileReader("data/FIGER/types.map");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineGroup = line.split("\t");
                uniqueTypes.add(lineGroup[1]);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


        FIGERContainer container = FIGERContainer.readFromFile("data/FIGER/FIGERContainer_unique.cache");
        int trainInstanceSize = container._instances.size() / 5;

        FeatureExtractor featureExtractor = new FeatureExtractor();

        FIGERTypingClassifier classifier = new FIGERTypingClassifier();
        int count = 0;
        for (FIGERInstance instance : container._instances){
            count ++;
            if (count > trainInstanceSize){
                break;
            }
            for (String type : instance._goldTypes){
                FIGERPair curPair = new FIGERPair(type, instance._types, "", featureExtractor);
                classifier.learn(curPair);
            }
        }
        classifier.doneWithRound();
        classifier.doneLearning();
        System.out.println("Training Done on " + count + " instances");

        int poscorrect = 0;
        int postotal = 0;
        int negtotal = 0;
        int negcorrect = 0;
        int secCount = 0;
        int pospredicted = 0;
        for (FIGERInstance instance : container._instances){
            secCount ++;
            if (secCount < trainInstanceSize + 1){
                continue;
            }
            FIGERPair curPair = new FIGERPair("", instance._types, "", featureExtractor);
            String predicted = classifier.discreteValue(curPair);
            System.out.println(predicted);
            postotal ++;
            if (instance._goldTypes.contains(predicted)){
                    poscorrect++;
            }
        }
        System.out.println("Precision: " + (double)poscorrect / (double)pospredicted);
        System.out.println("Recall: " + (double)poscorrect / (double)postotal);

    }

    public static void printer() throws Exception{
        FIGERContainer container = FIGERContainer.readFromFile("data/FIGER/FIGERContainer_unique.cache");
        System.out.println(container._instances.size());
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("data/FIGER/features2types_unique.txt", true));
        for (FIGERInstance instance : container._instances){
            bufferedWriter.write(L2S(instance._types) + "\t");
            bufferedWriter.write(L2S(instance._goldTypes) + "\n");
        }
        bufferedWriter.close();

    }

    public static String L2S(List<String> input){
        String ret = "";
        for (String s : input){
            ret += s + "|||";
        }
        if (ret.length() > 0){
            ret = ret.substring(0, ret.length() - 3);
        }
        return ret;
    }

    public static void main(String[] args) {
        try {
            //FeatureExtractor featureExtractor = new FeatureExtractor();
            //System.out.println(featureExtractor.getNNOnlyCategories("LeBron_James"));
            //testMajorVote();
            //produceTitles();
            testSupervisedFiger();
            //printer();
            //WikiHandler.getRedirectUrl("James_Lake");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
