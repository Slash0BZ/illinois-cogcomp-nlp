/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.taxorel;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
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
        String text = "university of oulu, university. movie, so i married an axe murderer.";
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
        for (Relation r : ta.getView(ViewNames.TAXOREL).getRelations()){
            printRelation(r);
        }
    }

}
