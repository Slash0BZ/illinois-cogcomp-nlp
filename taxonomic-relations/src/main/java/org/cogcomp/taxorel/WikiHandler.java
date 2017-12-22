/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package org.cogcomp.taxorel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;
import java.util.*;

/**
 * Created by xuany on 11/12/2017.
 */
public class WikiHandler {

    public static List<String> getTitlesFromQuery(List<String> queries){
        List<String> titles = new ArrayList<>();
        JSONObject jsonObject = null;
        try {
            String request = "";
            for (String q : queries){
                request += q + " ";
            }
            if (request.endsWith(" ")){
                request = request.substring(0, request.length() - 1);
            }
            request = URLEncoder.encode(request, "UTF-8");
            URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srlimit=max&srsearch=" + request + "&format=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output = br.readLine();
            jsonObject = new JSONObject(output);
            conn.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }
        if (jsonObject.getJSONObject("query").getJSONObject("searchinfo").has("suggestion")){
            String suggestion = (String)jsonObject.getJSONObject("query").getJSONObject("searchinfo").get("suggestion");
            List<String> newQuery = new ArrayList<>();
            newQuery.add(suggestion);
            return getTitlesFromQuery(newQuery);
        }
        JSONArray results = jsonObject.getJSONObject("query").getJSONArray("search");
        for (int i = 0; i < results.length(); i++){
            titles.add((String)results.getJSONObject(i).get("title"));
        }
        return titles;
    }

    public static int getTotalHits(String query){
        JSONObject jsonObject = null;
        try {
            String request = URLEncoder.encode(query, "UTF-8");
            URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + request + "&format=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output = br.readLine();
            jsonObject = new JSONObject(output);
            conn.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }
        return (Integer)jsonObject.getJSONObject("query").getJSONObject("searchinfo").get("totalhits");
    }

    public static String getContentByTitle(String title){
        String ret = "";
        JSONObject jsonObject = null;
        try {
            String request = URLEncoder.encode(title, "UTF-8");
            URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&prop=revisions&rvprop=content&format=json&titles=" + request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output = br.readLine();
            jsonObject = new JSONObject(output);
            conn.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }
        JSONObject result = jsonObject.getJSONObject("query").getJSONObject("pages");
        String key = (String)result.keySet().toArray()[0];
        if (key.equals("-1")){
            return ret;
        }
        if (!result.getJSONObject(key).has("revisions")){
            return ret;
        }
        JSONArray results = result.getJSONObject(key).getJSONArray("revisions");
        for (int i = 0; i < results.length(); i++){
            ret += (String)results.getJSONObject(i).get("*");
        }
        return ret;
    }

    public static List<String> getParentCategory(String category, Connection conn){
        List<String> ret = new ArrayList<>();
        String normalized = category.replace(" ", "_");
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT CONVERT(super_cats USING utf8) FROM category WHERE cat_title=?");
            statement.setString(1, normalized);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                String combined = (String)rs.getObject(1);
                String[] subcats = combined.split("\\|\\|");
                for (String subcat : subcats){
                    if (subcat.length() > 0){
                        ret.add(subcat);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if (ret.size() > 0){
            return ret;
        }
        String request = "Category:" + category;
        return getInfoFromTitle(request).categories;
    }

    public static List<String> getParentCategoryViaMapDB(String category, HTreeMap<String, String> map){
        List<String> ret = new ArrayList<>();
        String normalized = category.replace(" ", "_");
        String combined = map.get(normalized);
        if (combined == null){
            String request = "Category:" + category;
            return getInfoFromTitle(request).categories;
        }
        String[] subcats = combined.split("\\|\\|");
        for (String subcat : subcats){
            if (subcat.length() > 0){
                ret.add(subcat);
            }
        }
        if (ret.size() > 0){
            return ret;
        }
        String request = "Category:" + category;
        return getInfoFromTitle(request).categories;
    }

    public static List<String> getDaughtersCategory(String category){
        JSONObject jsonObject = null;
        try {
            String request = URLEncoder.encode("Category:" + category, "UTF-8");
            URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&titles=" + request + "&prop=categories&format=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output = br.readLine();
            jsonObject = new JSONObject(output);
            conn.disconnect();

        } catch (Exception e){
            e.printStackTrace();
        }
        JSONObject result = jsonObject.getJSONObject("query").getJSONObject("pages");
        String key = (String)result.keySet().toArray()[0];
        List<String> ret = new ArrayList<>();
        if (key.equals("-1")){
            return ret;
        }
        if (!result.getJSONObject(key).has("categories")){
            return ret;
        }
        JSONArray results = result.getJSONObject(key).getJSONArray("categories");
        for (int i = 0; i < result.length(); i++){
            String raw = (String)results.getJSONObject(i).get("title");
            ret.add(raw.substring(9));
        }
        return ret;
    }

    public static boolean existsEntry(String query){
        if (getTotalHits(query) > 0){
            return true;
        }
        return false;
    }

    public static ArticleQueryResult getInfoFromTitle(String title){
        ArticleQueryResult ret = new ArticleQueryResult(title);
        JSONObject jsonObject = null;
        try {
            String request = URLEncoder.encode(title, "UTF-8");
            URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&titles=" + request + "&prop=categories|extracts&cllimit=max&clshow=!hidden&exintro=true&explaintext=true&format=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output = br.readLine();
            jsonObject = new JSONObject(output);
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject result = jsonObject.getJSONObject("query").getJSONObject("pages");
        String key = (String)result.keySet().toArray()[0];
        if (result.getJSONObject(key).has("categories")) {
            JSONArray cats = result.getJSONObject(key).getJSONArray("categories");
            for (int j = 0; j < cats.length(); j++) {
                String rawCat = (String) cats.getJSONObject(j).get("title");
                if (rawCat.startsWith("Category:")) {
                    rawCat = rawCat.substring(9);
                }
                ret.categories.add(rawCat);
            }
        }
        if (!result.getJSONObject(key).has("extract")){
            ret.extract = "";
            return ret;
        }
        ret.extract = (String)result.getJSONObject(key).get("extract");
        return ret;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return -(o1.getValue()).compareTo( o2.getValue() );
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void exportToMapDB(){
        DB db = DBMaker.fileDB("data/FIGER/category.db").closeOnJvmShutdown().fileMmapEnableIfSupported().make();
        HTreeMap<String, String> map = db.hashMap("category").keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).create();
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/wiki", "root", "zhouxy960914");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CONVERT(category.cat_title USING utf8), CONVERT (category.super_cats USING utf8) FROM category");
            while (rs.next()){
                String cat = (String)rs.getObject(1);
                String superCat = (String)rs.getObject(2);
                map.put(cat, superCat);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        db.commit();
        db.close();
    }
}
