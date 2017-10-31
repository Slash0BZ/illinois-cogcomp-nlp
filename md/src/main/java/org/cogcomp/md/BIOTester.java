/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Score;
import edu.illinois.cs.cogcomp.lbjava.classify.ScoreSet;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.Lexicon;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.cogcomp.md.LbjGen.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is for running testers and utilities.
 */

public class BIOTester {

    /**
     * Returns the corpus data paths.
     * @param mode "train/eval/all/dev"
     * @param corpus "ACE/ERE"
     * @param fold The fold index. Not used in mode "all/dev"
     */
    public static String getPath(String mode, String corpus, int fold){
        if (corpus.equals("ERE")) {
            if (mode.equals("train")) {
                return "data/ere/cv/train/" + fold;
            }
            else if (mode.equals("eval")) {
                return "data/ere/cv/eval/" + fold;
            }
            else if (mode.equals("all")){
                return "data/ere/data";
            }
            else {
                return "INVALID_PATH";
            }
        }
        else if (corpus.equals("ACE")){
            if (mode.equals("train")) {
                return "data/partition_with_dev/train/" + fold;
            }
            else if (mode.equals("eval")) {
                return "data/partition_with_dev/eval/" + fold;
            }
            else if (mode.equals("dev")){
                return "data/partition_with_dev/dev";
            }
            else if (mode.equals("all")){
                return "data/all";
            }
            else{
                return "INVALID_PATH";
            }
        }
        else {
            return "INVALID CORPUS";
        }
    }

