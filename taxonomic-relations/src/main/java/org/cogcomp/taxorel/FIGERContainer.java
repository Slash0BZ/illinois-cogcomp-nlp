package org.cogcomp.taxorel;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.*;
import java.util.*;

public class FIGERContainer implements Serializable {
    public List<FIGERInstance> _instances;
    public FIGERContainer(){
        _instances = new ArrayList<>();
    }
    public void addExample(FIGERInstance instance){
        _instances.add(instance);
    }

    public static void saveToFile(FIGERContainer container, String fileName){
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(container);
            oos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static FIGERContainer readFromFile(String fileName){
        FIGERContainer ret = null;
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            ret = (FIGERContainer) ois.readObject();
            ois.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static List<String> S2L(String input){
        List<String> ret = new ArrayList<>();
        for (String s: input.split("\\|\\|\\|")){
            ret.add(s);
        }
        return ret;
    }

    public static void main(String[] args){
        File file = new File("data/FIGER/types.map");
        Map<String, String> typeMap = new HashMap<>();
        DB db = DBMaker.fileDB("data/FIGER/entity2type.db").closeOnJvmShutdown().fileMmapEnableIfSupported().make();
        HTreeMap<String, String> entity2title = db.hashMap("entity2title", Serializer.STRING, Serializer.STRING).createOrOpen();
        HTreeMap<String, String> entity2type = db.hashMap("entity2type", Serializer.STRING, Serializer.STRING).createOrOpen();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineGroup = line.split("\t");
                typeMap.put(lineGroup[0], lineGroup[1]);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        MentionReader mentionReader = MentionReader.getMentionReader("data/FIGER/train.data.gz");
        EntityProtos.Mention m;
        int count = 0;
        FIGERContainer container = new FIGERContainer();
        Set<String> seen = new HashSet<>();
        while ((m = mentionReader.readMention()) != null) {
            if (seen.contains(m.getEntityName())){
                continue;
            }
            seen.add(m.getEntityName());
            count ++;
            if (count % 10000 == 0){
                System.out.println("Processed: " + count);
            }
            Set<String> typeSet = new HashSet<>();
            for (String type : m.getLabelsList()) {
                if (typeMap.containsKey(type)) {
                    typeSet.add(typeMap.get(type));
                }
            }
            if (typeSet.size() == 0){
                continue;
            }
            if (entity2type.containsKey(m.getEntityName())){
                FIGERInstance figerInstance = new FIGERInstance(S2L(entity2type.get(m.getEntityName())), new ArrayList<>(typeSet));
                container.addExample(figerInstance);
            }
        }
        saveToFile(container, "data/FIGER/FIGERContainer_unique.cache");
    }
}
