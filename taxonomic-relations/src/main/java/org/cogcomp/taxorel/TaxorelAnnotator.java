/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.taxorel;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.util.ArrayList;

/**
 * Created by xuany on 11/25/2017.
 */
public class TaxorelAnnotator extends Annotator {
    public static final String[] labels = new String[]{"NOT_RELATED", "1>2 Ancestor", "2>1 Descendant", "Sibling"};
    private FeatureExtractor featureExtractor = null;
    public TaxorelAnnotator(){
        this(true);
    }
    public TaxorelAnnotator(boolean lazilyInitialize){
        super(ViewNames.TAXOREL, new String[]{}, lazilyInitialize);
    }
    public void initialize(ResourceManager rm){
        featureExtractor = new FeatureExtractor();
    }
    public void addView(TextAnnotation ta) throws AnnotatorException {
        if (!isInitialized()) {
            doInitialize();
        }
        View tokenView = ta.getView(ViewNames.TOKENS);
        View relationView = new SpanLabelView(ViewNames.TAXOREL, ta);
        for (int s = 0; s < ta.getNumberOfSentences(); s++) {
            int splitIdx = -1;
            Sentence cur = ta.getSentence(s);
            for (int i = cur.getStartSpan(); i < cur.getEndSpan(); i++) {
                if (tokenView.getConstituents().get(i).toString().equals(",")) {
                    splitIdx = i;
                }
            }
            if (splitIdx == -1) {
                throw new AnnotatorException("No comma splitter found.");
            }
            Constituent arg1 = new Constituent("", ViewNames.TAXOREL, ta, cur.getStartSpan(), splitIdx);
            Constituent arg2 = new Constituent("", ViewNames.TAXOREL, ta, splitIdx + 1, cur.getEndSpan() - 1);
            int result = Integer.parseInt(featureExtractor.settleEntity(arg1.toString(), arg2.toString(), new ArrayList<>(), new ArrayList<>()));
            String label = labels[result];
            relationView.addConstituent(arg1);
            relationView.addConstituent(arg2);
            Relation output = new Relation(label, arg1, arg2, 1.0f);
            relationView.addRelation(output);
        }
        ta.addView(ViewNames.TAXOREL, relationView);
    }

}
