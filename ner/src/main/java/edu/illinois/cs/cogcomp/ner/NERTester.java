package edu.illinois.cs.cogcomp.ner;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Sentence;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.lbjava.parse.LinkedVector;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.ExpressiveFeaturesAnnotator;
import edu.illinois.cs.cogcomp.ner.InferenceMethods.Decoder;
import edu.illinois.cs.cogcomp.ner.LbjTagger.Data;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NERDocument;
import edu.illinois.cs.cogcomp.ner.LbjTagger.NEWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuany on 4/26/2018.
 */
public class NERTester {
    public static void testCoNLLonOntonotes() throws Exception {
        NERAnnotator nerAnnotator = new NERAnnotator(ViewNames.NER_CONLL);
        nerAnnotator.doInitialize();
        ColumnFormatTAReader columnFormatReader = new ColumnFormatTAReader("data/ontonotes/MUC/");
        Map<String, Integer> labeled = new HashMap<>();
        Map<String, Integer> predicted = new HashMap<>();
        Map<String, Integer> correct = new HashMap<>();
        int count = 0;
        for (TextAnnotation ta : columnFormatReader) {
            if (count > 4000){
                break;
            }
            ArrayList<LinkedVector> sentences = new ArrayList<>();
            String[] tokens = ta.getTokens();
            int[] tokenindices = new int[tokens.length];
            int tokenIndex = 0;
            int neWordIndex = 0;
            for (int i = 0; i < ta.getNumberOfSentences(); i++) {
                Sentence sentence = ta.getSentence(i);
                String[] wtoks = sentence.getTokens();
                LinkedVector words = new LinkedVector();
                for (String w : wtoks) {
                    if (w.length() > 0) {
                        NEWord.addTokenToSentence(words, w, "unlabeled");
                        tokenindices[neWordIndex] = tokenIndex;
                        neWordIndex++;
                        tokenIndex++;
                    }
                }
                if (words.size() > 0)
                    sentences.add(words);
            }
            // Do the annotation.
            Data data = new Data(new NERDocument(sentences, "input"));
            try {
                ExpressiveFeaturesAnnotator.annotate(data);
                Decoder.annotateDataBIO(data, nerAnnotator.t1, nerAnnotator.t2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ArrayList<LinkedVector> nerSentences = data.documents.get(0).sentences;
            int tokenoffset = 0;
            Map<Integer, String> predictions = new HashMap<>();
            for (LinkedVector vector : nerSentences) {
                for (int j = 0; j < vector.size(); j++, tokenoffset++) {
                    NEWord neWord = (NEWord) (vector.get(j));
                    String prediction = neWord.neTypeLevel2;
                    String targetType = neWord.neTargetType;
                    predictions.put(tokenoffset, targetType);
                }
            }
            for (Constituent mention : ta.getView("MENTIONS")){
                count ++;
                String goldType = mention.getLabel();
                if (!(goldType.equals("PERSON") || goldType.equals("ORG") || goldType.equals("LOC") || goldType.equals("PER"))){
                    continue;
                }
                if (goldType.equals("PERSON")){
                    goldType = "PER";
                }
                Map<String, Integer> freq = new HashMap<>();
                for (int i = mention.getStartSpan(); i < mention.getEndSpan(); i++){
                    incrementMap(freq, predictions.get(i).split("-")[1]);
                }
                int maxCount = 0;
                String predictedType = "";
                for (String s : freq.keySet()){
                    if (freq.get(s) > maxCount){
                        maxCount = freq.get(s);
                        predictedType = s;
                    }
                }
                if (predictedType.equals(goldType)){
                    incrementMap(correct, predictedType);
                }
                incrementMap(labeled, goldType);
                incrementMap(predicted, predictedType);
            }
        }
        printPerformance(labeled, predicted, correct);
    }

    public static void incrementMap(Map<String, Integer> map, String key){
        if (map.containsKey(key)){
            map.put(key, map.get(key) + 1);
        }
        else {
            map.put(key, 1);
        }
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

    public static void main(String[] args){
        try {
            testCoNLLonOntonotes();
        }
        catch (Exception e){

        }
    }
}
