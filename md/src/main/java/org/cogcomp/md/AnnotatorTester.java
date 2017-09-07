/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ACEReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREDocumentReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.ereReader.EREMentionRelationReader;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import weka.core.converters.ArffLoader;

import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The testing class for MentionAnnotator
 * Validating if the annotator is working as expected
 */
public class AnnotatorTester {
    /**
     * By default, this function uses the ERE model trained with Type on ERE corpus, should have a fairly high performance.
     */
    public static void test_basic_annotator(){
        EREMentionRelationReader ereMentionRelationReader = null;
        ACEReader aceReader = null;
        POSAnnotator posAnnotator = new POSAnnotator();
        int total_labeled = 0;
        int total_predicted = 0;
        int total_correct = 0;
        int total_type_correct = 0;
        int total_extent_correct = 0;
        List<Constituent> ACE_Missed = new ArrayList<>();
        List<Constituent> ERE_Missed_unique = new ArrayList<>();
        try {
            aceReader = new ACEReader("data/partition_with_dev/dev", false);
            ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);
            /*
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_NONTYPE");
            for (XmlTextAnnotation xta : ereMentionRelationReader) {
                TextAnnotation ta = xta.getTextAnnotation();
                ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
                for (Constituent gc : ta.getView(ViewNames.MENTION_ACE).getConstituents()){
                    boolean found = false;
                    Constituent gch = ACEReader.getEntityHeadForConstituent(gc, ta, "B");
                    if (gch == null){
                        continue;
                    }
                    for (Constituent pc : ta.getView(ViewNames.MENTION).getConstituents()){
                        if (Integer.parseInt(pc.getAttribute("EntityHeadStartSpan")) == gch.getStartSpan() &&
                                Integer.parseInt(pc.getAttribute("EntityHeadEndSpan")) == gch.getEndSpan()){
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        ACE_Missed.add(gc);
                    }
                }
            }
            */
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ACE_NONTYPE");
            aceReader.reset();
            for (XmlTextAnnotation xta : ereMentionRelationReader) {
                TextAnnotation ta = xta.getTextAnnotation();
                ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
                total_predicted += ta.getView(ViewNames.MENTION).getNumberOfConstituents();
                boolean first = true;
                for (Constituent pc : ta.getView(ViewNames.MENTION).getConstituents()){
                    boolean found = false;
                    if (pc.getAttribute("EntityType").equals("VEH") || pc.getAttribute("EntityType").equals("WEA")){
                        continue;
                    }
                    total_labeled ++;
                    for (Constituent gc : ta.getView(ViewNames.MENTION_ERE).getConstituents()){
                        //gc.addAttribute("EntityType", gc.getLabel());
                        Constituent gch = ACEReader.getEntityHeadForConstituent(gc, ta, "B");
                        if (gch == null){
                            continue;
                        }
                        if (Integer.parseInt(pc.getAttribute("EntityHeadStartSpan")) == gch.getStartSpan() &&
                                Integer.parseInt(pc.getAttribute("EntityHeadEndSpan")) == gch.getEndSpan()){
                            total_correct ++;
                            if (pc.getAttribute("EntityType").equals(gc.getAttribute("EntityType"))){
                                total_type_correct ++;
                            }
                            if (pc.getStartSpan() == gc.getStartSpan() && pc.getEndSpan() == gc.getEndSpan()){
                                total_extent_correct ++;
                            }
                            found = true;
                            break;
                        }
                    }
                    first = false;
                    if (!found){
                        System.out.println(pc.getTextAnnotation().getSentenceFromToken(pc.getStartSpan()));
                        System.out.println(pc.toString() + " " + pc.getAttribute("EntityType"));
                        System.out.println(pc.getStartCharOffset());
                        System.out.println();
                    }
                }
            }
            /*
            for (Constituent c : ERE_Missed_unique){
                System.out.println(c.getTextAnnotation().getSentenceFromToken(c.getStartSpan()));
                System.out.println(c.toString());
                System.out.println(ACEReader.getEntityHeadForConstituent(c, c.getTextAnnotation(), "A").toString());
                System.out.println();
            }
            */

        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Labeled: " + total_labeled);
        System.out.println("Predicted: " + total_predicted);
        System.out.println("Correct: " + total_correct);
        System.out.println("Type Correct: " + total_type_correct);
        System.out.println("Extent Correct: " + total_extent_correct);
    }

