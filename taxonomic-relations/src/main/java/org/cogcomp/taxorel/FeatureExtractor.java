package org.cogcomp.taxorel;

import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import javatools.parsers.NounGroup;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by xuany on 11/12/2017.
 */
public class FeatureExtractor {
    POSAnnotator _posAnnotator = null;
    IdfManager _idfManager = null;
    StopWord _stopWord = null;
    int NUM_OF_DOCS = 5510659;
    int K = 2;
    public static final Set<String> INVALID_CATEGORY_HEAD = new HashSet<String>();
    static {
        INVALID_CATEGORY_HEAD.add("name");
        INVALID_CATEGORY_HEAD.add("surname");
        INVALID_CATEGORY_HEAD.add("genealogy");
        INVALID_CATEGORY_HEAD.add("convention");
        INVALID_CATEGORY_HEAD.add("category");
        INVALID_CATEGORY_HEAD.add("person");
        INVALID_CATEGORY_HEAD.add("redirect");
        INVALID_CATEGORY_HEAD.add("birth");
        INVALID_CATEGORY_HEAD.add("family");
        INVALID_CATEGORY_HEAD.add("history");
        INVALID_CATEGORY_HEAD.add("abstract");
        INVALID_CATEGORY_HEAD.add("form");
    }

    public FeatureExtractor(){
        _posAnnotator = new POSAnnotator();
        _idfManager = new IdfManager();
        _stopWord = new StopWord(true);
    }

