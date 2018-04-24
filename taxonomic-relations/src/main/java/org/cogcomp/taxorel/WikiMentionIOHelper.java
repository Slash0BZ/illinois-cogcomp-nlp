package org.cogcomp.taxorel;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by xuany on 2/23/2018.
 */
public class WikiMentionIOHelper {

    public static void serializeDataOut(WikiMentionContainer input, String outputFile){
        String fileName= outputFile;
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(input);
            oos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static WikiMentionContainer serializeDataIn(String inputFile){
        String fileName= inputFile;
        WikiMentionContainer ret = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            ret = (WikiMentionContainer) ois.readObject();
            ois.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static void oldMain(){
        WikiMentionContainer wikiMentionContainer = new WikiMentionContainer();
        FeatureExtractor featureExtractor = new FeatureExtractor();
        try {
            File conllGold = new File("data/conll/eng.list");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(conllGold));
            Map<String, String> goldType = new HashMap<>();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                String[] lineGroup = line.split(" ");
                String tag = lineGroup[0];
                if (tag.length() != 3){
                    continue;
                }
                String mention = line.substring(4);
                goldType.put(mention, tag);
            }

            File conllAnnotated = new File("data/conll/aida-yago2-dataset/AIDA-YAGO2-annotations.tsv");
            bufferedReader = new BufferedReader(new FileReader(conllAnnotated));
            Set<String> seenSet = new HashSet<>();
            int count = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineGroup = line.split("\t");
                if (lineGroup.length < 3) {
                    continue;
                }
                String mention = lineGroup[1].replace("_", " ");
                String wikiLink = lineGroup[2];
                String goldTag = "";
                if (goldType.containsKey(mention)) {
                    goldTag = goldType.get(mention);
                }
                if (goldTag.equals("")) {
                    continue;
                }

                String[] tempGroup = wikiLink.split("/");
                String wikiTitle = tempGroup[tempGroup.length - 1];
                if (seenSet.contains(wikiTitle)) {
                    continue;
                }
                seenSet.add(wikiTitle);
                count ++;
                System.out.println(count);
                WikiMention wikiMention = new WikiMention(wikiTitle, goldTag, featureExtractor);
                wikiMentionContainer.addMention(wikiMention);
            }
            serializeDataOut(wikiMentionContainer, "data/conll/cachedWikiMentions");
        }
        catch (Exception e){
            e.printStackTrace();
        }
/*
        WikiMentionContainer wikiMentionContainer1 = serializeDataIn("data/conll/cachedWikiMentions");
        for (WikiMention mention : wikiMentionContainer1.mentions){
            System.out.println(mention._title);
            System.out.println(mention._type);
            System.out.println(mention.categories);
        }*/

    }
    public static void main(String[] args){

    }

}
