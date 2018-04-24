package org.cogcomp.taxorel;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuany on 2/23/2018.
 */
public class WikiMention implements Serializable{
    public String _title;
    public String _type;
    public List<Pair<String, Integer>> categories = new ArrayList<>();
    public WikiMention(String title, String type, FeatureExtractor featureExtractor){
        _title = title;
        _type = type;
        categories = featureExtractor.getCategories(_title);
    }
}
