package org.cogcomp.md;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CorefNameMain {

    public static List<TextAnnotation> readFile(String file_name) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file_name));
        String line;
        JSONParser parser = new JSONParser();
        List<TextAnnotation> ret = new ArrayList<>();
        POSAnnotator posAnnotator = new POSAnnotator();
        while ((line = br.readLine()) != null) {
            List<String[]> tokens = new ArrayList<>();
            JSONObject object = (JSONObject)parser.parse(line);
            JSONArray sentences = (JSONArray)object.get("sentences");
            for (Object sentence : sentences) {
                List<String> curSentence = new ArrayList<>();
                for (Object token : ((JSONArray)sentence)){
                    curSentence.add((String)token);
                }
                String[] curSentenceArr = new String[curSentence.size()];
                curSentenceArr = curSentence.toArray(curSentenceArr);
                tokens.add(curSentenceArr);
            }
            TextAnnotation ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);
            List<Pair<Integer, Integer>> heads = new ArrayList<>();
            JSONArray names = (JSONArray)object.get("people");
            for (Object name : names) {
                JSONArray pair = (JSONArray)name;
                Long start = (Long)pair.get(0);
                Long end = (Long)pair.get(1);
                Pair<Integer, Integer> curHead = new Pair<>(start.intValue(), end.intValue() + 1);
                heads.add(curHead);
            }

            JSONArray clusters = (JSONArray)object.get("clusters");
            View mentionView = new SpanLabelView("MENTIONS", ta);
            for (Object cluster : clusters) {
                JSONArray entries = (JSONArray)cluster;
                for (Object entry : entries) {
                    JSONArray pair = (JSONArray)entry;
                    int start = ((Long)pair.get(0)).intValue();
                    int end = ((Long)pair.get(1)).intValue() + 1;
                    for (Pair<Integer, Integer> head : heads) {
                        if (head.getFirst() >= start && head.getSecond() <= end){
                            Constituent mention = new Constituent("PER", 1.0f, "MENTIONS", ta, start, end);
                            mention.addAttribute("EntityHeadStartSpan", head.getFirst().toString());
                            mention.addAttribute("EntityHeadEndSpan", head.getSecond().toString());
                            mentionView.addConstituent(mention);
                        }
                        break;
                    }
                }
            }
            ta.addView("MENTIONS", mentionView);
            ta.addView(posAnnotator);
            ret.add(ta);
        }
        for (TextAnnotation ta : ret) {
            View mentionView = ta.getView("MENTIONS");
            for (Constituent mention : mentionView) {
//                System.out.println("Full: " + mention + ", HEAD: " + MentionAnnotator.getPredictedHeadConstituent(mention));
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        try {
            CorefNameMain.readFile("data/CorefName/train.gold.full.jsonlines");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
