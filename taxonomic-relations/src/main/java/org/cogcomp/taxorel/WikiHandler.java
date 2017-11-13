package org.cogcomp.taxorel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
                request += q + " AND ";
            }
            if (request.endsWith(" AND ")){
                request = request.substring(0, request.length() - 5);
            }
            request = URLEncoder.encode(request, "UTF-8");
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

    public static List<String> getParentCategory(String category){
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
        JSONArray cats = result.getJSONObject(key).getJSONArray("categories");
        for (int j = 0; j < cats.length(); j++){
            String rawCat = (String)cats.getJSONObject(j).get("title");
            if (rawCat.startsWith("Category:")){
                rawCat = rawCat.substring(9);
            }
            ret.categories.add(rawCat);
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
}
