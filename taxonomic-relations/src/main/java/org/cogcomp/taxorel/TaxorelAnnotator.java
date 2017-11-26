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
        super(ViewNames.MENTION, new String[]{}, lazilyInitialize);
    }
    public void initialize(ResourceManager rm){
        featureExtractor = new FeatureExtractor();
    }
    public void addView(TextAnnotation ta) throws AnnotatorException {
        if (!isInitialized()) {
            doInitialize();
        }
        View tokenView = ta.getView(ViewNames.TOKENS);
        if (tokenView.getConstituents().size() != 3 || !tokenView.getConstituents().get(1).toString().equals(",")){
            throw new AnnotatorException("Incorrect Input Format");
        }
        String arg1 = tokenView.getConstituents().get(0).toString();
        String arg2 = tokenView.getConstituents().get(2).toString();
        int result = Integer.parseInt(featureExtractor.settleEntity(arg1, arg2, new ArrayList<>(), new ArrayList<>()));
        String label = labels[result];
        Constituent arg1New = tokenView.getConstituents().get(0).cloneForNewView(ViewNames.TAXOREL);
        Constituent arg2New = tokenView.getConstituents().get(2).cloneForNewView(ViewNames.TAXOREL);
        View relationView = new SpanLabelView(ViewNames.TAXOREL, ta);
        relationView.addConstituent(arg1New);
        relationView.addConstituent(arg2New);
        Relation output = new Relation(label, arg1New, arg2New, 1.0f);
        relationView.addRelation(output);
        ta.addView(ViewNames.TAXOREL, relationView);
    }

}