    public static void printAcePronouns(){
        ACEReader aceReader = null;
        EREMentionRelationReader ereMentionRelationReader = null;
        try {
            aceReader = new ACEReader("data/all", false);
            ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        int hasRef = 0;
        int noRef = 0;
        for (XmlTextAnnotation xta : ereMentionRelationReader){
            TextAnnotation ta = xta.getTextAnnotation();
            View mentionView = ta.getView(ViewNames.MENTION_ERE);
            View corefView = ta.getView(ViewNames.COREF_ERE);
            for (Constituent c : mentionView.getConstituents()){
                if (c.getAttribute("EntityMentionType").equals("PRO")){
                    if (corefView.getConstituentsCovering(c).size() == 0){
                        System.out.println("++++++++++++++++No Reference+++++++++++++");
                        System.out.println(c.toString() + " " + c.getAttribute("EntityType"));
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
                        System.out.println();
                        noRef ++;
                        continue;
                    }
                    String proEntityId = corefView.getConstituentsCovering(c).get(0).getAttribute("EntityID");
                    Sentence curSentence = c.getTextAnnotation().getSentence(c.getSentenceId());
                    int refCount = 0;
                    for (Constituent ic : corefView.getConstituentsCoveringSpan(curSentence.getStartSpan(), curSentence.getEndSpan())){
                        if (ic.getAttribute("EntityID").equals(proEntityId)){
                            refCount ++;
                        }
                    }
                    if (refCount == 0){
                        System.out.println("ERROR: REF COUNT CANNOT BE 0");
                    }
                    if (refCount == 1){
                        System.out.println("++++++++++++++++No Reference+++++++++++++");
                        System.out.println(curSentence.toString());
                        System.out.println(c.toString() + " " + c.getAttribute("EntityType"));
                        System.out.println("+++++++++++++++++++++++++++++++++++++++++");
                        System.out.println();
                        noRef ++;
                    }
                    if (refCount > 1){
                        System.out.println("----------------Has Reference------------");
                        System.out.println(curSentence.toString());
                        System.out.println(c.toString() + " " + c.getAttribute("EntityType"));
                        System.out.println("-----------------------------------------");
                        System.out.println();
                        hasRef ++;
                    }
                }
            }
        }
        System.out.println("Has reference: " + hasRef);
        System.out.println("No reference: " + noRef);
    }

    public static void testEREonACE(){
        ACEReader aceReader = null;
        POSAnnotator posAnnotator = new POSAnnotator();
        int total_predicted = 0;
        int total_labeled = 0;
        int total_correct = 0;
        try {
            aceReader = new ACEReader("data/partition_with_dev/dev", false);
            MentionAnnotator mentionAnnotator = new MentionAnnotator("ERE_NONTYPE");
            MentionAnnotator ACEAnnotator = new MentionAnnotator("ACE_NONTYPE");
            aceReader.reset();
            for (TextAnnotation ta : aceReader) {
                //ta.addView(posAnnotator);
                mentionAnnotator.addView(ta);
                View predictedMentionView = ta.getView(ViewNames.MENTION);
                View goldMentionView = ta.getView(ViewNames.MENTION_ACE);
                boolean first = true;
                List<Constituent> errors = new ArrayList<>();
                List<Constituent> errorsOnACE = new ArrayList<>();
                for (Constituent gc : goldMentionView){
                    if (gc.getAttribute("EntityType").equals("VEH") || gc.getAttribute("EntityType").equals("WEA") || gc.getAttribute("EntityMentionType").equals("PRO")){
                        continue;
                    }
                    total_labeled ++;
                    Constituent gcHead = ACEReader.getEntityHeadForConstituent(gc, ta, "A");
                    boolean foundMatch = false;
                    Constituent overlap = null;
                    for (Constituent pc : predictedMentionView){
                        if (pc.getAttribute("EntityType").equals("VEH") || pc.getAttribute("EntityType").equals("WEA") || pc.getAttribute("EntityMentionType").equals("PRO")){
                            continue;
                        }
                        if (first){
                            total_predicted ++;
                        }
                        int gcStart = gcHead.getStartSpan();
                        int gcEnd = gcHead.getEndSpan();
                        int pcStart = Integer.parseInt(pc.getAttribute("EntityHeadStartSpan"));
                        int pcEnd = Integer.parseInt(pc.getAttribute("EntityHeadEndSpan"));
                        if (gcStart == pcStart && gcEnd == pcEnd){
                            total_correct ++;
                            foundMatch = true;
                        }
                        if ((gcStart < pcEnd && gcStart >= pcStart) || (pcStart < gcEnd && pcStart >= gcStart)){
                            overlap = pc;
                        }
                        if (gcEnd == pcEnd && pcStart == gcStart - 1 && ta.getToken(pcStart).toLowerCase().equals("the")){
                            total_correct ++;
                        }

                    }
                    first = false;
                    if (!foundMatch){
                        errors.add(gc);
                    }
                }
                ACEAnnotator.addView(ta);
                predictedMentionView = ta.getView(ViewNames.MENTION);
                for (Constituent gc : goldMentionView) {
                    Constituent gcHead = ACEReader.getEntityHeadForConstituent(gc, ta, "A");
                    if (gc.getAttribute("EntityType").equals("VEH") || gc.getAttribute("EntityType").equals("WEA") || gc.getAttribute("EntityMentionType").equals("PRO")) {
                        continue;
                    }
                    boolean foundMatch = false;
                    for (Constituent pc : predictedMentionView) {
                        if (pc.getAttribute("EntityType").equals("VEH") || pc.getAttribute("EntityType").equals("WEA") || pc.getAttribute("EntityMentionType").equals("PRO")) {
                            continue;
                        }
                        if (first) {
                            total_predicted++;
                        }
                        int gcStart = gcHead.getStartSpan();
                        int gcEnd = gcHead.getEndSpan();
                        int pcStart = Integer.parseInt(pc.getAttribute("EntityHeadStartSpan"));
                        int pcEnd = Integer.parseInt(pc.getAttribute("EntityHeadEndSpan"));
                        if (gcStart == pcStart && gcEnd == pcEnd) {
                            total_correct++;
                            foundMatch = true;
                        }
                        if (gcEnd == pcEnd && pcStart == gcStart - 1 && ta.getToken(pcStart).toLowerCase().equals("the")) {
                            total_correct++;
                        }
                    }
                    if (!foundMatch) {
                        errorsOnACE.add(gc);
                    }
                }
                List<Constituent> toRemove = new ArrayList<>();
                for (Constituent c : errors){
                    for (Constituent ac : errorsOnACE){
                        if (c.getStartSpan() == ac.getStartSpan() && c.getEndSpan() == ac.getEndSpan()){
                            toRemove.add(c);
                        }
                    }
                }
                System.out.println(errors.size());
                System.out.println(errorsOnACE.size());
                errors.removeAll(toRemove);
                System.out.println(errors.size());
                for (Constituent c : errors) {
                    Constituent gcHead = ACEReader.getEntityHeadForConstituent(c, ta, "A");
                    System.out.println(c.toString());
                    System.out.println(gcHead.toString());
                    System.out.println();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Labeled: " + total_labeled);
        System.out.println("Predicted: " + total_predicted);
        System.out.println("Correct: " + total_correct);
    }
    public static void printer(){
        EREMentionRelationReader ereMentionRelationReader = null;
        ACEReader aceReader = null;
        Map<String, String> aceMap = new HashMap<>();
        Map<String, String> ereMap = new HashMap<>();
        int sentenceCountACE = 0;
        int mentionCountACE =0;
        int sentenceCountERE = 0;
        int mentionCountERE = 0;
        Map<Integer, Integer> aceSentenceMap = new HashMap<>();
        Map<Integer, Integer> ereSentenceMap = new HashMap<>();
        try {
            ereMentionRelationReader = new EREMentionRelationReader(EREDocumentReader.EreCorpus.ENR3, "data/ere/data", false);
            for (XmlTextAnnotation xmlTextAnnotation : ereMentionRelationReader){
                TextAnnotation ta = xmlTextAnnotation.getTextAnnotation();
                for (Constituent c : ta.getView(ViewNames.MENTION_ERE)){
                    mentionCountERE ++;
                    String lc = c.toString().toLowerCase();
                    if (c.getAttribute("EntityType") == null){
                        c.addAttribute("EntityType", c.getLabel());
                    }
                    ereMap.put(lc, c.getAttribute("EntityMentionType"));
                }
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence cur = ta.getSentence(i);
                    List<Constituent> within = ta.getView(ViewNames.MENTION_ERE).getConstituentsCoveringSpan(cur.getStartSpan(), cur.getEndSpan());
                    int key = within.size();
                    if (ereSentenceMap.containsKey(key)){
                        ereSentenceMap.put(key, ereSentenceMap.get(key) + 1);
                    }
                    else {
                        ereSentenceMap.put(key, 1);
                    }
                }
                sentenceCountERE += ta.getNumberOfSentences();
            }
            aceReader = new ACEReader("data/all", false);
            for (TextAnnotation ta : aceReader){
                for (Constituent c : ta.getView(ViewNames.MENTION_ACE)){
                    if (!c.getAttribute("EntityType").equals("VEH") && !c.getAttribute("EntityType").equals("WEA")){
                        mentionCountACE ++;
                    }
                    String lc = c.toString().toLowerCase();
                    if (ereMap.get(lc) != null){
                        if (!c.getAttribute("EntityMentionType").equals(ereMap.get(lc))){
                            Sentence cur = ta.getSentenceFromToken(c.getStartSpan());
                            System.out.println("---------" + c.getAttribute("EntityMentionType") + "/" + ereMap.get(lc) + "-------");
                            System.out.println(cur);
                            System.out.println(c.toString());
                        }
                    }
                }
                for (int i = 0; i < ta.getNumberOfSentences(); i++){
                    Sentence cur = ta.getSentence(i);
                    List<Constituent> within = ta.getView(ViewNames.MENTION_ACE).getConstituentsCoveringSpan(cur.getStartSpan(), cur.getEndSpan());
                    int key = within.size();
                    if (aceSentenceMap.containsKey(key)){
                        aceSentenceMap.put(key, aceSentenceMap.get(key) + 1);
                    }
                    else {
                        aceSentenceMap.put(key, 1);
                    }
                }
                sentenceCountACE += ta.getNumberOfSentences();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("ACE Average mention: " + (double)mentionCountACE / (double)sentenceCountACE);
        System.out.println("ERE Average mention: " + (double)mentionCountERE / (double)sentenceCountERE);
        System.out.println("------------PRINTING ACE MAP---------------");
        for (int key : aceSentenceMap.keySet()){
            System.out.println(key + ": " + aceSentenceMap.get(key));
        }
        System.out.println("------------PRINTING ERE MAP---------------");
        for (int key : ereSentenceMap.keySet()){
            System.out.println(key + ": " + ereSentenceMap.get(key));
        }
    }
    public static void main(String[] args){
        testEREonACE();
    }
}
