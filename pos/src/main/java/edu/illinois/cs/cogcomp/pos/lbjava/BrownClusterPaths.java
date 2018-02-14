// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D5091CE62C030144F756A782A44E6CA6FA572D30DF104A6F6883048528340709DE4D8451FFED57C9692C9CAD5FCCBD9DDDA675B52F4F889BDE3897FE607E9CE26DE7FE2BFCEF04601A7BBDC15618FEB1983C6A6457BD29D09FC2750D6C34B2C4E765546C31AB5B3F351A0BBED223DC5E33BE1565871CC66942F051CC0D57A0248EC97CE4EB4972F1B4968ECE51E27789734132628C395AF2DDF0EEA0B2A81161EF19EA2BF3492610B52EC0577AD832321E3CB5D6A95EA07A875269C6AE8B8F2C2DE4F99C5614239E89FFEA8C7B8635EB61562F8CE8C43E7FCD112221D19174C1FF53D7F2BD55CC3A5888FC5E703C0EFF443A100000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.ner.ExpressiveFeatures.BrownClusters;


public class BrownClusterPaths extends Classifier
{
  public BrownClusterPaths()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "BrownClusterPaths";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'BrownClusterPaths(Token)' defined on line 35 of POSAdditional.lbj received '" + type + "' as input.");
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
    for (; w != last; w = (Token) w.next)
    {
      String[] paths = bc.getPrefixes(w.form);
      for (int j = 0; j < paths.length; j++)
      {
        __id = "" + (i);
        __value = "" + (paths[j]);
        __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 0));
      }
      i++;
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'BrownClusterPaths(Token)' defined on line 35 of POSAdditional.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "BrownClusterPaths".hashCode(); }
  public boolean equals(Object o) { return o instanceof BrownClusterPaths; }
}

