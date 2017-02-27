// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D3E81CA02C030144F756F8D25C2EDB5F8A0E54405CB49E126AB6B1C473129DA554AFFEE644C3D0BBF687C40456B961CBA0AA753E5053BB09D9DDD1906EC1AE51CB1E8340511F46640F116304395B51078790AFE3EFCBCE197F4195196CC2FA0610DA85F009DEC85C383ED9B98ABDE353A763E8003713849A373D5D2AC8870A61D9F89C3FB5237973716417940466881C8456D5944D9AF5A54AE9709BB8229E22254C40A13D6A10BC70DF7B54A10E000000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.*;
import edu.illinois.cs.cogcomp.lbjava.infer.*;
import edu.illinois.cs.cogcomp.lbjava.io.IOUtilities;
import edu.illinois.cs.cogcomp.lbjava.learn.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.*;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.*;
import edu.illinois.cs.cogcomp.pos.*;
import edu.illinois.cs.cogcomp.sim.*;
import java.io.FileNotFoundException;


/**
  * Returns the <i>form</i> of the word, i.e, the raw text that represents it.
  * The only exceptions are the brackets <code>'('</code>, <code>'['</code>,
  * and <code>'{'</code> which are translated to <code>'-LRB-'</code> and
  * <code>')'</code>, <code>']'</code>, <code>'}'</code> which are translated
  * to <code>'-RRB-'</code>.
  *
  * @author Nick Rizzolo
 **/
public class paragamVector extends Classifier
{
  public paragamVector()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "paragamVector";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "real[]"; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'paragamVector(Token)' defined on line 20 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token w = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    int __featureIndex = 0;
    double __value;

    PhraseSim ps = null;
    try
    {
      ps = PhraseSim.getInstance();
    }    catch (FileNotFoundException e)
    {

    }
    double[] vec = ps.getVector(w.form);
    for (int i = 0; i < vec.length; i++)
    {
      __value = vec[i];
      __result.addFeature(new RealArrayStringFeature(this.containingPackage, this.name, "", __value, __featureIndex++, 0));
    }

    for (int __i = 0; __i < __result.featuresSize(); ++__i)
      __result.getFeature(__i).setArrayLength(__featureIndex);

    return __result;
  }

  public double[] realValueArray(Object __example)
  {
    return classify(__example).realValueArray();
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'paragamVector(Token)' defined on line 20 of POSKnown.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "paragamVector".hashCode(); }
  public boolean equals(Object o) { return o instanceof paragamVector; }
}