    public void extractInstance(Instance instance){
        int statusCode = 0;
        if (!WikiHandler.existsEntry(instance.entity1)){
            statusCode -= 1;
        }
        if (!WikiHandler.existsEntry(instance.entity2)){
            statusCode -= 2;
        }
        if (statusCode != 0){
            instance.scoreCos_AbsCat = statusCode;
            instance.scoreCos_CatAbs = statusCode;
            instance.scoreCos_CatCat = statusCode;
            instance.scoreCos_AbsAbs = statusCode;
            instance.ratio_TtlCat = statusCode;
            instance.ratio_CatTtl = statusCode;
            instance.ratio_CatCat = statusCode;
            return;
        }
        instance.scorePmi_E1E2 = calculatePMI(instance.entity1, instance.entity2);
        List<ArticleQueryResult> A = new ArrayList<>();
        List<ArticleQueryResult> B = new ArrayList<>();
        getCategoryText(instance.entity1, instance.entity2, A, B);

        List<String> arrCategoriesA = new ArrayList<>();
        List<String> arrTitlesA = new ArrayList<>();
        List<String> arrAbstractsA = new ArrayList<>();
        List<String> arrHeadsA = new ArrayList<>();
        List<String> arrDomainA = new ArrayList<>();

        List<String> arrCategoriesB = new ArrayList<>();
        List<String> arrTitlesB = new ArrayList<>();
        List<String> arrAbstractsB = new ArrayList<>();
        List<String> arrHeadsB = new ArrayList<>();
        List<String> arrDomainB = new ArrayList<>();

        List<String> arrCategoriesBOWA = new ArrayList<>();
        List<String> arrAbstractsBOWA = new ArrayList<>();
        List<String> arrCategoriesBOWB = new ArrayList<>();
        List<String> arrAbstractsBOWB = new ArrayList<>();

        extractInfoToLists(A, arrCategoriesA, arrTitlesA, arrAbstractsA);
        extractInfoToLists(B, arrCategoriesB, arrTitlesB, arrAbstractsB);

        extractDetailCategoryInfo(arrAbstractsA, arrHeadsA, arrDomainA);
        extractDetailCategoryInfo(arrAbstractsB, arrHeadsB, arrDomainB);

        fillUpWordArray(arrCategoriesA, arrCategoriesBOWA);
        fillUpWordArray(arrAbstractsA, arrAbstractsBOWA);
        fillUpWordArray(arrCategoriesB, arrCategoriesBOWB);
        fillUpWordArray(arrAbstractsB, arrAbstractsBOWB);

        try {
            instance.scoreCos_AbsCat = getCosSim(arrAbstractsBOWA, arrCategoriesBOWB);
            instance.scoreCos_CatAbs = getCosSim(arrCategoriesBOWA, arrAbstractsBOWB);
            instance.scoreCos_CatCat = getCosSim(arrCategoriesBOWA, arrCategoriesBOWB);
            instance.scoreCos_AbsAbs = getCosSim(arrAbstractsBOWA, arrAbstractsBOWB);
            instance.ratio_TtlCat = getDirectionalRatio(Constants.FROM_E1_TO_E2, arrTitlesA, arrCategoriesA, arrHeadsA, arrDomainA,
                                                                                 arrTitlesB, arrCategoriesB, arrHeadsB, arrDomainB);
            instance.ratio_CatTtl = getDirectionalRatio(Constants.FROM_E2_TO_E1, arrTitlesA, arrCategoriesA, arrHeadsA, arrDomainA,
                                                                                 arrTitlesB, arrCategoriesB, arrHeadsB, arrDomainB);
            instance.ratio_CatCat = getCatCatRatio(arrCategoriesA, arrHeadsA, arrCategoriesB, arrHeadsB);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void fillUpWordArray(List<String> arrStrings,
                                 List<String> arrWords) {

        for (String title : arrStrings) {
            List<String> arrTokens = _stopWord.removeStopWords(title);
            for (String token : arrTokens)
                arrWords.add(token);

        }

    }

    public void extractDetailCategoryInfo(List<String> cats, List<String> heads, List<String> domains){
        for (String cat : cats) {
            NounGroup nounGroup = new NounGroup(cat);
            String head = nounGroup.head();
            if (INVALID_CATEGORY_HEAD.contains(head))
                continue;
            String preMod = nounGroup.preModifier();
            NounGroup postMod = nounGroup.postModifier();
            heads.add(head);
            if (preMod != null)
                head = preMod.replace('_', ' ') + " " + head;
            heads.add(head);
            if (postMod != null) {
                String postHead = postMod.head();
                String postPreMod = postMod.preModifier();
                domains.add(postHead);
                if (postPreMod != null)
                    postHead = postPreMod.replace('_', ' ') + " " + postHead;
                domains.add(postHead);
            }
        }
    }

    public void extractInfoToLists(List<ArticleQueryResult> searchResults, List<String> cats, List<String> titles, List<String> abs){
        System.out.println("Getting " + searchResults.size() + " category tree information");
        for (ArticleQueryResult r : searchResults){
            cats.addAll(extractCategory(r));
            titles.add(r.title);
            abs.add(r.extract);
        }
    }

    public List<String> extractCategory(ArticleQueryResult r){
        Set<String> exists = new HashSet<>();
        return extract(r.categories,  0, exists);
    }

    public List<String> extract(List<String> inputCats, int level, Set<String> exists){
        ArrayList<String> arrCats = new ArrayList<String>();
        if (level > K){
            return arrCats;
        }
        for (String c : inputCats){
            NounGroup nounGroup = new NounGroup(c);
            if (INVALID_CATEGORY_HEAD.contains(nounGroup.head())) {
                continue;
            }
            if (exists.contains(c)){
                continue;
            }
            arrCats.add(c);
            exists.add(c);
            arrCats.addAll(extract(WikiHandler.getParentCategory(c), level + 1, exists));
        }
        return arrCats;
    }

    private double calculatePMI(String termA, String termB){
        int hitBoth = WikiHandler.getTotalHits(termA + " " + termB);
        int hitE1 = WikiHandler.getTotalHits(termA);
        int hitE2 = WikiHandler.getTotalHits(termB);
        int numDocs = NUM_OF_DOCS;

        double pE1E2 = (double) hitBoth / (double) numDocs;
        double pE1 = (double) hitE1 / (double) numDocs;
        double pE2 = (double) hitE2 / (double) numDocs;

        double pmi = 0.0;
        if (pE1 * pE2 == 0)
            pmi = 0.0;
        else {
            if (pE1E2 > 0.0 && (pE1 * pE2) > 0.0) {
                pmi = Math.log(pE1E2 / (pE1 * pE2));
                DecimalFormat df = new DecimalFormat("#.###");
                pmi = Double.parseDouble(df.format(pmi));
            }
        }
        return pmi;
    }


    private Map<String, Integer> getTokenFreq(List<String> arrTokens) {

        Map<String, Integer> mapTokenFreq = new HashMap<String, Integer>();

        for (String token : arrTokens) {

            if (mapTokenFreq.containsKey(token)) {
                Integer freq = mapTokenFreq.get(token);
                freq++;
                mapTokenFreq.put(token, freq);
            } else {
                Integer freq = new Integer(1);
                mapTokenFreq.put(token, freq);
            }
        }

        return mapTokenFreq;
    }

    private double getCosSim(List<String> arrBagOfWords_1,
                             List<String> arrBagOfWords_2) throws Exception {

        Map<String, Integer> mapTokenFreq1 = getTokenFreq(arrBagOfWords_1);

        Map<String, Integer> mapTokenFreq2 = getTokenFreq(arrBagOfWords_2);

        Set<String> keySet = new HashSet<String>(mapTokenFreq1.keySet());

        Set<String> anotherSet = new HashSet<String>(mapTokenFreq2.keySet());

        keySet.addAll(anotherSet);

        ArrayList<Integer> arrVector1 = new ArrayList<Integer>();
        ArrayList<Integer> arrVector2 = new ArrayList<Integer>();

        for (String key : keySet) {

            if (mapTokenFreq1.containsKey(key)) {
                // arrVector1.add(mapTokenFreq1.get(key));
                arrVector1.add(1);
            } else {
                arrVector1.add(0);
            }

            if (mapTokenFreq2.containsKey(key)) {
                // arrVector2.add(mapTokenFreq2.get(key));
                arrVector2.add(1);
            } else {
                arrVector2.add(0);
            }

        }

        double cosSim = CosineSimilarity.getSimilarity(arrVector1, arrVector2);

        DecimalFormat df = new DecimalFormat("#.###");
        // DecimalFormat df = new DecimalFormat("#.########");
        cosSim = Double.parseDouble(df.format(cosSim));

        return cosSim;
    }


    private double getDirectionalRatio(int direction, List<String> arrTitles1, List<String> arrCategories1, List<String> arrHeads1, List<String> arrDomains1,
                                                      List<String> arrTitles2, List<String> arrCategories2, List<String> arrHeads2, List<String> arrDomains2) {

        List<String> arrTitles = null;
        List<String> arrCats = null;
        List<String> arrHeads = null;
        List<String> arrDomains = null;

        Set<String> setValues = new HashSet<String>();

        if (direction == Constants.FROM_E1_TO_E2) {
            arrTitles = arrTitles1;
            arrCats = arrCategories2;
            arrHeads = arrHeads2;
            arrDomains = arrDomains2;
        } else {
            arrTitles = arrTitles2;
            arrCats = arrCategories1;
            arrHeads = arrHeads1;
            arrDomains = arrDomains1;
        }

        setValues.addAll(arrTitles);
        Set<String> setCats = new HashSet<String>(arrCats);
        Set<String> setHeads = new HashSet<String>(arrHeads);
        Set<String> setDomains = new HashSet<String>(arrDomains);

        Set<String> setAll = new HashSet<String>();
        setAll.addAll(setCats);
        setAll.addAll(setHeads);
        setAll.addAll(setDomains);
        Set<String> setInter = new HashSet<String>(setValues);
        setInter.retainAll(setAll);

        Set<String> setUnion = new HashSet<String>(setValues);
        setUnion.addAll(setAll);

        double ratio = 0.0;

        if (setUnion.size() > 0)
            ratio = (double) setInter.size() / (double) setUnion.size();

        DecimalFormat df = new DecimalFormat("#.###");
        ratio = Double.parseDouble(df.format(ratio));

        return ratio;

    }

    private double getCatCatRatio(List<String> arrCategories1, List<String> arrHeads1, List<String> arrCategories2, List<String> arrHeads2) {

        Set<String> setEntity1 = new HashSet<String>(arrCategories1);
        setEntity1.addAll(arrHeads1);

        Set<String> setEntity2 = new HashSet<String>(arrCategories2);
        setEntity2.addAll(arrHeads2);

        Set<String> setInter = new HashSet<String>(setEntity1);
        setInter.retainAll(setEntity2);

        Set<String> setUnion = new HashSet<String>(setEntity1);
        setUnion.addAll(setEntity2);

        double ratio = 0.0;
        if (setUnion.size() > 0)
            ratio = (double) setInter.size() / (double) setUnion.size();

        DecimalFormat df = new DecimalFormat("#.###");
        // DecimalFormat df = new DecimalFormat("#.########");
        ratio = Double.parseDouble(df.format(ratio));

        return ratio;
    }

    public String getCategoryText(String termA, String termB, List<ArticleQueryResult> A, List<ArticleQueryResult> B){
        List<String> req = new ArrayList<>();
        List<String> reqA = new ArrayList<>();
        List<String> reqB = new ArrayList<>();
        req.add(termA);
        req.add(termB);
        reqA.add(termA);
        reqB.add(termB);
        System.out.println("Getting titles combined");
        List<String> titlesCombined = WikiHandler.getTitlesFromQuery(req);
        titlesCombined = titlesCombined.subList(0, Math.min(100, titlesCombined.size()));
        System.out.println("Getting titles combined done.");
        System.out.println("Getting titles A");
        List<String> titlesA = WikiHandler.getTitlesFromQuery(reqA);
        System.out.println("Getting titles A done.");
        System.out.println("Getting titles B");
        List<String> titlesB = WikiHandler.getTitlesFromQuery(reqB);
        System.out.println("Getting titles B done.");
        List<String> categories = new ArrayList<>();
        System.out.println("Getting categories for " + titlesCombined.size() + " titles");
        for (String t : titlesCombined){
            categories.addAll(WikiHandler.getInfoFromTitle(t).categories);
        }
        System.out.println("Getting categories done.");
        System.out.println("Getting supplements");
        String concat = "";
        for (String cat : categories){
            concat += cat + ", ";
        }
        Map<String, Integer> freq = new HashMap<>();
        if (concat.length() > 0) {
            TextAnnotationBuilder tab;
            boolean splitOnHyphens = false;
            tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));
            TextAnnotation ta = tab.createTextAnnotation("", "", concat);
            try {
                ta.addView(_posAnnotator);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Constituent c : ta.getView(ViewNames.POS)) {
                if (c.getLabel().startsWith("NN") || c.getLabel().startsWith("JJ")) {
                    if (!freq.containsKey(c.toString().toLowerCase())) {
                        freq.put(c.toString().toLowerCase(), 1);
                    } else {
                        freq.put(c.toString().toLowerCase(), freq.get(c.toString().toLowerCase()) + 1);
                    }
                }
            }
        }
        Map<String, Double> scoredFreq = scoringToken(freq);
        List<String> supplements = getTopTokenSupplmentString(scoredFreq);
        List<String> A_Args = new ArrayList<>();
        A_Args.add(termA);
        A_Args.addAll(supplements);

        List<String> B_Args = new ArrayList<>();
        B_Args.add(termB);
        B_Args.addAll(supplements);
        System.out.println("Getting supplements done.");


        List<String> titleCombinedA = WikiHandler.getTitlesFromQuery(A_Args);
        titleCombinedA = titleCombinedA.subList(0, Math.min(50, titleCombinedA.size()));
        Set<String> titleAIntersection = new HashSet<>(titleCombinedA);
        titleAIntersection.retainAll(titlesA);
        List<String> titleCombinedB = WikiHandler.getTitlesFromQuery(B_Args);
        titleCombinedB = titleCombinedB.subList(0, Math.min(50, titleCombinedB.size()));
        Set<String> titleBIntersection = new HashSet<>(titleCombinedB);
        titleBIntersection.retainAll(titlesB);

        List<String> candidatesA = new ArrayList<>();
        List<String> candidatesB = new ArrayList<>();

        int count = 0;
        for (String s : titleAIntersection){
            candidatesA.add(s);
            count ++;
            if (count > 10){
                break;
            }
        }
        candidatesA.add(titlesA.get(0));

        count = 0;
        for (String s : titleBIntersection){
            candidatesB.add(s);
            count ++;
            if (count > 10){
                break;
            }
        }
        candidatesB.add(titlesB.get(0));

        System.out.println("Getting full information for " + candidatesA.size() + " titles for A");
        System.out.println("Intersection of A: ");
        System.out.println(candidatesA);
        for (String a : candidatesA){
            A.add(WikiHandler.getInfoFromTitle(a));
        }
        System.out.println("Getting full information for A done.");
        System.out.println("Getting full information for " + candidatesB.size() + " titles for B");
        System.out.println("Intersection of B: ");
        System.out.println(candidatesB);
        for (String b : candidatesB){
            B.add(WikiHandler.getInfoFromTitle(b));
        }
        System.out.println("Getting full information for B done.");
        return null;
    }

    private Map<String, Double> scoringToken(Map<String, Integer> histogram) {
        Map<String, Double> scoredFreq = new HashMap<>();
        Set<String> keySet = histogram.keySet();
        for (String key : keySet) {
            double idf = _idfManager.getIdf(key);
            Integer tf = histogram.get(key);
            scoredFreq.put(key, idf * tf.intValue());
        }
        return scoredFreq;
    }

    private List<String> getTopTokenSupplmentString(Map<String, Double> freq){
        List<String> ret = new ArrayList<>();
        freq = WikiHandler.sortByValue(freq);
        int count = 0;
        for (String s : freq.keySet()){
            if (count > 4){
                break;
            }
            count ++;
            ret.add(s);
        }
        return ret;
    }
}
