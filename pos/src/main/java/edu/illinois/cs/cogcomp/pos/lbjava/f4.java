// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// discrete% f4(Token w) <- labelOneAfter && labelOrBaseline

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;


public class f4 extends Classifier
{
  private static final labelOneAfter left = new labelOneAfter();
  private static final labelOrBaseline right = new labelOrBaseline();

  public f4()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "f4";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'f4(Token)' defined on line 159 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    FeatureVector __result;
    __result = new FeatureVector();
    Feature lf = left.featureValue(__example);
    Feature rf = right.featureValue(__example);
    __result.addFeature(lf.conjunction(rf, this));

    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'f4(Token)' defined on line 159 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "f4".hashCode(); }
  public boolean equals(Object o) { return o instanceof f4; }
}

