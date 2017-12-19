/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.taxorel;

import com.google.common.collect.ObjectArrays;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.config.SimConfigurator;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.pos.POSAnnotator;
import edu.illinois.cs.cogcomp.sim.LLMStringSim;
import edu.illinois.cs.cogcomp.sim.Metric;
import edu.illinois.cs.cogcomp.sim.MetricResponse;
import edu.illinois.cs.cogcomp.sim.WordSim;
import javatools.parsers.NounGroup;
import org.jibx.schema.codegen.extend.DefaultNameConverter;
import org.jibx.schema.codegen.extend.NameConverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by xuany on 11/12/2017.
 */
public class FeatureExtractor {
    POSAnnotator _posAnnotator = null;
    IdfManager _idfManager = null;
    StopWord _stopWord = null;
    public WordSim _wordSim = null;
    public Metric _LLMSim = null;
    public NameConverter _nameConverter = null;
    public Connection _conn = null;
    int NUM_OF_DOCS = 5510659;
    int K = 2;
    public static final Set<String> INVALID_CATEGORY_HEAD = new HashSet<String>();
    public static String[] skipWords = {"establishments", "births", "deaths", "stub", "history", "family", "person", "people", "events", "articles"};
    public static String[] skipWordsForParents = {"articles"};
    List<String> skipWordList;
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
        //_idfManager = new IdfManager();
        _stopWord = new StopWord(true);
        _nameConverter = new DefaultNameConverter();
        try {
            _wordSim = new WordSim(new SimConfigurator().getConfig(new ResourceManager("taxonomic-relations/src/main/config/configurations.properties")), "paragram");
            _LLMSim = new LLMStringSim("taxonomic-relations/src/main/config/configurations.properties");
            _conn = DriverManager.getConnection("jdbc:mysql://localhost/wiki", "wiki", "Wikipedia2017");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        skipWordList = new ArrayList<>(Arrays.asList(skipWords));
    }

