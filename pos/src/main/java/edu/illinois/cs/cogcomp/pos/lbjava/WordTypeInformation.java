// Modifying this comment will cause the next execution of LBJava to overwrite this file.
// F1B88000000000000000D51514E43C0301CFAC6B21D62A6211C5173848AC509017A22EC629D6BE06CE8C67920D6EFECA3E4A9827BCECCCECCA725A0B5814787ED1796131076E88DCD0CBB635E6F7B6C765B3D6EB8B31A554B5DF98A0E44C4C0BE41EC0249301C0A3C12F69B40427BEAB1810DA344286CB560206D93CD1C2611423BC14D15A460BA59889CEABDB09317B366A0F7C13866263F0096B637ACA638FD24F1DE06696ADA9D964E438CEA65C8404B2E338D44BE3239EF6AF0DA522750C5ACD0FA583E25CF1694A7FD352E1E721B71ECE81975DAE50D9343D3AD7B0457551E9051CA929C75B99445B7778826F5345E32B83073058F20380CFCF8E2AA84139B26193C6884E5056562CEB5D53A9D07B815114BDD9642B86CAB6162D0C5E2E37278566BC1EB395685E1DB28C0663638087B92D085456116E5CC1654F76EE72732BE9F274C78BBEA45D32A6845FEDDCF3EB48849CE9200000

package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscreteFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.DiscretePrimitiveStringFeature;
import edu.illinois.cs.cogcomp.lbjava.classify.FeatureVector;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;


public class WordTypeInformation extends Classifier
{
  public WordTypeInformation()
  {
    containingPackage = "edu.illinois.cs.cogcomp.pos.lbjava";
    name = "WordTypeInformation";
  }

  public String getInputType() { return "edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token"; }
  public String getOutputType() { return "discrete%"; }

  private static String[] __allowableValues = DiscreteFeature.BooleanValues;
  public static String[] getAllowableValues() { return __allowableValues; }
  public String[] allowableValues() { return __allowableValues; }

  public FeatureVector classify(Object __example)
  {
    if (!(__example instanceof Token))
    {
      String type = __example == null ? "null" : __example.getClass().getName();
      System.err.println("Classifier 'WordTypeInformation(Token)' defined on line 10 of POSAdditional.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    Token word = (Token) __example;

    FeatureVector __result;
    __result = new FeatureVector();
    String __id;
    String __value;

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
    for (; w != last; w = (Token) w.next, ++i)
    {
      boolean allCapitalized = true, allDigits = true, allNonLetters = true;
      for (int j = 0; j < w.form.length(); ++j)
      {
        char c = w.form.charAt(j);
        allCapitalized &= Character.isUpperCase(c);
        allDigits &= (Character.isDigit(c) || c == '.' || c == ',');
        allNonLetters &= !Character.isLetter(c);
      }
      __id = "" + ("c" + i);
      __value = "" + (allCapitalized);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
      __id = "" + ("d" + i);
      __value = "" + (allDigits);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
      __id = "" + ("p" + i);
      __value = "" + (allNonLetters);
      __result.addFeature(new DiscretePrimitiveStringFeature(this.containingPackage, this.name, __id, __value, valueIndexOf(__value), (short) 2));
    }
    return __result;
  }

  public FeatureVector[] classify(Object[] examples)
  {
    if (!(examples instanceof Token[]))
    {
      String type = examples == null ? "null" : examples.getClass().getName();
      System.err.println("Classifier 'WordTypeInformation(Token)' defined on line 10 of POSAdditional.lbj received '" + type + "' as input.");
      new Exception().printStackTrace();
      System.exit(1);
    }

    return super.classify(examples);
  }

  public int hashCode() { return "WordTypeInformation".hashCode(); }
  public boolean equals(Object o) { return o instanceof WordTypeInformation; }
}

