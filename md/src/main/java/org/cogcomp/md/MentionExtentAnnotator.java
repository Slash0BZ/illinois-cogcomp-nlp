/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.core.resources.ResourceConfigurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.edison.utilities.WordNetManager;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.FlatGazetteers;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.GazetteersFactory;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import org.cogcomp.Datastore;
import org.cogcomp.md.LbjGen.bio_classifier_nam;
import org.cogcomp.md.LbjGen.bio_classifier_nom;
import org.cogcomp.md.LbjGen.bio_classifier_pro;
import org.cogcomp.md.LbjGen.extent_classifier;

import java.io.File;
import java.util.Vector;

/**
 * This class gives a given TextAnnotation a new View ViewNames.MENTION
 * The View contains Constituents that are annotated mentions of the given TextAnnotation
 * The annotator requires POS View to work.
 *
 * The Constituents in ViewNames.MENTION is the full mention includes extent
 * To get the head of a Constituent, use the Attribute "EntityHeadStartSpan" and "EntityHeadEndSpan"
 */
public class MentionExtentAnnotator extends Annotator{

    private extent_classifier classifier_extent;
    private FlatGazetteers gazetteers;
    private BrownClusters brownClusters;
    private WordNetManager wordNet;
    private Annotator mentionHeadAnnotator;

    private String _mode;

    public MentionExtentAnnotator(){
        this(true, "ONTONOTES");
    }

    public MentionExtentAnnotator(String mode) {
        this(true, mode);
    }

    public MentionExtentAnnotator(boolean lazilyInitialize, String mode){
        super(ViewNames.MENTION_EXTENT, new String[]{ViewNames.POS}, lazilyInitialize);
        _mode = mode;
    }

    public void initialize(ResourceManager rm){
        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            if (_mode.equals("ONTONOTES")) {
                File extentFile = ds.getDirectory("org.cogcomp.mention", "ACE_EXTENT", 1.0, false);
                String fileName_EXTENT = extentFile.getPath() + File.separator + "ACE_EXTENT" + File.separator + "EXTENT_ACE";
                classifier_extent = new extent_classifier(fileName_EXTENT + ".lc", fileName_EXTENT + ".lex");
                mentionHeadAnnotator = new NERAnnotator(ViewNames.NER_ONTONOTES);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            Datastore ds = new Datastore(new ResourceConfigurator().getDefaultConfig());
            File gazetteersResource = ds.getDirectory("org.cogcomp.gazetteers", "gazetteers", 1.3, false);
            GazetteersFactory.init(5, gazetteersResource.getPath() + File.separator + "gazetteers", true);
            gazetteers = (FlatGazetteers) GazetteersFactory.get();
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
            brownClusters = BrownClusters.get();
            WordNetManager.loadConfigAsClasspathResource(true);
            wordNet = WordNetManager.getInstance();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addView(TextAnnotation ta) throws AnnotatorException{
        if (!isInitialized()){
            doInitialize();
        }
        if (!ta.hasView(ViewNames.POS)){
            throw new AnnotatorException("Missing required view POS");
        }
        View mentionExtentView = new SpanLabelView(ViewNames.MENTION_EXTENT, MentionExtentAnnotator.class.getCanonicalName(), ta, 1.0f, true);

        View mentionHeadView = mentionHeadAnnotator.getView(ta);

        for (Constituent head : mentionHeadView) {
            Constituent fullMention = ExtentTester.getFullMention(classifier_extent, head, gazetteers, brownClusters, wordNet);
            mentionExtentView.addConstituent(fullMention);
        }
        ta.addView(ViewNames.MENTION_EXTENT, mentionExtentView);
    }
}