    /**
     * Extracts the most common predicted type in a given BILOU sequence.
     */
    public static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }

    /**
     * Trainer for the head named entity classifier.
     * @param train_parser The parser containing all training examples
     * @param modelLoc The expected model file destination. Support null.
     */
    public static bio_classifier_nam train_nam_classifier(Parser train_parser, String modelLoc){
        bio_classifier_nam classifier = new bio_classifier_nam();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (int i = 0; i < 1; i++) {
            train_parser.reset();
            for (Object example = train_parser.next(); example != null; example = train_parser.next()) {
                classifier.learn(example);
            }
            classifier.doneWithRound();
        }
        classifier.doneLearning();
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_namc train_nam_classifierc(Parser train_parser, String modelLoc){
        bio_classifier_namc classifier = new bio_classifier_namc();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (int i = 0; i < 1; i++) {
            train_parser.reset();
            for (Object example = train_parser.next(); example != null; example = train_parser.next()) {
                classifier.learn(example);
            }
            classifier.doneWithRound();
        }
        classifier.doneLearning();
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_nam train_nam_classifier(Parser train_parser){
        return train_nam_classifier(train_parser, null);
    }

    /**
     * Trainer for the head nominal classifier.
     * @param train_parser The parser containing all training examples
     * @param modelLoc The expected model file destination. Support null.
     */
    public static bio_classifier_nom train_nom_classifier(Parser train_parser, String modelLoc){
        bio_classifier_nom classifier = new bio_classifier_nom();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (int i = 0; i < 1; i++) {
            train_parser.reset();
            for (Object example = train_parser.next(); example != null; example = train_parser.next()) {
                classifier.learn(example);
            }
            classifier.doneWithRound();
        }
        classifier.doneLearning();
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_nomc train_nom_classifierc(Parser train_parser, String modelLoc){
        bio_classifier_nomc classifier = new bio_classifier_nomc();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (int i = 0; i < 1; i++) {
            train_parser.reset();
            for (Object example = train_parser.next(); example != null; example = train_parser.next()) {
                classifier.learn(example);
            }
            classifier.doneWithRound();
        }
        classifier.doneLearning();
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_nom train_nom_classifier(Parser train_parser){
        return train_nom_classifier(train_parser, null);
    }

    /**
     * Trainer for the head pronoun classifier.
     * @param train_parser The parser containing all training examples
     * @param modelLoc The expected model file destination. Support null.
     */
    public static bio_classifier_pro train_pro_classifier(Parser train_parser, String modelLoc){
        bio_classifier_pro classifier = new bio_classifier_pro();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        train_parser.reset();
        classifier.doneWithRound();
        classifier.doneLearning();
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static bio_classifier_proc train_pro_classifierc(Parser train_parser, String modelLoc){
        bio_classifier_proc classifier = new bio_classifier_proc();
        train_parser.reset();
        BatchTrainer trainer = new BatchTrainer(classifier, train_parser);
        String modelFileName = "";
        if (modelLoc == null){
            String parser_id = ((BIOReader)train_parser).id;
            modelFileName = "tmp/bio_classifier_" + parser_id;
        }
        else{
            modelFileName = modelLoc;
        }
        classifier.setLexiconLocation(modelFileName + ".lex");
        Learner preExtractLearner = trainer.preExtract(modelFileName + ".ex", true, Lexicon.CountPolicy.none);
        preExtractLearner.saveLexicon();
        Lexicon lexicon = preExtractLearner.getLexicon();
        classifier.setLexicon(lexicon);
        int examples = 0;
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            examples ++;
        }
        train_parser.reset();
        classifier.initialize(examples, preExtractLearner.getLexicon().size());
        for (Object example = train_parser.next(); example != null; example = train_parser.next()){
            classifier.learn(example);
        }
        train_parser.reset();
        classifier.doneWithRound();
        classifier.doneLearning();
        if (modelLoc != null){
            classifier.setModelLocation(modelFileName + ".lc");
            classifier.saveModel();
        }
        return classifier;
    }

    public static String getCorpus (Constituent c){
        if (c.getTextAnnotation().getId().startsWith("bn") || c.getTextAnnotation().getId().startsWith("nw")){
            return "ACE";
        }
        if (c.getTextAnnotation().getId().startsWith("ENG")){
            //return "ERE";
        }
        return "ERE";
    }

    public static bio_classifier_pro train_pro_classifier(Parser train_parser) {
        return train_pro_classifier(train_parser, null);
    }

    /**
     *
     * @param t The target Consitutent
     * @param candidates The learner array containing 3 Learners.
     *                    candidates[0] : NAM
     *                    candidates[1] : NOM
     *                    candidates[2] : PRO
     * @return a pair of a String and a Integer.
     *          The String: The result of the joint inferencing
     *          The Integer: The index of the selected learner in candidates
     */
    public static Pair<Pair<String, List<Pair<String, Double>>>, Integer> joint_inference(Constituent t, Learner[] candidates){

        double highest_start_score = -10.0;
        List<Pair<String, Double>> score_list = new ArrayList<>();
        String[] preBIOLevel1 = new String[candidates.length];
        String[] preBIOLevel2 = new String[candidates.length];
        for (int i = 0; i < preBIOLevel1.length; i++){
            preBIOLevel2[i] = "O";
        }
        int chosen = -1;
        for (int i = 0; i < candidates.length; i++){
            String prediction = candidates[i].discreteValue(t);
            preBIOLevel1[i] = prediction;
            Double curScore = 0.0;
            ScoreSet scores = candidates[i].scores(t);
            Score[] scoresArray = scores.toArray();
            for (Score s : scoresArray){
                if (s.value.equals(prediction)){
                    curScore = s.score;
                }
            }
            score_list.add(new Pair<>(prediction, Math.floor(curScore * 100) / 100));
            if (prediction.startsWith("B") || prediction.startsWith("U")){
                if (curScore > highest_start_score) {
                    highest_start_score = curScore;
                    chosen = i;
                }
            }
        }
        if (highest_start_score < 0){
            chosen = -1;
        }
        if (getCorpus(t).equals("ACE")){
            double lowest = 10.0;
            boolean allO = true;
            for (int i = 0; i < 3; i++){
                if (score_list.get(i).getSecond() < lowest){
                    lowest = score_list.get(i).getSecond();
                }
                if (!score_list.get(i).getFirst().startsWith("O")){
                    allO = false;
                }
            }
            if (allO && lowest > 1.5){
                chosen = -1;
            }
        }
        if (getCorpus(t).equals("ERE")){
            double lowest = 10.0;
            boolean allO = true;
            for (int i = 2; i < 6; i++){
                if (score_list.get(i).getSecond() < lowest){
                    lowest = score_list.get(i).getSecond();
                }
                if (!score_list.get(i).getFirst().startsWith("O")){
                    allO = false;
                }
            }
            if (allO && lowest > 1.5){
                chosen = -1;
            }
        }
        /*
        if (getCorpus(t).equals("ACE") && chosen > 2){
            boolean allO = true;
            for (int i = 0; i < 3; i++){
                if (!score_list.get(i).getFirst().startsWith("O")){
                    allO = false;
                }
            }
            if (allO && score_list.get(chosen - 3).getFirst().startsWith("O") && score_list.get(chosen - 3).getSecond() > 1.0){
                chosen = -1;
            }
            else if (!score_list.get(chosen - 3).getFirst().startsWith("O")){
                chosen = chosen - 3;
            }
        }
        else if (getCorpus(t).equals("ERE") && chosen < 3){
            boolean allO = true;
            for (int i = 3; i < 6; i++){
                if (!score_list.get(i).getFirst().startsWith("O")){
                    allO = false;
                }
            }
            if (allO && score_list.get(chosen + 3).getFirst().startsWith("O") && score_list.get(chosen + 3).getSecond() > 1.0){
                chosen = -1;
            }
            else if (!score_list.get(chosen + 3).getFirst().startsWith("O")){
                chosen = chosen + 3;
            }
        }
        */
        if (chosen == -1){
            return new Pair<>(new Pair<>("O", score_list), -1);
        }
        else{
            return new Pair<>(new Pair<>(candidates[chosen].discreteValue(t), score_list), chosen);
        }
    }

    public static String inference(Constituent c, Classifier classifier){
        return classifier.discreteValue(c);
    }

    /**
     *
     * @param curToken The token of the start of a mention (either gold/predicted)
     * @param classifier The selected classifier from joint_inference
     * @param isGold Indicates if getting the gold mention or not
     * @return A constituent of the entire mention head. The size may be larger than 1.
     */
    public static Constituent getConstituent(Constituent curToken, Classifier classifier, boolean isGold) {
        View bioView = curToken.getTextAnnotation().getView("BIO");
        String goldType = "NA";
        if (isGold) {
            if (!curToken.getAttribute("BIO").startsWith("O")) {
                goldType = (curToken.getAttribute("BIO").split("-"))[1];
            }
        }
        List<String> predictedTypes = new ArrayList<>();
        if (!isGold) {
            predictedTypes.add((inference(curToken, classifier).split("-"))[1]);
        }
        int startIdx = curToken.getStartSpan();
        int endIdx = startIdx + 1;
        if (inference(curToken, classifier).startsWith("B") && endIdx < bioView.getEndSpan()) {
            String preBIOLevel2_dup = curToken.getAttribute("preBIOLevel1");
            String preBIOLevel1_dup = inference(curToken, classifier);
            Constituent pointerToken = null;
            while (endIdx < bioView.getEndSpan()) {
                pointerToken = bioView.getConstituentsCoveringToken(endIdx).get(0);
                pointerToken.addAttribute("preBIOLevel1", preBIOLevel1_dup);
                pointerToken.addAttribute("preBIOLevel2", preBIOLevel2_dup);
                if (isGold) {
                    String curGold = pointerToken.getAttribute("BIO");
                    if (!(curGold.startsWith("I") || curGold.startsWith("L"))) {
                        break;
                    }
                }
                else {
                    String curPrediction = inference(pointerToken, classifier);
                    if (!(curPrediction.startsWith("I") || curPrediction.startsWith("L"))) {
                        break;
                    }
                    predictedTypes.add(curPrediction.split("-")[1]);
                }
                preBIOLevel2_dup = preBIOLevel1_dup;
                preBIOLevel1_dup = inference(pointerToken, classifier);
                endIdx ++;
            }
        }

        Constituent wholeMention = new Constituent(curToken.getLabel(), 1.0f, "BIO_Mention", curToken.getTextAnnotation(), startIdx, endIdx);
        if (isGold){
            wholeMention.addAttribute("EntityType", goldType);
            wholeMention.addAttribute("EntityMentionType", curToken.getAttribute("EntityMentionType"));
        }
        else{
            wholeMention.addAttribute("EntityType", mostCommon(predictedTypes));
            String className = classifier.getClass().toString();
            String emt = className.substring(className.length() - 3).toUpperCase();
            wholeMention.addAttribute("EntityMentionType", emt);
        }
        return wholeMention;
    }

    /**
     * Cross Validation tester
     */
    public static void test_cv(){
        boolean isBIO = false;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        int violations = 0;

        for (int i = 0; i < 5; i++){

            Parser test_parser = new BIOCombinedReader(i, "ERE-EVAL", "ALL");
            bio_label output = new bio_label();
            System.out.println("Start training fold " + i);
            Parser train_parser_nam = new BIOCombinedReader(i, "ERE-TRAIN", "NAM");
            Parser train_parser_nom = new BIOCombinedReader(i, "ERE-TRAIN", "NOM");
            Parser train_parser_pro = new BIOCombinedReader(i, "ERE-TRAIN", "PRO");

            bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
            bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
            bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

            Learner[] candidates = new Learner[3];
            candidates[0] = classifier_nam;
            candidates[1] = classifier_nom;
            candidates[2] = classifier_pro;

            int labeled_mention = 0;
            int predicted_mention = 0;
            int correct_mention = 0;

            System.out.println("Start evaluating fold " + i);
            String preBIOLevel1 = "";
            String preBIOLevel2 = "";

            for (Object example = test_parser.next(); example != null; example = test_parser.next()){
                System.out.println(((Constituent)example).toString());
                ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

                Pair<Pair<String, List<Pair<String, Double>>>, Integer> cands = joint_inference((Constituent)example, candidates);

                String bioTag = cands.getFirst().getFirst();
                if (bioTag.equals("I") && !(preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }
                if (bioTag.equals("L") && !(preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }
                if (bioTag.equals("U") && (preBIOLevel1.equals("B") || preBIOLevel1.equals("I"))){
                    violations ++;
                }
                if (bioTag.equals("B") && preBIOLevel1.equals("I")){
                    violations ++;
                }
                if (bioTag.equals("O") && (preBIOLevel1.equals("I") || preBIOLevel1.equals("B"))){
                    violations ++;
                }

                preBIOLevel2 = preBIOLevel1;
                preBIOLevel1 = bioTag;

                boolean goldStart = false;
                boolean predictedStart = false;

                if (bioTag.startsWith("B") || bioTag.startsWith("U")){
                    predicted_mention ++;
                    predictedStart = true;
                }
                String correctTag = output.discreteValue(example);

                if (correctTag.startsWith("B") || correctTag.startsWith("U")){
                    labeled_mention ++;
                    goldStart = true;
                }
                boolean correctBoundary = false;
                if (goldStart && predictedStart) {
                    int candidateIdx = cands.getSecond();
                    Constituent goldMention = getConstituent((Constituent)example, candidates[candidateIdx], true);
                    Constituent predictMention = getConstituent((Constituent)example, candidates[candidateIdx], false);
                    if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                        correctBoundary = true;
                        correct_mention++;
                    }
                }
            }
            total_labeled_mention += labeled_mention;
            total_predicted_mention += predicted_mention;
            total_correct_mention += correct_mention;
        }

        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
        System.out.println("violations: " + violations);
    }

    /**
     * Test set tester
     */
    public static void test_ts(){
        List<Constituent> errosOnACE = new ArrayList<>();
        ACEReader aceReader = null;
        int inconCount = 0;
        try {
            aceReader = new ACEReader("data/partition_with_dev/dev", false);
            POSAnnotator posAnnotator = new POSAnnotator();
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_NONTYPE");
            for (TextAnnotation ta : aceReader) {
                ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
                View predictedMentionView = ta.getView(ViewNames.MENTION);
                View goldMentionView = ta.getView(ViewNames.MENTION_ACE);
                for (Constituent gc : goldMentionView) {
                    Constituent gcHead = ACEReader.getEntityHeadForConstituent(gc, ta, "A");
                    if (gc.getAttribute("EntityType").equals("VEH") || gc.getAttribute("EntityType").equals("WEA") || gc.getAttribute("EntityMentionType").equals("PRO")) {
                        continue;
                    }
                    if (!gc.getAttribute("EntityMentionType").equals("NOM")){
                        if (gc.getStartSpan() != gcHead.getStartSpan() || gc.getEndSpan() != gcHead.getEndSpan()) {
                            System.out.println("______NOTTHESAME___________");
                            if (!gc.toString().toLowerCase().contains("the")) {
                                inconCount++;
                            }
                            System.out.println(gc.toString());
                            System.out.println(gcHead.toString());
                            System.out.println();
                        }
                    }
                    boolean foundMatch = false;
                    for (Constituent pc : predictedMentionView) {
                        if (pc.getAttribute("EntityType").equals("VEH") || pc.getAttribute("EntityType").equals("WEA") || pc.getAttribute("EntityMentionType").equals("PRO")) {
                            continue;
                        }
                        int gcStart = gcHead.getStartSpan();
                        int gcEnd = gcHead.getEndSpan();
                        int pcStart = Integer.parseInt(pc.getAttribute("EntityHeadStartSpan"));
                        int pcEnd = Integer.parseInt(pc.getAttribute("EntityHeadEndSpan"));
                        if (gcStart == pcStart && gcEnd == pcEnd) {
                            foundMatch = true;
                        }
                    }
                    if (!foundMatch) {
                        errosOnACE.add(gcHead);
                    }
                }
            }
            System.out.println(inconCount);
        }
        catch (Exception e){

        }
        boolean isBIO = false;
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        int total_correct_nam = 0;
        int total_false_type_nam = 0;
        int total_correct_nom = 0;
        int total_false_type_nom = 0;
        int total_correct_pro = 0;
        int total_false_type_pro = 0;

        Parser test_parser = new BIOReader(getPath("dev", "ACE", 0), "ACE05-EVAL", "ALL", isBIO);

        Parser train_parser_nam = new BIOReader("data/ere/data", "ERE-TRAIN", "NAM", isBIO);
        Parser train_parser_nom = new BIOReader("data/ere/data", "ERE-TRAIN", "NOM", isBIO);
        Parser train_parser_pro = new BIOReader("data/ere/data", "ERE-TRAIN", "PRO", isBIO);
        /*
        Parser train_parser_nam = new BIOReader("data/all", "ACE05-TRAIN", "NAM", isBIO);
        Parser train_parser_nom = new BIOReader("data/all", "ACE05-TRAIN", "NOM", isBIO);
        Parser train_parser_pro = new BIOReader("data/all", "ACE05-TRAIN", "PRO", isBIO);
        */
        bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
        bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
        bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

        Learner[] candidates = new Learner[3];
        candidates[0] = classifier_nam;
        candidates[1] = classifier_nom;
        candidates[2] = classifier_pro;

        String preBIOLevel1 = "";
        String preBIOLevel2 = "";

        List<Constituent> errors = new ArrayList<>();

        for (Object example = test_parser.next(); example != null; example = test_parser.next()){

            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            Pair<Pair<String, List<Pair<String, Double>>>, Integer> cands = joint_inference((Constituent)example, candidates);

            String bioTag = cands.getFirst().getFirst();
            int learnerIdx = cands.getSecond();

            preBIOLevel2 = preBIOLevel1;
            preBIOLevel1 = bioTag;

            boolean goldStart = false;
            boolean predictedStart = false;

            if (bioTag.startsWith("B") || bioTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            String correctTag = ((Constituent)example).getAttribute("BIO");
            if (correctTag.startsWith("B") || correctTag.startsWith("U")){
                if (correctTag.contains("WEA") || correctTag.contains("VEH")){
                    //continue;
                }
                total_labeled_mention ++;
                goldStart = true;
            }
            boolean printError = false;
            if (goldStart && predictedStart) {
                Constituent goldMention = getConstituent((Constituent)example, candidates[learnerIdx], true);
                Constituent predictMention = getConstituent((Constituent)example, candidates[learnerIdx], false);
                boolean boundaryCorrect = false;
                boolean typeCorrect = false;
                if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                    boundaryCorrect = true;
                }
                if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                    typeCorrect = true;
                }
                if (boundaryCorrect){
                    total_correct_mention ++;
                    if (learnerIdx == 0){
                        total_correct_nam ++;
                    }
                    if (learnerIdx == 1){
                        total_correct_nom ++;
                    }
                    if (learnerIdx == 2){
                        total_correct_pro ++;
                    }
                }
                if (!boundaryCorrect){
                    printError = true;
                }
            }
            //if ((goldStart && !predictedStart) || printError){
            if (goldStart && predictedStart && !printError){
                Constituent predictedMention = getConstituent((Constituent)example, candidates[learnerIdx], false);
                Constituent goldMention = getConstituent((Constituent)example, candidates[0], true);
                boolean foundInACEErrors = false;
                for (Constituent ac : errosOnACE){
                    if (ac.getStartSpan() == goldMention.getStartSpan() && ac.getEndSpan() == goldMention.getEndSpan() && ac.getTextAnnotation().getId() == goldMention.getTextAnnotation().getId()){
                        foundInACEErrors = true;
                    }
                }
                //if (!foundInACEErrors) {
                if (!predictedMention.getAttribute("EntityMentionType").equals(goldMention.getAttribute("EntityMentionType"))){
                    errors.add(goldMention);
                    System.out.println("--------" + goldMention.getAttribute("EntityMentionType") + "/" + goldMention.getAttribute("EntityType") + "---------");
                    Sentence curSentence = goldMention.getTextAnnotation().getSentenceFromToken(goldMention.getStartSpan());
                    String preBIOLevel1Dup = "";
                    String preBIOLevel2Dup = "";
                    View BioView = ((Constituent)example).getTextAnnotation().getView("BIO");
                    int candidateIdx = 0;
                    for (int i = curSentence.getStartSpan(); i < curSentence.getEndSpan(); i++){
                        Constituent curBioCons = BioView.getConstituentsCoveringToken(i).get(0);
                        curBioCons.addAttribute("preBIOLevel1", preBIOLevel1Dup);
                        curBioCons.addAttribute("preBIOLevel2", preBIOLevel2Dup);
                        Pair<Pair<String, List<Pair<String, Double>>>, Integer> candsDup = joint_inference(curBioCons, candidates);
                        String prediction = null;
                        if (candsDup.getFirst().getFirst().startsWith("B") || candsDup.getFirst().getFirst().startsWith("U")){
                            prediction = candsDup.getFirst().getFirst().split("-")[0];
                            candidateIdx = candsDup.getSecond();
                        }
                        else{
                            prediction = inference(curBioCons, candidates[candidateIdx]);
                        }
                        preBIOLevel2Dup = preBIOLevel1Dup;
                        preBIOLevel1Dup = prediction;
                        System.out.print(curBioCons.toString() + "(" + prediction + ") ");
                    }
                    System.out.println();
                    System.out.println(goldMention.toString());
                    System.out.println("--------------------\n");
                }
            }
        }

        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        System.out.println("NAM: " + total_false_type_nam + "/" + total_correct_nam);
        System.out.println("NOM: " + total_false_type_nom + "/" + total_correct_nom);
        System.out.println("PRO: " + total_false_type_pro + "/" + total_correct_pro);
    }

    /**
     * ERE corpus tester
     */
    public static void test_ere(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;
        int total_correct_type_match = 0;

        Parser test_parser = new BIOReader(getPath("all", "ERE", 0), "ERE-EVAL", "ALL", false);
        Parser train_parser_nam = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NAM", false);
        Parser train_parser_nom = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NOM", false);
        Parser train_parser_pro = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "PRO", false);
        bio_classifier_nam classifier_nam = train_nam_classifier(train_parser_nam);
        bio_classifier_nom classifier_nom = train_nom_classifier(train_parser_nom);
        bio_classifier_pro classifier_pro = train_pro_classifier(train_parser_pro);

        String preBIOLevel1 = "";
        String preBIOLevel2 = "";

        Learner[] candidates = new Learner[3];
        candidates[0] = classifier_nam;
        candidates[1] = classifier_nom;
        candidates[2] = classifier_pro;

        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preBIOLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preBIOLevel2);

            Pair<Pair<String, List<Pair<String, Double>>>, Integer> prediction = joint_inference((Constituent)example, candidates);
            String goldTag = ((Constituent)example).getAttribute("BIO");
            String predictedTag = prediction.getFirst().getFirst();
            preBIOLevel2 = preBIOLevel1;
            preBIOLevel1 = predictedTag;
            boolean goldStart = false;
            if (goldTag.startsWith("B") || goldTag.startsWith("U")){
                total_labeled_mention ++;
                goldStart = true;
            }
            boolean predictedStart = false;
            if (predictedTag.startsWith("B") || predictedTag.startsWith("U")){
                total_predicted_mention ++;
                predictedStart = true;
            }
            boolean correct = false;
            boolean type_match = false;
            if (goldStart && predictedStart){
                Constituent goldMention = getConstituent((Constituent)example, candidates[prediction.getSecond()], true);
                Constituent predictedMention = getConstituent((Constituent)example, candidates[prediction.getSecond()], false);
                if (goldMention.getStartSpan() == predictedMention.getStartSpan() && goldMention.getEndSpan() == predictedMention.getEndSpan()){
                    correct = true;
                }
                if (goldMention.getAttribute("EntityType").equals(predictedMention.getAttribute("EntityType"))){
                    type_match = true;
                }
                if (correct){
                    total_correct_mention ++;
                    if (type_match){
                        total_correct_type_match ++;
                    }
                }
            }
        }
        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        System.out.println("Total Correct Type Match: " + total_correct_type_match);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
    }

    public static void test_tac(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        Parser train_parser = new BIOReader("data/tac/es/tac2016.train", "ColumnFormat-TRAIN", "ALL", false);
        Parser test_parser = new BIOReader("data/tac/es/tac2016.test", "ColumnFormat-EVAL", "ALL", false);
        bio_classifier_nom classifier = train_nom_classifier(train_parser);
        String preLevel1 = "";
        String preLevel2 = "";
        Map<String, Integer> predictedMap = new HashMap<>();
        Map<String, Integer> labeledMap = new HashMap<>();
        Map<String, Integer> correctMap = new HashMap<>();
        for (Object example = test_parser.next(); example != null; example = test_parser.next()){
            ((Constituent)example).addAttribute("preBIOLevel1", preLevel1);
            ((Constituent)example).addAttribute("preBIOLevel2", preLevel2);
            String predictedTag = inference((Constituent)example, classifier);
            String goldTag = ((Constituent)example).getAttribute("BIO");
            boolean predictedStart = false;
            boolean goldStart = false;
            if (predictedTag.startsWith("B") || predictedTag.startsWith("U")){
                total_predicted_mention ++;
                String key = predictedTag.split("-")[1];
                if (predictedMap.containsKey(key)){
                    predictedMap.put(key, predictedMap.get(key) + 1);
                }
                else{
                    predictedMap.put(key, 1);
                }
                predictedStart = true;
            }
            if (goldTag.startsWith("B") || goldTag.startsWith("U")){
                total_labeled_mention ++;
                String key = goldTag.split("-")[1];
                if (labeledMap.containsKey(key)){
                    labeledMap.put(key, labeledMap.get(key) + 1);
                }
                else{
                    labeledMap.put(key, 1);
                }
                goldStart = true;
            }
            if (predictedStart && goldStart){
                Constituent goldMention = getConstituent((Constituent)example, classifier, true);
                Constituent predictedMention = getConstituent((Constituent)example, classifier, false);
                if (goldMention.getStartSpan() == predictedMention.getStartSpan() && goldMention.getEndSpan() == predictedMention.getEndSpan()){
                    if (goldMention.getAttribute("EntityType").equals(predictedMention.getAttribute("EntityType"))) {
                        total_correct_mention++;
                        String key = goldMention.getAttribute("EntityType");
                        if (correctMap.containsKey(key)){
                            correctMap.put(key, correctMap.get(key) + 1);
                        }
                        else{
                            correctMap.put(key, 1);
                        }
                    }
                }
            }
            preLevel2 = preLevel1;
            preLevel1 = predictedTag;
        }
        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        for (String type : labeledMap.keySet()){
            p = (double)correctMap.get(type) / (double)predictedMap.get(type);
            r = (double)correctMap.get(type) / (double)labeledMap.get(type);
            f = 2 * p * r / (p + r);
            System.out.println(type + "\t" + p * 100.0 + "\t" + r * 100.0 + "\t" + f * 100.0 + "\t" + labeledMap.get(type));
        }
    }

    public static void test_tac_with_annotator(){
        //Please change this line to test on either nam or nom only
        String target_mention_type = "nom";
        //Please change this line to the correct data path
        ColumnFormatReader columnFormatReader = new ColumnFormatReader("data/tac/2016." + target_mention_type);
        POSAnnotator posAnnotator = new POSAnnotator();
        MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_NONTYPE");
        int labeled = 0;
        int predicted = 0;
        int correct = 0;
        try {
            for (TextAnnotation ta : columnFormatReader) {
                ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
                View goldView = ta.getView("MENTIONS");
                View predictedView = ta.getView(ViewNames.MENTION);
                labeled += goldView.getNumberOfConstituents();
                for (Constituent pc : predictedView){
                    if (pc.getAttribute("EntityMentionType").equals(target_mention_type.toUpperCase())){
                        predicted ++;
                    }
                }
                for (Constituent gc : goldView){
                    Constituent gcHead = ACEReader.getEntityHeadForConstituent(gc, gc.getTextAnnotation(), "NOT_RELEVANT");
                    for (Constituent pc : predictedView){
                        Constituent pcHead = MentionAnnotator.getPredictedHeadConstituent(pc);
                        if (gcHead.getStartSpan() == pcHead.getStartSpan() && gcHead.getEndSpan() == pcHead.getEndSpan()){
                            if (pc.getAttribute("EntityMentionType").equals(target_mention_type.toUpperCase())){
                                correct ++;
                            }
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        double p = (double)correct / (double)predicted;
        double r = (double)correct / (double)labeled;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
    }

    /**
     * Calculates the average mention head size by type.
     * Research purposes only
     */
    public static void calculateAvgMentionLength(){
        ACEReader aceReader = null;
        try{
            aceReader = new ACEReader(getPath("all", "ERE", 0), false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        double nam = 0.0;
        double nom = 0.0;
        double pro = 0.0;
        double namcount = 0.0;
        double nomcount = 0.0;
        double procount = 0.0;
        for (TextAnnotation ta : aceReader){
            for (Constituent c : ta.getView(ViewNames.MENTION_ACE)){
                Constituent ch = ACEReader.getEntityHeadForConstituent(c, ta, "A");
                if (ch.getAttribute("EntityMentionType").equals("NAM")){
                    nam += (double)(ch.getEndSpan() - ch.getStartSpan());
                    namcount += 1.0;
                }
                if (ch.getAttribute("EntityMentionType").equals("NOM")){
                    nom += (double)(ch.getEndSpan() - ch.getStartSpan());
                    nomcount += 1.0;
                }
                if (ch.getAttribute("EntityMentionType").equals("PRO")){
                    pro += (double)(ch.getEndSpan() - ch.getStartSpan());
                    procount += 1.0;
                }
            }
        }
        System.out.println("NAM LENGTH: " + nam / namcount);
        System.out.println("NOM LENGTH: " + nom / nomcount);
        System.out.println("PRO LENGTH: " + pro / procount);
    }

    /**
     * Test the model trained on hybrid ACE/ERE and evaluated on hybrid ACE/ERE
     * Produce results on separate types
     */
    public static void test_hybrid(){
        int total_labeled_mention = 0;
        int total_predicted_mention = 0;
        int total_correct_mention = 0;

        int total_ace_labeled_mention = 0;
        int total_ere_labeled_mention = 0;
        int total_ace_predicted_mention = 0;
        int total_ere_predicted_mention = 0;
        int total_ace_correct_mention = 0;
        int total_ere_correct_mention = 0;
        int total_ace_type_correct = 0;
        int total_ere_type_correct = 0;

        List<String> outputs = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Parser test_parser = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_EVAL_" + i);
            Parser train_parser_nam_ace = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ACE_NAM_" + i);
            Parser train_parser_nom_ace = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ACE_NOM_" + i);
            Parser train_parser_pro_ace = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ACE_PRO_" + i);
            Parser train_parser_nam_ere = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ERE_NAM_" + i);
            Parser train_parser_nom_ere = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ERE_NOM_" + i);
            Parser train_parser_pro_ere = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ERE_PRO_" + i);
            bio_classifier_nam classifier_nam_ace = train_nam_classifier(train_parser_nam_ace);
            bio_classifier_nom classifier_nom_ace = train_nom_classifier(train_parser_nom_ace);
            bio_classifier_pro classifier_pro_ace = train_pro_classifier(train_parser_pro_ace);
            bio_classifier_namc classifier_nam_ere = train_nam_classifierc(train_parser_nam_ere, null);
            bio_classifier_nomc classifier_nom_ere = train_nom_classifierc(train_parser_nom_ere, null);
            bio_classifier_proc classifier_pro_ere = train_pro_classifierc(train_parser_pro_ere, null);

            Learner[] candidates_joint = new Learner[6];
            candidates_joint[0] = classifier_nam_ace;
            candidates_joint[1] = classifier_nom_ace;
            candidates_joint[2] = classifier_pro_ace;
            candidates_joint[3] = classifier_nam_ere;
            candidates_joint[4] = classifier_nom_ere;
            candidates_joint[5] = classifier_pro_ere;

            String preBIOLevel1 = "";
            String preBIOLevel2 = "";

            for (Object example = test_parser.next(); example != null; example = test_parser.next()) {

                ((Constituent) example).addAttribute("preBIOLevel1", preBIOLevel1);
                ((Constituent) example).addAttribute("preBIOLevel2", preBIOLevel2);

                Pair<Pair<String, List<Pair<String, Double>>>, Integer> cands = joint_inference((Constituent) example, candidates_joint);

                String bioTag = cands.getFirst().getFirst();
                int learnerIdx = cands.getSecond();

                preBIOLevel2 = preBIOLevel1;
                preBIOLevel1 = bioTag;

                boolean goldStart = false;
                boolean predictedStart = false;

                if (bioTag.startsWith("B") || bioTag.startsWith("U")) {
                    total_predicted_mention++;
                    if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                            ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                        total_ace_predicted_mention ++;
                    }
                    else {
                        total_ere_predicted_mention ++;
                    }
                    predictedStart = true;
                }
                String correctTag = ((Constituent) example).getAttribute("BIO");

                if (correctTag.startsWith("B") || correctTag.startsWith("U")) {
                    total_labeled_mention++;
                    if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                            ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                        total_ace_labeled_mention ++;
                    }
                    else {
                        total_ere_labeled_mention ++;
                    }
                    goldStart = true;
                }

                if (goldStart && predictedStart) {
                    Constituent goldMention = getConstituent((Constituent) example, candidates_joint[learnerIdx], true);
                    Constituent predictMention = getConstituent((Constituent) example, candidates_joint[learnerIdx], false);
                    boolean boundaryCorrect = false;
                    boolean typeCorrect = false;
                    if (goldMention.getStartSpan() == predictMention.getStartSpan() && goldMention.getEndSpan() == predictMention.getEndSpan()) {
                        boundaryCorrect = true;
                    }
                    if (goldMention.getAttribute("EntityType").equals(predictMention.getAttribute("EntityType"))) {
                        typeCorrect = true;
                    }
                    if (boundaryCorrect) {
                        total_correct_mention++;
                        if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                                ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                            total_ace_correct_mention ++;
                        }
                        else {
                            total_ere_correct_mention ++;
                        }
                        if (typeCorrect){
                            if (((Constituent)example).getTextAnnotation().getId().startsWith("bn") ||
                                    ((Constituent)example).getTextAnnotation().getId().startsWith("nw")){
                                total_ace_type_correct ++;
                            }
                            else {
                                total_ere_type_correct ++;
                            }
                        }
                    }
                }

                if (predictedStart && !goldStart){
                    Constituent predictMention = getConstituent((Constituent) example, candidates_joint[learnerIdx], false);
                    TextAnnotation ta = predictMention.getTextAnnotation();
                    String output = "";
                    Sentence sentence = predictMention.getTextAnnotation().getSentenceFromToken(predictMention.getStartSpan());
                    String goldMentionViewName = ViewNames.MENTION_ERE;
                    if (ta.getId().startsWith("bn") || ta.getId().startsWith("nw")){
                        goldMentionViewName = ViewNames.MENTION_ACE;
                    }
                    List<Constituent> goldMentions = ta.getView(goldMentionViewName).getConstituentsCoveringSpan(sentence.getStartSpan(), sentence.getEndSpan());
                    List<Integer> goldStarts = new ArrayList<>();
                    List<Integer> goldEnds = new ArrayList<>();
                    for (Constituent c : goldMentions){
                        Constituent cHead = ACEReader.getEntityHeadForConstituent(c, ta, "");
                        if (cHead == null){
                            continue;
                        }
                        goldStarts.add(cHead.getStartSpan());
                        goldEnds.add(cHead.getEndSpan());
                    }
                    for (int t = sentence.getStartSpan(); t < sentence.getEndSpan(); t++){
                        if (goldEnds.contains(t)){
                            output = output.substring(0, output.length() - 1);
                            output += "} ";
                        }
                        if (t == predictMention.getEndSpan()){
                            output = output.substring(0, output.length() - 1);
                            output += "] ";
                        }
                        if (goldStarts.contains(t)){
                            output += "{";
                        }
                        if (t == predictMention.getStartSpan()){
                            output += "[";
                        }
                        output += ta.getToken(t) + " ";
                    }
                    output += "\t";
                    List<Pair<String, Double>> predictions = cands.getFirst().getSecond();
                    int chosen = cands.getSecond();
                    for (int k = 0; k < predictions.size(); k++){
                        Pair<String, Double> curp = predictions.get(k);
                        if (k == chosen){
                            output += "[" + curp.getFirst().charAt(0) + ":" + curp.getSecond() + "]\t";
                        }
                        else {
                            output += curp.getFirst().charAt(0) + ":" + curp.getSecond() + "\t";
                        }
                    }
                    if (predictMention.getTextAnnotation().getId().startsWith("bn") || predictMention.getTextAnnotation().getId().startsWith("nw")){
                        output += "ACE" + "\t";
                    }
                    else {
                        output += "ERE" + "\t";
                    }
                    outputs.add(output);
                }
            }
        }

        System.out.println("Total Labeled Mention: " + total_labeled_mention);
        System.out.println("Total Predicted Mention: " + total_predicted_mention);
        System.out.println("Total Correct Mention: " + total_correct_mention);
        double p = (double)total_correct_mention / (double)total_predicted_mention;
        double r = (double)total_correct_mention / (double)total_labeled_mention;
        double f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        System.out.println("Total Labeled Mention ACE: " + total_ace_labeled_mention);
        System.out.println("Total Predicted Mention ACE: " + total_ace_predicted_mention);
        System.out.println("Total Correct Mention ACE: " + total_ace_correct_mention);
        System.out.println("Total Type Correct ACE: " + total_ace_type_correct);
        p = (double)total_ace_correct_mention / (double)total_ace_predicted_mention;
        r = (double)total_ace_correct_mention / (double)total_ace_labeled_mention;
        f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);

        System.out.println("Total Labeled Mention ERE: " + total_ere_labeled_mention);
        System.out.println("Total Predicted Mention ERE: " + total_ere_predicted_mention);
        System.out.println("Total Correct Mention ERE: " + total_ere_correct_mention);
        System.out.println("Total Type Correct ERE: " + total_ere_type_correct);
        p = (double)total_ere_correct_mention / (double)total_ere_predicted_mention;
        r = (double)total_ere_correct_mention / (double)total_ere_labeled_mention;
        f = 2 * p * r / (p + r);
        System.out.println("Precision: " + p);
        System.out.println("Recall: " + r);
        System.out.println("F1: " + f);
        /*
        for (String output : outputs){
            System.out.println(output);
        }
        */

    }

    public static void test_hybrid_tac(){
        int i = 0;
        Parser test_parser = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_EVAL_" + i);
        Parser train_parser_nam_ace = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ACE_NAM_" + i);
        Parser train_parser_nom_ace = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ACE_NOM_" + i);
        Parser train_parser_pro_ace = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ACE_PRO_" + i);
        Parser train_parser_nam_ere = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ERE_NAM_" + i);
        Parser train_parser_nom_ere = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ERE_NOM_" + i);
        Parser train_parser_pro_ere = BIOCombinedReader.serializeIn("md/preprocess/reader/COMBINED_ERE_PRO_" + i);
        bio_classifier_nam classifier_nam_ace = train_nam_classifier(train_parser_nam_ace);
        bio_classifier_nom classifier_nom_ace = train_nom_classifier(train_parser_nom_ace);
        bio_classifier_pro classifier_pro_ace = train_pro_classifier(train_parser_pro_ace);
        bio_classifier_namc classifier_nam_ere = train_nam_classifierc(train_parser_nam_ere, null);
        bio_classifier_nomc classifier_nom_ere = train_nom_classifierc(train_parser_nom_ere, null);
        bio_classifier_proc classifier_pro_ere = train_pro_classifierc(train_parser_pro_ere, null);

        Learner[] candidates_joint = new Learner[6];
        candidates_joint[0] = classifier_nam_ace;
        candidates_joint[1] = classifier_nom_ace;
        candidates_joint[2] = classifier_pro_ace;
        candidates_joint[3] = classifier_nam_ere;
        candidates_joint[4] = classifier_nom_ere;
        candidates_joint[5] = classifier_pro_ere;


    }

    public static void TrainModel(String corpus){
        if (corpus.equals("ACE")) {
            Parser train_parser_nam = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NAM", false);
            Parser train_parser_nom = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "NOM", false);
            Parser train_parser_pro = new BIOReader(getPath("all", "ACE", 0), "ACE05-TRAIN", "PRO", false);
            train_nam_classifier(train_parser_nam, "models/ACE_NAM");
            train_nom_classifier(train_parser_nom, "models/ACE_NOM");
            train_pro_classifier(train_parser_pro, "models/ACE_PRO");
        }
        else if (corpus.equals("ERE")){
            Parser train_parser_nam = new BIOReader(getPath("all", "ERE", 0), "ACE05-TRAIN", "NAM", false);
            Parser train_parser_nom = new BIOReader(getPath("all", "ERE", 0), "ACE05-TRAIN", "NOM", false);
            Parser train_parser_pro = new BIOReader(getPath("all", "ERE", 0), "ACE05-TRAIN", "PRO", false);
            train_nam_classifier(train_parser_nam, "models/ERE_NAM");
            train_nom_classifier(train_parser_nom, "models/ERE_NOM");
            train_pro_classifier(train_parser_pro, "models/ERE_PRO");
        }
        else if (corpus.equals("TAC")){
            Parser train_parser_nom = new BIOReader("data/tac/en/tac2016.train", "ColumnFormat-TRAIN", "ALL", false);
            train_nom_classifier(train_parser_nom, "models/TAC_NOM");
        }
    }

    public static void TrainACEModel(){
        TrainModel("ACE");
    }

    public static void TrainEREModel(){
        TrainModel("ERE");
    }

    public static void generateReaders(){
        for (int i = 0; i < 5; i ++){
            BIOCombinedReader train_parser_nam_ace = new BIOCombinedReader(i, "ACE-TRAIN", "NAM");
            BIOCombinedReader train_parser_nom_ace = new BIOCombinedReader(i, "ACE-TRAIN", "NOM");
            BIOCombinedReader train_parser_pro_ace = new BIOCombinedReader(i, "ACE-TRAIN", "PRO");
            BIOCombinedReader train_parser_nam_ere = new BIOCombinedReader(i, "ERE-TRAIN", "NAM");
            BIOCombinedReader train_parser_nom_ere = new BIOCombinedReader(i, "ERE-TRAIN", "NOM");
            BIOCombinedReader train_parser_pro_ere = new BIOCombinedReader(i, "ERE-TRAIN", "PRO");
            BIOCombinedReader test = new BIOCombinedReader(i, "ALL-EVAL", "ALL");
            BIOCombinedReader.serializeOut(train_parser_nam_ace, "md/preprocess/reader/COMBINED_ACE_NAM_" + i);
            BIOCombinedReader.serializeOut(train_parser_nom_ace, "md/preprocess/reader/COMBINED_ACE_NOM_" + i);
            BIOCombinedReader.serializeOut(train_parser_pro_ace, "md/preprocess/reader/COMBINED_ACE_PRO_" + i);
            BIOCombinedReader.serializeOut(train_parser_nam_ere, "md/preprocess/reader/COMBINED_ERE_NAM_" + i);
            BIOCombinedReader.serializeOut(train_parser_nom_ere, "md/preprocess/reader/COMBINED_ERE_NOM_" + i);
            BIOCombinedReader.serializeOut(train_parser_pro_ere, "md/preprocess/reader/COMBINED_ERE_PRO_" + i);
            BIOCombinedReader.serializeOut(test, "md/preprocess/reader/COMBINED_EVAL_" + i);
        }
    }

    public static void main(String[] args){
        test_hybrid();
    }
}
