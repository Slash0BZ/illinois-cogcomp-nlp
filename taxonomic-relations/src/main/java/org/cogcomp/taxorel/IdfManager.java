/**
 *
 */
package org.cogcomp.taxorel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dxquang
 * Apr 16, 2009
 */
public class IdfManager {

    Map<String, Double> mapIdf = new HashMap<String, Double>();

    public static String IDF_FILE = "data/jupiter/idf_unnormalized.txt.uniqued";

    /**
     *
     */
    public IdfManager(String idfFile) {

        System.out.println("Loading idf data.");
        readIdf(idfFile);

    }

    /**
     *
     */
    public IdfManager() {
        System.out.println("\nLoading idf data.");
        readIdf(IDF_FILE);
        System.out.println("Done.");

    }

    private void readIdf(String idfFile) {

        ArrayList<String> arrLines = DataHandler.readLines(idfFile);

        for (String line : arrLines) {

            line = line.trim();
            String items[] = line.split("\\t");

            if (items.length != 3)
                continue;

            mapIdf.put(items[1], Double.parseDouble(items[2]));

        }

    }

    public boolean containTerm(String term) {
        if (mapIdf.containsKey(term))
            return true;
        return false;
    }


    public double getIdf(String term) {
        if (containTerm(term))
            return mapIdf.get(term);
        return 0;
    }
}