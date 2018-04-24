package org.cogcomp.taxorel;

import java.io.Serializable;
import java.util.List;

public class FIGERInstance implements Serializable {
    public List<String> _goldTypes;
    public List<String> _types;
    public FIGERInstance(List<String> types, List<String> goldTypes){
        _types = types;
        _goldTypes = goldTypes;
    }
}
