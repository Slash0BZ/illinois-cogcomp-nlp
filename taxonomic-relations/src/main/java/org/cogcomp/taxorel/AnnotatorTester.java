package org.cogcomp.taxorel;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Relation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;

/**
 * Created by xuany on 11/25/2017.
 */
public class AnnotatorTester {

    public static void printRelation (Relation r){
        Constituent source = r.getSource();
        Constituent target = r.getTarget();
        System.out.println(r.getRelationName());
        System.out.println(source.toString() + " || " + target.toString());
    }

    public static void main(String[] args){
        TaxorelAnnotator taxorelAnnotator = new TaxorelAnnotator();
        String text = "Obama, President";
        String corpus = "taxorel";
        String textId = "001";

        // Create a TextAnnotation From Text
        TextAnnotationBuilder stab =
                new TokenizerTextAnnotationBuilder(new StatefulTokenizer());
        TextAnnotation ta = stab.createTextAnnotation(corpus, textId, text);
        try {
            taxorelAnnotator.addView(ta);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Relation r = ta.getView("TAXOREL").getRelations().get(0);
        printRelation(r);
    }

}
