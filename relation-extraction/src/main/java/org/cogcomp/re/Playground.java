package org.cogcomp.re;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerAnnotator;
import edu.illinois.cs.cogcomp.chunker.main.ChunkerConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.Gazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.pipeline.common.Stanford331Configurator;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordDepHandler;
import edu.illinois.cs.cogcomp.pipeline.handlers.StanfordTrueCaseHandler;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.BIOFeatureExtractor;
import org.cogcomp.md.ExtentTester;
import org.cogcomp.md.LbjGen.extent_classifier;
import org.cogcomp.re.LbjGen.relation_classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by xuany on 4/25/2018.
 */
public class Playground {
    public static Map<String, String> readType(String path) throws Exception{
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        Map<String, String> ret = new HashMap<>();
        while ((line = bufferedReader.readLine()) != null) {
            String[] lineGroup = line.split("\t");
            if (lineGroup.length < 2){
                continue;
            }
            ret.put(lineGroup[0], lineGroup[1]);
        }
        return ret;
    }

    public static List<TextAnnotation> readFile(String path) throws Exception{

        StanfordTrueCaseHandler stanfordTrueCaseHandler = new StanfordTrueCaseHandler();
        stanfordTrueCaseHandler.initialize(null);

        List<TextAnnotation> ret = new ArrayList<>();
        //BufferedWriter bw = new BufferedWriter(new FileWriter("data/CS546/re_output_train.txt", true));
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        Map<String, String> types = readType("data/CS546/sent_ent_type.txt");

        Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
        File modelDir = ds.getDirectory("org.cogcomp.re", "ACE_GOLD_BI", 1.0, false);
        String modelFile = modelDir.getPath() + File.separator + "ACE_GOLD_BI" + File.separator + "ACE_GOLD_BI.lc";
        String lexFile = modelDir.getPath() + File.separator + "ACE_GOLD_BI" + File.separator + "ACE_GOLD_BI.lex";
        relation_classifier relationClassifier = new relation_classifier();
        relationClassifier.readModel(modelFile);
        relationClassifier.readLexicon(lexFile);
        ACERelationConstrainedClassifier constrainedClassifier = new ACERelationConstrainedClassifier(relationClassifier);

        Gazetteers gazetteers = null;
        WordNetManager wordNet = null;

        ChunkerAnnotator chunker  = new ChunkerAnnotator(true);
        chunker.initialize(new ChunkerConfigurator().getDefaultConfig());
        Properties stanfordProps = new Properties();
        stanfordProps.put("annotators", "pos, parse");
        stanfordProps.put("parse.originalDependencies", true);
        stanfordProps.put("parse.maxlen", Stanford331Configurator.STFRD_MAX_SENTENCE_LENGTH);
        stanfordProps.put("parse.maxtime", Stanford331Configurator.STFRD_TIME_PER_SENTENCE);
        POSTaggerAnnotator pos_Annotator = new POSTaggerAnnotator("pos", stanfordProps);
        ParserAnnotator parseAnnotator = new ParserAnnotator("parse", stanfordProps);
        StanfordDepHandler stanfordDepHandler = new StanfordDepHandler(pos_Annotator, parseAnnotator);

        File extentFile = ds.getDirectory("org.cogcomp.mention", "ACE_EXTENT", 1.0, false);
        String fileName_EXTENT = extentFile.getPath() + File.separator + "ACE_EXTENT" + File.separator + "EXTENT_ACE";
        extent_classifier classifier_extent = new extent_classifier(fileName_EXTENT + ".lc", fileName_EXTENT + ".lex");
        Map<String, Integer> labeled = new HashMap<>();
        Map<String, Integer> predicted_orig = new HashMap<>();
        Map<String, Integer> correct_orig = new HashMap<>();
        Map<String, Integer> predicted_maj = new HashMap<>();
        Map<String, Integer> correct_maj = new HashMap<>();
        try {
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.6, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
            gazetteers = GazetteersFactory.get();
            Vector<String> bcs = new Vector<>();
            bcs.add("brown-clusters" + File.separator + "brown-english-wikitext.case-intact.txt-c1000-freq10-v3.txt");
            bcs.add("brown-clusters" + File.separator + "brownBllipClusters");
            bcs.add("brown-clusters" + File.separator + "brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt");
            Vector<Integer> bcst = new Vector<>();
            bcst.add(5);
            bcst.add(5);
            bcst.add(5);
            Vector<Boolean> bcsl = new Vector<>();
            bcsl.add(false);
            bcsl.add(false);
            bcsl.add(false);
            BrownClusters.init(bcs, bcst, bcsl, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BrownClusters brownClusters = BrownClusters.get();
        POSAnnotator posAnnotator = new POSAnnotator();
        relation_classifier classifier = new relation_classifier();
        classifier.setLexiconLocation("data/CS546/relation_classifier_train.lex");
        classifier.setModelLocation("data/CS546/relation_classifier_train.lc");
        while ((line = bufferedReader.readLine()) != null) {
            String[] lineGroup = line.split("\t");
            String originalSent = lineGroup[0];

            int source = Integer.parseInt(lineGroup[1]);
            int source_end = Integer.parseInt(lineGroup[2]) + 1;
            int target = Integer.parseInt(lineGroup[3]);
            int target_end = Integer.parseInt(lineGroup[4]) + 1;
            String relation_type = lineGroup[5];

            String[] tokens = originalSent.split(" ");
            List<String[]> tokenizedSentence = new ArrayList<>();
            tokenizedSentence.add(tokens);
            TextAnnotation originalTA = BasicTextAnnotationBuilder.createTextAnnotationFromTokens("","",tokenizedSentence);
            originalTA.addView(posAnnotator);
            stanfordDepHandler.addView(originalTA);
            chunker.addView(originalTA);
            Constituent source_mention = new Constituent("", "MENTION", originalTA, source, source_end);
            Constituent target_mention = new Constituent("", "MENTION", originalTA, target, target_end);


            String source_type = types.get(originalSent + source_mention.getTokenizedSurfaceForm());
            String target_type = types.get(originalSent + target_mention.getTokenizedSurfaceForm());
            if (source_type == null || target_type == null){
                continue;
            }

            source_mention.addAttribute("EntityType", source_type);
            target_mention.addAttribute("EntityType", target_type);


            SpanLabelView mentionView = new SpanLabelView("MENTION", originalTA);

            Constituent source_mention_full = ExtentTester.getFullMention(classifier_extent, source_mention, gazetteers, brownClusters, wordNet);
            Constituent target_mention_full = ExtentTester.getFullMention(classifier_extent, target_mention, gazetteers, brownClusters, wordNet);
            source_mention_full.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(source_mention));
            target_mention_full.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(target_mention));

            mentionView.addConstituent(source_mention_full);
            mentionView.addConstituent(target_mention_full);
            originalTA.addView("MENTION", mentionView);

            View annotatedTokenView = new SpanLabelView("RE_ANNOTATED", originalTA);
            for (Constituent co : originalTA.getView(ViewNames.TOKENS).getConstituents()) {
                Constituent c = co.cloneForNewView("RE_ANNOTATED");
                for (String s : co.getAttributeKeys()) {
                    c.addAttribute(s, co.getAttribute(s));
                }
                c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, c));
                c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, c));
                annotatedTokenView.addConstituent(c);
            }
            originalTA.addView("RE_ANNOTATED", annotatedTokenView);
            Relation r = new Relation(relation_type, source_mention_full, target_mention_full, 1.0);
            r.addAttribute("RelationType", relation_type);
            r.addAttribute("RelationSubtype", relation_type);
            classifier.learn(r);
