// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5091CE43C030144F7568A44598C4228B2E4830DF18A407BAA7863DD4AE4D2BB2BD125901FFE873E0A2DE9CADDD997B3ED382FD83A04F887776743B2D38F04E6DBB074FB2B6AFCE3DE98C064BEE093AA20FD7323F8D738AF6B5567412BC52499005213BF3AA891F40DB3F16E242A5BE099A85E374D3AAA1F285E2394E1A68914B6901245E17DECEB4972F9AC2D0D5284CFCD16ED05C4891BC3B3AF25670F758515C40B1FF19EA2364D7407AC47869879D261374491D1191CB45ED9993303239173B5C99F47152BF68F21B674DAAB09FC6CCFFEBB1FE2D7ACAD3AA42F25399E2C1367480681FA7157A9D6AFD27035240F3E9C87A96EFA80E60A8DBF50766587902C100000

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

