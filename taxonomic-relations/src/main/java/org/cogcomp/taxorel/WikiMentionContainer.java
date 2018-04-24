package org.cogcomp.taxorel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuany on 2/23/2018.
 */
public class WikiMentionContainer implements Serializable{
    public List<WikiMention> mentions;
    public WikiMentionContainer(){
        mentions = new ArrayList<>();
    }
    public void addMention(WikiMention mention){
        mentions.add(mention);
    }
}
