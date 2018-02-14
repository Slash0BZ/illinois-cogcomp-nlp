// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5091CE43C030144F7568A44598C4228B2E4830DF18A407BAA7863DD4AE06CEAC6784524CFB3ED838A4B72B67776EDE87F0AC73E820DD3E5D9D1DCA4F0E309B5FE2C1DFACA9E3B77BF14603A5778C15518FEB2997CEB14D7DDA2B3A095E21AC4082989DF155CC8708ED9F037121DA578C45C2F13AE155D872C2799427753CC0A5B4801A2F8B676F5AC39F456968EC142E7E603F28262CC85E9C1D792B38FB0CA82628D8FF8471913AED28356A34B4CBC61B8932AC8E88C0E5A2F6CCC918199C8B9D2E4C7AB829D73C7985B3A65D99C76369194F99FFDF93E17AF418B7459C35A623D5836CE801C44EC02AE43BD4FB5E47A480E7C391F43DCF911CD041B7FB013C179A17C100000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;


public class BrownClusterPathsConj extends Classifier
{
  public BrownClusterPathsConj()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "BrownClusterPathsConj";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BrownClusterPathsConj(Token)' defined on line 51 of POSAdditional.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

    BrownClusters bc = BrownClusters.get();
    int i;
    Token w = word, last = word;
    for (i = 0; i <= 1 && last != null; ++i)
    {
      last = (Token) last.next;
    }
    for (i = 0; i > -1 && w.previous != null; --i)
    {
      w = (Token) w.previous;
    }
    String fet = "";
    for (; w != last; w = (Token) w.next)
    {
      String[] paths = bc.getPrefixes(w.form);
      for (int j = 0; j < paths.length; j++)
      {
        fet += paths[j];
      }
      i++;
    }
    __id = "" + (fet);
    __value = "true";
    __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'BrownClusterPathsConj(Token)' defined on line 51 of POSAdditional.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BrownClusterPathsConj".hashCode(); }
  public boolean equals(Object o) { return o instanceof BrownClusterPathsConj; }
}

