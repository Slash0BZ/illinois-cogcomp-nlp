/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.question_typer;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.question_typer.lbjava.QuestionFineTyper;

/**
 * Created by daniel on 1/24/18.
 */
public class QuestionTypeAnnotator extends Annotator {
    QuestionFineTyper fine = null;
    QuestionFineTyper coarse = new QuestionFineTyper();

    public QuestionTypeAnnotator() {
        super(ViewNames.QUESTION_TYPE, new String[]{ViewNames.LEMMA, ViewNames.POS, ViewNames.NER_ONTONOTES, ViewNames.NER_CONLL}, true);
    }

    @Override
    public void initialize(ResourceManager rm) {
        fine = new QuestionFineTyper();
        coarse = new QuestionFineTyper();
    }

    @Override
    protected void addView(TextAnnotation ta) throws AnnotatorException {
        SpanLabelView view = new SpanLabelView(ViewNames.QUESTION_TYPE, ViewNames.QUESTION_TYPE, ta, 1.0);
        assert ta.getAvailableViews().contains(ViewNames.LEMMA) && ta.getAvailableViews().contains(ViewNames.NER_CONLL) &&
                ta.getAvailableViews().contains(ViewNames.NER_ONTONOTES): "the annotator does not have the required views ";
        String fineLabel = fine.discreteValue(ta);
        Double fineLabelScore = fine.scores(ta).getScore(fineLabel).score;
        String coarseLabel = coarse.discreteValue(ta);
        Double coarseLabelScore = coarse.scores(ta).getScore(coarseLabel).score;
        Constituent cFine = new Constituent(fineLabel, fineLabelScore, ViewNames.QUESTION_TYPE,
                ta, 0, ta.getTokens().length);
        Constituent cCoarse = new Constituent(coarseLabel, coarseLabelScore, ViewNames.QUESTION_TYPE, ta, 0, ta.getTokens().length);
        view.addConstituent(cCoarse);
        view.addConstituent(cFine);
        ta.addView(ViewNames.QUESTION_TYPE, view);
    }
}
