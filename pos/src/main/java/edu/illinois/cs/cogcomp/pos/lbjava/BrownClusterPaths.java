// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D509D4E43C030164FA2F1B0AA856211C6172C20E20590675571D6A3D4D1C27A2BD1C5905FEEC83E0496756DC8FDCB9F9D96FDA3A047F8757344BF6664F18CD2731E0EB8F81E39C22E0E67205798FEB24C36BD2A9BE455D158248286B10A516EA76A293E10663E3CC182CE707824378F8CC3AE60F485C223277D0CE86C82849A507BD9BE2FC326A0B2B47A0A07EB1DCB0AC9431BA3A3AF2D3C8EF22B2BC9461FF99E28DF9A4130C529A78AB1635BC4687F0E4BDE65B6C13D59819D6B961F5A3ADBE319F22A8FDDE8FC0D7E1DA74D91FAC09DE2C183325629972BE9872F7ECFDBAE7D9641D256A7ECF30786D9449E9100000

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

