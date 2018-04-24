package org.cogcomp.taxorel;

import java.util.List;

/**
 * Created by xuany on 3/7/2018.
 */
public class FIGERPair {
    public String _type;
    public List<String> _features;
    public List<Double> _similarities;
    public String _label;
    public FIGERPair(String type, List<String> features, String label, FeatureExtractor featureExtractor){
        _type = type;
        _features = features;
        _label = label;
        //List<String> tmpTypeList = new ArrayList<>();
        //tmpTypeList.add(type);

        //_similarities = featureExtractor.getLLMSim(features, tmpTypeList);
    }
}