    public void extractInstance(Instance instance){
        int statusCode = 0;
        if (!WikiHandler.existsEntry(instance.entity1)){
            statusCode -= 1;
        }
        if (!WikiHandler.existsEntry(instance.entity2)){
            statusCode -= 2;
        }
        List<ArticleQueryResult> A = new ArrayList<>();
        List<ArticleQueryResult> B = new ArrayList<>();
        instance.initialPrediction = settleEntity(instance.entity1, instance.entity2, A, B);
        //getCategoryText(instance.entity1, instance.entity2, A, B);
        if (A.size() == 0){
            statusCode -= 1;
        }
        if (B.size() == 0){
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
        for (ArticleQueryResult r : searchResults){
            cats.addAll(extractCategory(r));
            titles.add(r.title);
            abs.add(r.extract);
        }
    }

    public List<String> extractCategory(ArticleQueryResult r){
        Set<String> exists = new HashSet<>();
        return extract(r.categories,  0, 0);
    }

    public List<String> extract(List<String> inputCats, int level, int counter){
        ArrayList<String> arrCats = new ArrayList<String>();
        if (level > K){
            return arrCats;
        }
        if (counter > 10){
            return arrCats;
        }
        for (String c : inputCats){
            arrCats.add(c);
            List<String> newExtracts = WikiHandler.getParentCategory(c, _conn);
            if (newExtracts.size() == 1){
                arrCats.addAll(extract(newExtracts, level, counter + 1));
            }
            else {
                arrCats.addAll(extract(newExtracts, level + 1, counter + 1));
            }
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
        //List<String> titlesCombined = WikiHandler.getTitlesFromQuery(req);
        //titlesCombined = titlesCombined.subList(0, Math.min(100, titlesCombined.size()));
        List<String> titlesCombined = new ArrayList<>();
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
            //candidatesA.add(s);
            count ++;
            if (count > 10){
                break;
            }
        }
        candidatesA.add(titlesA.get(0));

        count = 0;
        for (String s : titleBIntersection){
            //candidatesB.add(s);
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

    public boolean mostMatch (String target, String test){
        target = target.toLowerCase();
        target = target.replace("-", " ");
        target = target.replace(",", "");
        test = test.toLowerCase();
        test = test.replace("-", " ");
        test = test.replace(",", "");
        String[] testTokens = test.split("\\s+");
        String[] targetTokens = target.split("\\s+");
        int hit = 0;
        for (String stest : testTokens){
            if (target.contains(stest)){
                hit ++;
            }else {
                for (String starget : targetTokens){
                    if (_wordSim.compare(starget, stest).score >= 0.8){
                        hit ++;
                    }
                }
            }
        }
        if ((double)hit / (double)(targetTokens.length) >= 0.5){
            return true;
        }
        return false;
    }

    public String settleEntity(String termA, String termB, List<ArticleQueryResult> A, List<ArticleQueryResult> B){
        List<String> reqA = new ArrayList<>();
        List<String> reqB = new ArrayList<>();
        List<String> reqAB = new ArrayList<>();
        reqA.add(termA);
        reqB.add(termB);
        reqAB.add(termA);
        reqAB.add(termB);
        List<String> titlesARaw = WikiHandler.getTitlesFromQuery(reqA);
        List<String> titlesBRaw = WikiHandler.getTitlesFromQuery(reqB);
        List<String> titlesABRaw = WikiHandler.getTitlesFromQuery(reqAB);

        List<String> titlesA = new ArrayList<>();
        List<String> titlesB = new ArrayList<>();

        for (String sa : titlesARaw){
            String temp = sa.replaceAll("\\(.*\\)", "");
            if (mostMatch(termA, temp) && mostMatch(temp, termA) && !sa.contains("disambiguation")){
                boolean add = true;
                for (String c : WikiHandler.getInfoFromTitle(sa).categories){
                    if (c.toLowerCase().contains("disambiguation")){
                        add = false;
                    }
                }
                if (add) {
                    titlesA.add(sa);
                }
            }
        }
        for (String sb : titlesBRaw){
            String temp = sb.replaceAll("\\(.*\\)", "");
            if (mostMatch(termB, temp) && mostMatch(temp, termB) && !sb.contains("disambiguation")){
                boolean add = true;
                for (String c : WikiHandler.getInfoFromTitle(sb).categories){
                    if (c.toLowerCase().contains("disambiguation")){
                        add = false;
                    }
                }
                if (add) {
                    titlesB.add(sb);
                }
            }
        }

        if (titlesA.size() == 0) {
            titlesA = titlesARaw.subList(0, Math.min(3, titlesARaw.size()));
        }
        titlesA = titlesA.subList(0, Math.min(3, titlesA.size()));
        if (titlesB.size() == 0) {
            System.out.println("Added by insufficient candidates");
            titlesB = titlesBRaw.subList(0, Math.min(3, titlesBRaw.size()));
        }
        titlesB = titlesB.subList(0, Math.min(3, titlesB.size()));

        List<String> combinedChoices = titlesABRaw.subList(0, Math.min(3, titlesABRaw.size()));


        String titleA = null;
        String titleB = null;

        boolean resolvedByHierarchy = false;

        for (String sa : titlesA){
            if (combinedChoices.contains(sa)){
                titleA = sa;
                titleB = titlesB.get(0);
                resolvedByHierarchy = true;
                break;
            }
        }
        for (String sb : titlesB){
            if (combinedChoices.contains(sb)){
                titleB = sb;
                titleA = titlesA.get(0);
                resolvedByHierarchy = true;
                break;
            }
        }

        if (titlesA.size() == 1 && !resolvedByHierarchy){
            titleA = titlesA.get(0);
        }
        if (titlesB.size() == 1 && !resolvedByHierarchy){
            titleB = titlesB.get(0);
        }

        Map<Pair<String, String>, Integer> freqMap = new HashMap<>();
        if (!resolvedByHierarchy && (titleA == null || titleB == null)){
            for (String jointTitle : combinedChoices){
                String content = WikiHandler.getContentByTitle(jointTitle);
                for (String sa : titlesA){
                    for (String sb : titlesB){
                        if ((content.contains("[" + sa + "|") || content.contains("[" + sa + "]")) && (content.contains("[" + sb + "|") || content.contains("[" + sb + "]"))){
                            Pair<String, String> cur = new Pair(sa, sb);
                            if (freqMap.containsKey(cur)){
                                freqMap.put(cur, freqMap.get(cur) + 1);
                            }
                            else {
                                freqMap.put(cur, 1);
                            }
                        }
                    }
                }
            }
            if (freqMap.size() > 0){
                freqMap = WikiHandler.sortByValue(freqMap);
                titleA = ((Pair<String, String>)freqMap.keySet().toArray()[0]).getFirst();
                titleB = ((Pair<String, String>)freqMap.keySet().toArray()[0]).getSecond();
            }
        }

        if (titleA == null){
            if (titlesA.size() > 0) {
                titleA = titlesA.get(0);
            }
        }
        if (titleB == null){
            if (titlesB.size() > 0) {
                titleB = titlesB.get(0);
            }
        }
        System.out.println(termA + ": " + titleA);
        System.out.println(termB + ": " + titleB);
        System.out.println();

        if (titleA != null) {
            A.add(WikiHandler.getInfoFromTitle(titleA));
        }
        if (titleB != null) {
            B.add(WikiHandler.getInfoFromTitle(titleB));
        }
        System.out.println(termA + ": " + titlesA);
        System.out.println(termA + "'s first choice: " + titleA);
        System.out.println(termB + ": " + titlesB);
        System.out.println(termB + "'s first choice: " + titleB);

        return classifyTitles(titleA, titleB, termA, termB);

    }

    public String classifyTitles(String titleA, String titleB, String termA, String termB){
        if (titleA == null || titleB == null){
            return "-1";
        }
        List<String> catesA = WikiHandler.getInfoFromTitle(titleA).categories;
        List<String> catesB = WikiHandler.getInfoFromTitle(titleB).categories;
        if (catesA.size() == 1){
            catesA.addAll(WikiHandler.getParentCategory(catesA.get(0), _conn));
        }
        if (catesB.size() == 1){
            catesB.addAll(WikiHandler.getParentCategory(catesB.get(0), _conn));
        }

        List<String> catesAFull = extract(catesA, 0, 0);
        System.out.println("A Cats: " + catesA);
        List<String> catesBFull = extract(catesB, 0, 0);
        System.out.println("B Cats: " + catesB);

        List<String> detA = new ArrayList<>();
        for (String ca : getFilteredNouns(catesAFull)){
            double scoreTerm = Math.max(getLLMScore(ca, termA), getLLMScore(termA, ca));
            double scoreTitle = Math.max(getLLMScore(ca, titleA), getLLMScore(titleA, ca));;
            if (scoreTerm < 0.8 && scoreTitle < 0.8){
                detA.add(ca);
            }
        }

        System.out.println("DETA: " + detA);
        List<String> detB = new ArrayList<>();
        for (String cb : getFilteredNouns(catesBFull)){
            double scoreTerm = Math.max(getLLMScore(cb, termB), getLLMScore(termB, cb));
            double scoreTitle = Math.max(getLLMScore(cb, titleB), getLLMScore(titleB, cb));;
            if (scoreTerm < 0.8 && scoreTitle < 0.8){
                detB.add(cb);
            }
        }

        System.out.println("DETB: " + detB);

        boolean isTwo = false;
        boolean isOne = false;

        List<String> titleSetA = new ArrayList<>();
        titleSetA.add(titleA);
        titleSetA.add(termA);

        List<String> titleSetB = new ArrayList<>();
        titleSetB.add(titleB);
        titleSetB.add(termB);

        for (String sb : detB){
            sb = depluralizePhrase(sb);
            for (String tsa : titleSetA){
                tsa = depluralizePhrase(tsa);
                if (getLLMScore(sb, tsa) > 0.85){
                    System.out.println("Reason for 1: " + sb + " " + tsa);
                    isOne = true;
                    break;
                }
            }
        }

        for (String sa : detA){
            sa = depluralizePhrase(sa);
            for (String tsb : titleSetB){
                tsb = depluralizePhrase(tsb);
                if (getLLMScore(sa, tsb) > 0.85){
                    System.out.println("Reason for 2: " + sa + " " + tsb);
                    System.out.println(detA);
                    isTwo = true;
                    break;
                }
            }
        }

        if (isOne && !isTwo){
            return "1";
        }
        if (!isOne && isTwo){
            return "2";
        }
        List<String> cateALv2 = new ArrayList<>();
        List<String> cateBLv2 = new ArrayList<>();
        for (String c : catesA){
            cateALv2.addAll(WikiHandler.getParentCategory(c, _conn));
        }
        cateALv2.addAll(catesA);

        for (String c : catesB){
            cateBLv2.addAll(WikiHandler.getParentCategory(c, _conn));
        }
        cateBLv2.addAll(catesB);

        List<Double> LLMScores = getLLMSim(cateALv2, cateBLv2);
        System.out.println("Filtered A: " + getFilteredNouns(catesA));
        System.out.println("LLMScore: " + LLMScores);
        System.out.println(titleA + ": " + cateALv2);
        System.out.println(titleB + ": " + cateBLv2);

        if (LLMScores.get(0) > 0.8){
            return "3";
        }

        return "0";
    }

    public String depluralizePhrase(String input){
        String[] token = input.split(" ");
        String ret = "";
        for (String t : token){
            ret += _nameConverter.depluralize(t) + " ";
        }
        if (ret.length() > 0){
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    public List<String> getFilteredNouns(List<String> inputs){
        List<String> retTmp = new ArrayList<>();
        for (String i : inputs) {
            TextAnnotationBuilder tab;
            boolean splitOnHyphens = false;
            tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));
            TextAnnotation ta = tab.createTextAnnotation("", "", i);
            try {
                ta.addView(_posAnnotator);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String cur = "";
            for (Constituent c : ta.getView(ViewNames.POS)) {
                if (c.getLabel().startsWith("IN")){
                    break;
                }
                if (c.getLabel().startsWith("NNS")) {
                    cur += c.toString() + " ";
                }
            }
            if (cur.length() > 0){
                cur = cur.substring(0, cur.length() - 1);
            }
            boolean skipCur = false;
            for (String skip: skipWordList){
                if (cur.toLowerCase().contains(skip.toLowerCase())){
                    skipCur = true;
                }
            }
            if (skipCur){
                continue;
            }
            retTmp.add(cur);
        }
        List<String> retFinal = new ArrayList<>();
        for (String s : retTmp){
            TextAnnotationBuilder tab;
            boolean splitOnHyphens = false;
            tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));
            TextAnnotation ta = tab.createTextAnnotation("", "", s);
            if (s.length() == 0){
                continue;
            }
            try {
                ta.addView(_posAnnotator);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String cur = "";
            for (Constituent c : ta.getView(ViewNames.POS)) {
                if (c.getLabel().contains("S")){
                    cur += c.toString() + " ";
                }
            }
            if (cur.length() > 0){
                cur = cur.substring(0, cur.length() - 1);
                retFinal.add(cur);
            }
        }
        return retTmp;
    }

    public List<Double> getLLMSim(List<String> catsA, List<String> catsB){
        List<Double> ret = new ArrayList<>();
        double maxScore = 0.0;
        String maxString = "";
        double avgScoreA = 0.0;
        double totalScoreA = 0.0;
        double avgScoreB = 0.0;
        double totalScoreB = 0.0;
        catsA = getFilteredNouns(catsA);
        catsB = getFilteredNouns(catsB);
        for (String ca : catsA){
            double caMax = 0.0;
            for (String cb : catsB){
                MetricResponse LLMResponse = _LLMSim.compare(ca, cb);
                double cur = 0.0;
                if (LLMResponse != null) {
                    cur = _LLMSim.compare(ca, cb).score;
                }
                if (cur > maxScore){
                    maxScore = cur;
                    maxString = ca + " " + cb;
                }
                if (cur > caMax){
                    caMax = cur;
                }
            }
            avgScoreA += caMax;
            totalScoreA += caMax;
        }
        avgScoreA /= (double)catsA.size();
        for (String cb : catsB){
            double cbMax = 0.0;
            for (String ca : catsA){
                MetricResponse LLMResponse = _LLMSim.compare(ca, cb);
                double cur = 0.0;
                if (LLMResponse != null) {
                    cur = _LLMSim.compare(ca, cb).score;
                }
                if (cur > cbMax){
                    cbMax = cur;
                }
            }
            avgScoreB += cbMax;
            totalScoreB += cbMax;
        }
        avgScoreB /= (double)catsB.size();
        ret.add(maxScore);
        ret.add(avgScoreA);
        ret.add(avgScoreB);
        ret.add(totalScoreA);
        ret.add(totalScoreB);
        System.out.println("MAXSTRING: " + maxString);
        return ret;
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

    public double getLLMScore(String a, String b){
        MetricResponse m = _LLMSim.compare(a, b);
        if (m != null){
            return m.score;
        }
        return 0.0;
    }
    String[] locations = new String[]{"country", "county", "park", "province", "cemetery", "glacier", "astral body", "island", "body of water", "mountain"};
    String[] organization = new String[]{"educational institution", "terrorism", "military", "fraternities and sororities", "sports league", "sports team", "political party", "stock exchange", "government agency", "airline", "railway", "news agency"};
    String[] work_of_art = new String[]{"play", "music", "broadcast_programming", "film", "newspaper"};
    String[] facility = new String[]{"restaurant", "sports venue", "library", "hospital", "airport", "power station", "hotel", "bridge", "dam", "theater"};
    String[] person = new String[]{"politician", "coach", "sportspeople", "clergy", "architect", "engineer", "author", "physician", "surgeon", "soldier", "monarch", "film director", "actor", "musician"};
    String[] medicine = new String[]{"symptom", "therapy", "drug"};
    String[] event = new String[]{"Natural disaster", "election", "Sports events", "war", "protest", "Terrorist incidents"};
    String[] product = new String[]{"food", "engine", "camera", "train", "mobile phone", "car", "ship", "computer", "airplane", "weapon"};
    String[][] types = new String[][]{locations, organization, work_of_art, facility, person, medicine, event, product};
    String[][] typeIdentifier = new String[][]{person, organization, ObjectArrays.concat(locations, facility, String.class), event};

    public Map<String, Pair<String, List<String>>> preprocessTypes(){
        Map<String, Pair<String, List<String>>> ret = new HashMap<>();
        for (String[] typeGroup : types){
            for (String type : typeGroup){
                List<String> query = new ArrayList<>();
                query.add(type);
                List<String> titles = WikiHandler.getTitlesFromQuery(query);
                Pair<String, List<String>> curPair = new Pair<>(titles.get(0), WikiHandler.getInfoFromTitle(titles.get(0)).categories);
                ret.put(type, curPair);
            }
        }
        return ret;
    }

    public List<List<String>> clusterTypes(List<String> types){
        List<List<String>> ret = new ArrayList<>();
        List<String> typesCopy = new ArrayList<>(types);
        List<String> traversed = new ArrayList<>();
        while (true){
            if (traversed.size() == typesCopy.size()){
                break;
            }
            List<String> curCluster = new ArrayList<>();
            String seed = "";
            for (String s : typesCopy){
                if (!traversed.contains(s)){
                    seed = s;
                }
            }
            curCluster.add(seed);
            traversed.add(seed);
            for (String s : typesCopy){
                if (s.equals(seed)){
                    continue;
                }
                if (traversed.contains(s)){
                    continue;
                }
                boolean merge = true;
                for (String c : curCluster){
                    MetricResponse metricResponse = _LLMSim.compare(c, s);
                    if (metricResponse == null){
                        continue;
                    }
                    if (metricResponse.score < 0.5){
                        merge = false;
                        break;
                    }
                }
                if (merge){
                    curCluster.add(s);
                    traversed.add(s);
                }
            }
            ret.add(curCluster);
            for (List<String> group : ret){
                System.out.println(group);
            }
        }
        return ret;
    }

    public List<String> typer(String input){
        List<String> ret = new ArrayList<>();
        List<String> query = new ArrayList<>();
        query.add(input);
        String title = WikiHandler.getTitlesFromQuery(query).get(0);

        List<String> catesA = WikiHandler.getInfoFromTitle(title).categories;

        if (catesA.size() == 1){
            catesA.addAll(WikiHandler.getParentCategory(catesA.get(0), _conn));
        }

        List<List<String>> outputs = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            outputs.add(new ArrayList<String>());
        }
        int[] count = new int[]{0, 0, 0, 0};
        Map<String, boolean[]> results = new HashMap<>();
        for (String s : catesA){
            boolean[] result = isCoarseTypeHelperConcur(s, 0, outputs);
            results.put(s, result);
            for (int i = 0; i < result.length; i++){
                if (result[i]){
                    count[i] += 1;
                }
            }
        }
        int maxCountIdx = 0;
        int maxCount = count[0];
        for (int i = 1; i < count.length; i++){
            if (count[i] > maxCount) {
                maxCountIdx = i;
                maxCount = count[i];
            }
        }
        Set<String> keywords = new HashSet<>(getFilteredNouns(outputs.get(maxCountIdx)));



        List<String> detA = new ArrayList<>();
        for (String s : results.keySet()){
            if (results.get(s)[maxCountIdx]){
                detA.add(s);
            }
        }
        List<String> detAFull = new ArrayList<>(keywords);


        List<String> confidenceSet = new ArrayList<>();
        for (String s : detA){
            confidenceSet.add(depluralizePhrase(s));
        }
        for (String s : detAFull){
            confidenceSet.add(depluralizePhrase(s));
        }

        int maxScore = 0;
        String chosen = "";
        for (String type : typeIdentifier[maxCountIdx]) {
            for (String sa : detAFull) {
                sa = depluralizePhrase(sa);
                String sb = depluralizePhrase(type);
                if (getLLMScore(sa, sb) > 0.9) {
                    int curScore = 0;
                    for (String da : confidenceSet){
                        if (getLLMScore(da, sa) > 0.9 || getLLMScore(sa, da) > 0.9){
                            curScore ++;
                        }
                    }
                    if (curScore > maxScore){
                        maxScore = curScore;
                        chosen = type;
                    }
                    break;
                }
            }
        }
        ret.add(chosen);

        return ret;
    }


    public boolean isCoarseTypeHelper(String cur, String target, int level, List<String> outputs){
        if (level > 8){
            return false;
        }
        List<String> cats = WikiHandler.getParentCategory(cur, _conn);
        int fitCount = 0;
        for (String s : cats){
            if (s.toLowerCase().equals(target.toLowerCase())){
                outputs.add(s);
                return true;
            }
            if (isCoarseTypeHelper(s, target, level + 1, outputs)){
                fitCount ++;
            }
        }
        if (((double)fitCount / (double)cats.size()) >= 0.5){
            outputs.add(cur);
            return true;
        }
        return false;
    }

    public double calculateTypeScore(String cur, String target, int level){
        if (level > 8){
            return 0.0;
        }
        List<String> cats = WikiHandler.getParentCategory(cur, _conn);
        if (cats.size() == 0){
            return 0.0;
        }
        double score = 0.0;
        for (String s : cats){
            if (s.toLowerCase().equals(target.toLowerCase())){
                return 1.0;
            }
            double accScore = calculateTypeScore(s, target, level + 1);
            score += accScore;
        }
        score = score / (double)cats.size();
        return score;
    }

    public boolean[] isCoarseTypeHelperConcur(String cur, int level, List<List<String>> outputs){
        if (level > 9){
            return new boolean[]{false, false, false, false};
        }
        List<String> catsRaw = WikiHandler.getParentCategory(cur, _conn);
        List<String> cats = new ArrayList<>();
        for (String s : catsRaw){
            boolean add = true;
            for (String skips : skipWordsForParents) {
                if (s.toLowerCase().contains(skips)){
                    add = false;
                    break;
                }
            }
            if (add){
                cats.add(s);
            }
        }
        int[] fitCount = new int[]{0, 0, 0, 0};
        boolean[] initialRet = new boolean[]{false, false, false, false};
        if (cats.contains("People")){
            int idx = 0;
            initialRet[idx] = true;
            if (level < 3) {
                outputs.get(idx).add(cur);
            }
        }
        if (cats.contains("Organizations")){
            int idx = 1;
            initialRet[idx] = true;
            if (level < 3) {
                outputs.get(idx).add(cur);
            }
        }
        if (cats.contains("Places")){
            int idx = 2;
            initialRet[idx] = true;
            initialRet[idx] = true;
            if (level < 3) {
                outputs.get(idx).add(cur);
            }
        }
        if (cats.contains("Events")){
            int idx = 3;
            initialRet[idx] = true;
            initialRet[idx] = true;
            if (level < 3) {
                outputs.get(idx).add(cur);
            }
        }
        for (boolean b : initialRet){
            if (b){
                return initialRet;
            }
        }
        for (String s : cats){

            boolean[] result = (isCoarseTypeHelperConcur(s, level + 1, outputs));
            for (int i = 0; i < result.length; i++){

                if (result[i]){
                    fitCount[i] += 1;
                }
            }
        }
        int maxCountIdx = 0;
        int maxCount = fitCount[0];
        for (int i = 1; i < fitCount.length; i++){
            if (fitCount[i] > maxCount) {
                maxCountIdx = i;
                maxCount = fitCount[i];
            }
        }
        if (((double)fitCount[maxCountIdx] / (double)cats.size()) >= 0.5){
            initialRet[maxCountIdx] = true;
            if (level < 3) {
                outputs.get(maxCountIdx).add(cur);
            }
        }
        return initialRet;
    }

}
