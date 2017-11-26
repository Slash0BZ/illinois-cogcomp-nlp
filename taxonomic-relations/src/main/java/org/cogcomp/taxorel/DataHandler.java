/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
/**
 *
 */
package org.cogcomp.taxorel;

import java.io.*;
import java.util.ArrayList;

/**
 * @author dxquang May 22, 2009
 */
public class DataHandler {

    public static final int READ_ONLY_WIKI = 0;
    public static final int READ_ONLY_NONWIKI = 1;
    public static final int READ_ALL = 2;

    public static BufferedReader openReader(String fname) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fname), "UTF-8"));
            return reader;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean closeReader(BufferedReader reader) {
        try {
            reader.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String> readLines(String fileName) {
        BufferedReader reader = openReader(fileName);
        String line;
        ArrayList<String> content = new ArrayList<String>();
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                content.add(line);
            }

            reader.close();

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to read from file " + fileName);
            System.exit(1);
            return null;
        }
    }

    public static BufferedWriter openWriter(String fname){
        return openWriter(fname, false);
    }

    public static BufferedWriter openWriter(String fname, boolean append) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fname, append), "UTF-8"));
            return writer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean closeWriter(BufferedWriter writer) {
        try {
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void writeContent(String content, String outputFileName) {
        BufferedWriter writer = openWriter(outputFileName);
        try {
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to write to file " + outputFileName);
            System.exit(1);
        }
        closeWriter(writer);
    }


    public static ArrayList<Instance> readTrainingInstances(String inputFile,
                                                            int type) throws Exception {

        ArrayList<Instance> arrInstances = new ArrayList<Instance>();

        BufferedReader reader = openReader(inputFile);

        String line;

        while ((line = reader.readLine()) != null) {

            line = line.trim();

            String chunks[] = line.split("\\t+");

            if (chunks.length < 4)
                continue;

            Instance instance = new Instance(chunks[2], chunks[3]);
            instance.relation = Integer.parseInt(chunks[0]);
            instance.entityClass = chunks[1];

            if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

                if (chunks.length < 10)
                    continue;

                if (Double.parseDouble(chunks[4]) < 0.0)
                    continue;

                instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
                instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
                instance.ratio_CatCat = Double.parseDouble(chunks[6]);

                instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

                instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
                instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
                instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
                instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

            }

            arrInstances.add(instance);

        }

        closeReader(reader);

        return arrInstances;
    }

    public static ArrayList<Instance> readTestingInstances(String inputFile,
                                                           int type, int ignoreUnknow) throws Exception {

        ArrayList<Instance> arrInstances = new ArrayList<Instance>();

        BufferedReader reader = openReader(inputFile);

        String line;

        while ((line = reader.readLine()) != null) {

            line = line.trim();

            String chunks[] = line.split("\\t+");

            for (int i = 0; i < chunks.length; i++) {
                chunks[i] = chunks[i].trim();
            }

            if (chunks.length < 4)
                continue;

            if (ignoreUnknow == READ_ONLY_WIKI) {
                if (Double.parseDouble(chunks[4]) < 0.0)
                    continue;
            } else if (ignoreUnknow == READ_ONLY_NONWIKI) {
                if (Double.parseDouble(chunks[4]) >= 0.0)
                    continue;
            }

            Instance instance = new Instance(chunks[2], chunks[3]);
            instance.relation = Integer.parseInt(chunks[0]);
            instance.entityClass = chunks[1];

            if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

                if (chunks.length < 10)
                    continue;

                instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
                instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
                instance.ratio_CatCat = Double.parseDouble(chunks[6]);

                instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

                instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
                instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
                instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
                instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

            }

            arrInstances.add(instance);

        }

        closeReader(reader);

        return arrInstances;
    }

    public static ArrayList<Instance> readExtendedTestingInstances(
            String inputFile, int type, int ignoreUnknow) throws Exception {

        ArrayList<Instance> arrInstances = new ArrayList<Instance>();

        BufferedReader reader = openReader(inputFile);

        String line;

        while ((line = reader.readLine()) != null) {

            line = line.trim();

            String chunks[] = line.split("\\t+");

            if (chunks.length < 4)
                continue;

            if (ignoreUnknow == READ_ONLY_WIKI) {
                if (Double.parseDouble(chunks[4]) < 0.0)
                    continue;
            } else if (ignoreUnknow == READ_ONLY_NONWIKI) {
                if (Double.parseDouble(chunks[4]) >= 0.0)
                    continue;
            }

            Instance instance = new Instance(chunks[2], chunks[3]);
            instance.relation = Integer.parseInt(chunks[0]);
            instance.entityClass = chunks[1];

            if (type == Constants.INPUT_TYPE_INTERMEDIATE) {

                if (chunks.length < 10)
                    continue;

                instance.ratio_TtlCat = Double.parseDouble(chunks[4]);
                instance.ratio_CatTtl = Double.parseDouble(chunks[5]);
                instance.ratio_CatCat = Double.parseDouble(chunks[6]);

                instance.scorePmi_E1E2 = Double.parseDouble(chunks[7]);

                instance.scoreCos_AbsAbs = Double.parseDouble(chunks[8]);
                instance.scoreCos_CatCat = Double.parseDouble(chunks[9]);
                instance.scoreCos_AbsCat = Double.parseDouble(chunks[10]);
                instance.scoreCos_CatAbs = Double.parseDouble(chunks[11]);

            }

            instance.textLine = line;

            arrInstances.add(instance);

        }

        closeReader(reader);

        return arrInstances;
    }



    public static ArrayList<String> makeStringInstances(
            ArrayList<Instance> arrInstances, int type) {

        ArrayList<String> arrStringInstances = new ArrayList<String>();

        for (Instance e : arrInstances) {

            // String className = (e.relation == Constants.ANCESTOR_E1_TO_E2 ||
            // e.relation == Constants.ANCESTOR_E2_TO_E1) ? "_"
            // : e.entityClass;

            String className = e.entityClass;

            String instance = null;
            if (type == Constants.INPUT_TYPE_GOLD)
                instance = e.relation + "\t" + className + "\t" + e.entity1
                        + "\t" + e.entity2 + "\n";
            else if (type == Constants.INPUT_TYPE_INTERMEDIATE) {
                instance = e.relation + "\t" + className + "\t" + e.entity1
                        + "\t" + e.entity2 + "\t" + e.ratio_TtlCat + "\t"
                        + e.ratio_CatTtl + "\t" + e.ratio_CatCat + "\t"
                        + e.scorePmi_E1E2 + "\t" + e.scoreCos_AbsAbs + "\t"
                        + e.scoreCos_CatCat + "\t" + e.scoreCos_AbsCat + "\t"
                        + e.scoreCos_CatAbs + "\n";
            } else {
                instance = e.relation + "\t" + className + "\t" + e.entity1
                        + "\t" + e.entity2 + "\t" + e.ratio_TtlCat + "\t"
                        + e.ratio_CatTtl + "\t" + e.ratio_CatCat + "\t"
                        + e.scorePmi_E1E2 + "\t" + e.scoreCos_AbsAbs + "\t"
                        + e.scoreCos_CatCat + "\t" + e.scoreCos_AbsCat + "\t"
                        + e.scoreCos_CatAbs + "\t" + e.finalScore + "\t"
                        + e.predictedRelation + "\n";
            }

            arrStringInstances.add(instance);

        }
        return arrStringInstances;
    }

    public static void writeLines(ArrayList<String> outputLines,
                                  String outputFile) {
        BufferedWriter writer = openWriter(outputFile, true);
        try {
            for (String line : outputLines) {
                writer.write(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to write to file " + outputFile);
            System.exit(1);
        }
        closeWriter(writer);
    }
}