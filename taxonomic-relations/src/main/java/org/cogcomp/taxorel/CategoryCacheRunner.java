package org.cogcomp.taxorel;

/**
 * Created by xuany on 12/1/2017.
 */
public class CategoryCacheRunner {
    public static void main(String[] args){
        Thread object_xaa = new Thread(new CategoryCache("data/xaa"));
        Thread object_xab = new Thread(new CategoryCache("data/xab"));
        Thread object_xac = new Thread(new CategoryCache("data/xac"));
        Thread object_xad = new Thread(new CategoryCache("data/xad"));
        Thread object_xae = new Thread(new CategoryCache("data/xae"));
        Thread object_xaf = new Thread(new CategoryCache("data/xaf"));

        object_xaa.start();
        object_xab.start();
        object_xac.start();
        object_xad.start();
        object_xae.start();
        object_xaf.start();
    }
}
