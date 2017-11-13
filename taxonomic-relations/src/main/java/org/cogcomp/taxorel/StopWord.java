/**
 *
 */
package org.cogcomp.taxorel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dxquang
 * Apr 21, 2009
 */
public class StopWord {

    public static final String STOP_WORD_STRING = "i, me, my, myself, we, our, ours, ourselves, you, your, yours, yourself, yourselves, he, him, his, himself, she, her, hers, herself, it, its, itself, they, them, their, theirs, themselves, what, which, who, whom, this, that, these, those, am, is, are, was, were, be, been, being, have, has, had, having, do, does, did, doing, would, should, could, ought, i'm, you're, he's, she's, it's, we're, they're, i've, you've, we've, they've, i'd, you'd, he'd, she'd, we'd, they'd, i'll, you'll, he'll, she'll, we'll, they'll, isn't, aren't, wasn't, weren't, hasn't, haven't, hadn't, doesn't, don't, didn't, won't, wouldn't, shan't, shouldn't, can't, cannot, couldn't, mustn't, let's, that's, who's, what's, here's, there's, when's, where's, why's, how's, a, an, the, and, but, if, or, because, as, until, while, of, at, by, for, with, about, against, between, into, through, during, before, after, above, below, to, from, up, down, in, out, on, off, over, under, again, further, then, once, here, there, when, where, why, how, all, any, both, each, few, more, most, other, some, such, no, nor, not, only, own, same, so, than, too, very, one, every, least, less, many, now, ever, never, say, says, said, also, get, go, goes, just, made, make, put, see, seen, whether, like, well, back, even, still, way, take, since, another, however, two, three, four, five, first, second, new, old, high, long, m, re, s, ve, d, ll, isn, aren, wasn, weren, hasn, hadn, doesn, don, didn, wouldn, shan, shouldn, couldn, mustn, t";

    protected Set<String> setStopWords;

    protected boolean useStemmer;

    /**
     *
     */
    public StopWord(boolean useStemmer) {
        this.useStemmer = useStemmer;
        setStopWords = parseStopWordString();
    }

    private Set<String> parseStopWordString() {
        Set<String> setSW = new HashSet<String>();
        String stopWordString = STOP_WORD_STRING;
        if (this.useStemmer == true) {
            Stemmer s = new Stemmer();
            stopWordString = s.stem(stopWordString);
        }

        String tokens[] = stopWordString.split(",+");
        for (String token : tokens) {
            token = token.trim();
            setSW.add(token);
        }
        return setSW;
    }

    public boolean isStopWord(String word, boolean useStemmer) {
        if (useStemmer == true) {
            Stemmer s = new Stemmer();
            word = s.stem(word);
        }
        else
            word = word.toLowerCase();

        word = word.trim();

        return setStopWords.contains(word);
    }

    public ArrayList<String> removeStopWords(String text) {

        text = formatString(text);

        if (useStemmer == true) {
            Stemmer s = new Stemmer();
            text = s.stem(text);
        }

        ArrayList<String> arrTokens = new ArrayList<String>();
        String tokens[] = text.split("\\s+");

        for (String token : tokens) {
            if (setStopWords.contains(token))
                continue;
            else
                arrTokens.add(token);
        }

        return arrTokens;

    }

    private String formatString(String text) {

        text = text.toLowerCase();
        text = text.replaceAll("\\p{Punct}", " ");
        text = text.replaceAll("\\s\\s+", " ");
        text = text.trim();

        return text;
    }


}