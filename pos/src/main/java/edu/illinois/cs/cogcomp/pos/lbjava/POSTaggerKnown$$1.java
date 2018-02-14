/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.pos.POSConfigurator;

import java.util.LinkedList;

public class POSTaggerKnown$$1 extends Classifier {
    private static ResourceManager rm = new POSConfigurator().getDefaultConfig();
    private static String baselineModelFile = rm.getString("baselineModelPath");
    private static String baselineLexFile = rm.getString("baselineLexPath");
    private static final BaselineTarget __baselineTarget = new BaselineTarget(baselineModelFile,
            baselineLexFile);
    private static final WordForm __wordForm = new WordForm();
    private static final labelTwoBefore __labelTwoBefore = new labelTwoBefore();
    private static final labelOneBefore __labelOneBefore = new labelOneBefore();
    private static final labelOneAfter __labelOneAfter = new labelOneAfter();
    private static final labelTwoAfter __labelTwoAfter = new labelTwoAfter();
    private static final L2bL1b __L2bL1b = new L2bL1b();
    private static final L1bL1a __L1bL1a = new L1bL1a();
    private static final L1aL2a __L1aL2a = new L1aL2a();
    private static final f1 __f1 = new f1();
    private static final f2 __f2 = new f2();
    private static final f3 __f3 = new f3();
    private static final f4 __f4 = new f4();
    private static final f5 __f5 = new f5();
    private static final WordTypeInformation __wordTypeInformation = new WordTypeInformation();
    private static final BrownClusterPaths __bcc = new BrownClusterPaths();
    private static final BrownClusterPathsConj __bccj = new BrownClusterPathsConj();

    public POSTaggerKnown$$1() {
        containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
        name = "POSTaggerKnown$$1";
    }

    public String getInputType() {
        return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token";
    }

    public String getOutputType() {
        return "discrete%";
    }

    public FeatureVector classify(Object __example) {
        FeatureVector __result;
        __result = new FeatureVector();
        __result.addFeature(__wordForm.featureValue(__example));
        __result.addFeature(__baselineTarget.featureValue(__example));
        __result.addFeature(__labelTwoBefore.featureValue(__example));
        __result.addFeature(__labelOneBefore.featureValue(__example));
        __result.addFeature(__labelOneAfter.featureValue(__example));
        __result.addFeature(__labelTwoAfter.featureValue(__example));
        __result.addFeature(__L2bL1b.featureValue(__example));
        __result.addFeature(__L1bL1a.featureValue(__example));
        __result.addFeature(__L1aL2a.featureValue(__example));
/*       __result.addFeatures(__wordTypeInformation.classify(__example));*/
/*        __result.addFeatures(__bcc.classify(__example));
        __result.addFeatures(__bccj.classify(__example));*/
        return __result;
    }

    public int hashCode() {
        return "POSTaggerKnown$$1".hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof POSTaggerKnown$$1;
    }

    public LinkedList getCompositeChildren() {
        LinkedList result = new LinkedList();
        result.add(__wordForm);
        result.add(__baselineTarget);
        result.add(__labelTwoBefore);
        result.add(__labelOneBefore);
        result.add(__labelOneAfter);
        result.add(__labelTwoAfter);
        result.add(__L2bL1b);
        result.add(__L1bL1a);
        result.add(__L1aL2a);
/*        result.add(__wordTypeInformation);*/
/*        result.add(__bcc);
        result.add(__bccj);*/

        return result;
    }
}
