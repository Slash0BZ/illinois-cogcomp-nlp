package org.cogcomp.taxorel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuany on 11/12/2017.
 */
public class ArticleQueryResult {
    public List<String> categories;
    public String extract;
    public String title;
    public ArticleQueryResult(String title){
        this.title = title;
        categories = new ArrayList<>();
        extract = "";
    }
}