/*            String classified_type = ACERelationTester.getCoarseType(constrainedClassifier.discreteValue(r));
            incrementMap(labeled, relation_type);
            incrementMap(predicted_orig, classified_type);
            if (classified_type.equals(relation_type)){
                incrementMap(correct_orig, relation_type);
            }
            Map<String, Integer> vote = new HashMap<>();
            incrementMap(vote, classified_type);
            for (int i = 6; i < lineGroup.length; i++){
                String curSent = lineGroup[i];
                int cur_source = Integer.parseInt(lineGroup[i + 1]);
                int cur_source_end = Integer.parseInt(lineGroup[i + 2]) + 1;
                int cur_target = Integer.parseInt(lineGroup[i + 3]);
                int cur_target_end = Integer.parseInt(lineGroup[i + 4]) + 1;
                i = i + 4;
                String[] curTokens = curSent.split(" ");
                List<String[]> curTokenizedSentence = new ArrayList<>();
                curTokenizedSentence.add(curTokens);
                TextAnnotation curTA = BasicTextAnnotationBuilder.createTextAnnotationFromTokens("","",curTokenizedSentence);
                curTA.addView(posAnnotator);
                stanfordDepHandler.addView(curTA);
                chunker.addView(curTA);
                Constituent cur_source_mention = new Constituent("", "MENTION", curTA, cur_source, cur_source_end);
                Constituent cur_target_mention = new Constituent("", "MENTION", curTA, cur_target, cur_target_end);
                cur_source_mention.addAttribute("EntityType", source_type);
                cur_target_mention.addAttribute("EntityType", target_type);

                SpanLabelView curMentionView = new SpanLabelView("MENTION", curTA);

                Constituent cur_source_mention_full = ExtentTester.getFullMention(classifier_extent, cur_source_mention, gazetteers, brownClusters, wordNet);
                Constituent cur_target_mention_full = ExtentTester.getFullMention(classifier_extent, cur_target_mention, gazetteers, brownClusters, wordNet);
                cur_source_mention_full.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(cur_source_mention));
                cur_target_mention_full.addAttribute("GAZ", ((FlatGazetteers) gazetteers).annotatePhrase(cur_target_mention));

                curMentionView.addConstituent(cur_source_mention_full);
                curMentionView.addConstituent(cur_target_mention_full);
                curTA.addView("MENTION", curMentionView);

                View curAnnotatedTokenView = new SpanLabelView("RE_ANNOTATED", curTA);
                for (Constituent co : curTA.getView(ViewNames.TOKENS).getConstituents()) {
                    Constituent c = co.cloneForNewView("RE_ANNOTATED");
                    for (String s : co.getAttributeKeys()) {
                        c.addAttribute(s, co.getAttribute(s));
                    }
                    c.addAttribute("WORDNETTAG", BIOFeatureExtractor.getWordNetTags(wordNet, c));
                    c.addAttribute("WORDNETHYM", BIOFeatureExtractor.getWordNetHyms(wordNet, c));
                    curAnnotatedTokenView.addConstituent(c);
                }
                curTA.addView("RE_ANNOTATED", curAnnotatedTokenView);
                Relation curR = new Relation("", cur_source_mention_full, cur_target_mention_full, 1.0);
                String curClassifiedType = ACERelationTester.getCoarseType(constrainedClassifier.discreteValue(curR));
                incrementMap(vote, curClassifiedType);
                double score = relation_type.equals(curClassifiedType) ? 1.0 : 0.0;
                bw.append(originalSent + "\t" + curSent + "\t" + source_mention.getTokenizedSurfaceForm() + "\t" + target_mention.getTokenizedSurfaceForm() + "\t" + relation_type + "\t" + classified_type + "\t" + curClassifiedType + '\t' + score + '\n');
            }
            int max_count = 0;
            String max_label = "";
            for (String s : vote.keySet()){
                if (vote.get(s) > max_count){
                    max_count = vote.get(s);
                    max_label = s;
                }
            }
            incrementMap(predicted_maj, max_label);
            if (max_label.equals(relation_type)){
                incrementMap(correct_maj, max_label);
            }*/

        }
        classifier.doneWithRound();
        classifier.doneLearning();
        classifier.saveModel();
        classifier.saveLexicon();
        //printPerformance(labeled, predicted_orig, correct_orig);
        //printPerformance(labeled, predicted_maj, correct_maj);

        fileReader.close();
        return null;
    }

    public static void printPerformance(Map<String, Integer> labeled, Map<String, Integer> predicted, Map<String, Integer> correct){
        for (String type : labeled.keySet()){
            double labeledCount = (double) labeled.get(type);
            double predictedCount = predicted.containsKey(type) ? (double) predicted.get(type) : 0.0;
            double correctCount = correct.containsKey(type) ? (double) correct.get(type) : 0.0;
            double p = predictedCount > 0.0 ? correctCount / predictedCount : 0.0;
            double r = labeledCount > 0.0 ? correctCount / labeledCount : 0.0;
            double f = computeF1(p, r);

            System.out.println(type + "\t" + p + "\t" + r + "\t" + f);
        }
    }

    public static double computeF1(double p, double r){
        if (p + r != 0) {
            return 2 * p * r / (p + r);
        }
        else {
            return 0.0;
        }
    }

    public static void incrementMap(Map<String, Integer> map, String key){
        if (map.containsKey(key)){
            map.put(key, map.get(key) + 1);
        }
        else {
            map.put(key, 1);
        }
    }

    public static void ReTrain(){

    }


    public static void main(String[] args){
        try {
            readFile("data/CS546/train.out");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
