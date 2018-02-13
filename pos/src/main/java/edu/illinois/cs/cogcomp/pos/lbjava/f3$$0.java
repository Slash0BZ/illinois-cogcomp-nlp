// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// discrete f3$$0(Token w) <- f3$$0$0 && labelOneBefore

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.Feature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;


public class f3$$0 extends Classifier
{
  private static final f3$$0$0 left = new f3$$0$0();
  private static final labelOneBefore right = new labelOneBefore();

  public f3$$0()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "f3$$0";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete"; }

  public Feature featureValue(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'f3$$0(Token)' defined on line 156 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Feature __result;
    __result = left.featureValue(__example).conjunction(right.featureValue(__example), this);
    return __result;
  }

  public FeatureVector classify(Object __example)
  {
    return new FeatureVector(featureValue(__example));
  }

  public String discreteValue(Object __example)
  {
    return featureValue(__example).getStringValue();
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'f3$$0(Token)' defined on line 156 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "f3$$0".hashCode(); }
  public boolean equals(Object o) { return o instanceof f3$$0; }
}

