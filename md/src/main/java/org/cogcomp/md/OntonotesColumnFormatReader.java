/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.md;

import edu.illinois.cs.cogcomp.annotation.BasicTextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.AnnotationReader;
import edu.illinois.cs.cogcomp.nlp.corpusreaders.CorpusReaderConfigurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a reader similar to ACEReader
 * It reads column formatted files and returns TextAnnotations.
 * @Issue: It does not obey the token index defined in the columnformat file,
 *          The first token is assigned index 0, rather than the index in the file.
 */
public class OntonotesColumnFormatReader extends AnnotationReader<TextAnnotation> {
    String _path;
    List<String> _filePaths;
    List<TextAnnotation> _tas;
    private int _tas_idx;
    private int _t_mentions;
    public OntonotesColumnFormatReader(String path){
        super(CorpusReaderConfigurator.buildResourceManager(path));
        _path = path;
        _t_mentions = 0;
        fillPaths(path);
        readTextAnnotations();
    }
    public void initializeReader(){
        _path = "INVALID";
        _filePaths = new ArrayList<>();
        _tas = new ArrayList<>();
        _tas_idx = 0;
    }

    public void readTextAnnotations(){
        for (String s : _filePaths){
            _tas.add(readSingleFile(s));
        }
    }
    public void fillPaths(String dirpath) {
        File dir = new File(dirpath);
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    fillPaths(file.getCanonicalPath());
                } else {
                    _filePaths.add(file.getCanonicalPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public TextAnnotation readSingleFile(String file){
        List<String[]> tokens = new ArrayList<>();
        List<Pair<Integer, Integer>> mentions = new ArrayList<>();
        List<String> mentionTypes = new ArrayList<>();
        List<String> curSentence = new ArrayList<>();
        List<Integer> curMention = new ArrayList<>();
        int tokenIdx = 0;
        BufferedReader br = null;
        TextAnnotation ta = null;
        boolean contSig = false;
        try {
            br = new BufferedReader(new FileReader(file));
            String rawLine;
            Map<String, Integer> firstMeetMap = new HashMap<>();
            while ((rawLine = br.readLine()) != null) {
                if (rawLine.length() < 6){
                    contSig = true;
                    continue;
                }
                if (contSig){
                    String[] curSentenceArr = new String[curSentence.size()];
                    curSentenceArr = curSentence.toArray(curSentenceArr);
                    tokens.add(curSentenceArr);
                    curSentence = new ArrayList<>();
                    contSig = false;
                }
                String[] line = rawLine.split("\\s+");
                if (line.length < 6) {
                    continue;
                }
                String word = line[3];

                String corefNote = line[line.length - 1];
                for (int i = 0; i < corefNote.length(); i++) {
                    if (corefNote.charAt(i) == '(') {
                        String start = "";
                        for (int j = i + 1; j < corefNote.length(); j++) {
                            if (!Character.isDigit(corefNote.charAt(j))) {
                                break;
                            }
                            start += corefNote.charAt(j);
                        }
                        firstMeetMap.put(start, tokenIdx);
                    }
                    if (corefNote.charAt(i) == ')') {
                        String end = "";
                        for (int j = i - 1; j >= 0; j--) {
                            if (!Character.isDigit(corefNote.charAt(j))) {
                                break;
                            }
                            end = corefNote.charAt(j) + end;
                        }
                        mentions.add(new Pair<>(firstMeetMap.get(end), tokenIdx + 1));
                    }
                }
                curSentence.add(word);

                tokenIdx++;
            }
            if (curSentence.size() > 0) {
                String[] curSentenceArr = new String[curSentence.size()];
                curSentenceArr = curSentence.toArray(curSentenceArr);
                tokens.add(curSentenceArr);
            }
            ta = BasicTextAnnotationBuilder.createTextAnnotationFromTokens(tokens);

            SpanLabelView mentionView = new SpanLabelView("MENTIONS", this.getClass().getCanonicalName(), ta, 1.0f);
            for (int i = 0; i < mentions.size(); i++) {
                Pair<Integer, Integer> curBound = mentions.get(i);
                Constituent constituent = new Constituent("MENTION", 1.0f, "MENTIONS", ta, curBound.getFirst(), curBound.getSecond());
                mentionView.addConstituent(constituent);
            }
            ta.addView("MENTIONS", mentionView);
            br.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ta;
    }
    public boolean hasNext(){
        return _tas_idx < _tas.size();
    }
    public TextAnnotation next(){
        if (_tas_idx == _tas.size()){
            return null;
        }
        else{
            _tas_idx ++;
            return _tas.get(_tas_idx - 1);
        }
    }
    public String generateReport(){
        return null;
    }
}
