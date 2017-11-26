/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
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
