/**
 *
 */
package org.cogcomp.taxorel;

import java.util.ArrayList;

/**
 * @author dxquang Apr 27, 2009
 */
public class Instance {

    public int relation; // (0: None, 1: ANCESTOR_E1_E2, 2: ANCESTOR_E2_E1, 3: COUSIN)

    public String entityClass;

    public String entity1;
    public String entity2;

    public double ratio_TtlCat;
    public double ratio_CatTtl;
    public double ratio_CatCat;

    public double scorePmi_E1E2;

    public double scoreCos_AbsAbs;
    public double scoreCos_CatCat;
    public double scoreCos_AbsCat;
    public double scoreCos_CatAbs;

    public double finalScore;

    public double testXY;
    public double testYX;
    public double testZZ;
    public double testNO;

    public int predictedRelation; // (0: None, 1: ANCESTOR_E1_E2, 2:
    // ANCESTOR_E2_E1, 3: SIBLING)

    public String textLine;

    // This was added for ACL10;
    public int[] additionalFeatures1 = new int[3]; // ["born"?, year?, many
    // years?]
    public int[] additionalFeatures2 = new int[3]; // ["born"?, year?, many
    // years?]

    // ANCESTOR_E2_E1, 3: COUSIN)

    /**
     *
     */
    public Instance(String entity1, String entity2) {

        /*
        if (RelationIdentification.mapClassMapping.containsKey(entity1))
            entity1 = RelationIdentification.mapClassMapping.get(entity1);

        if (RelationIdentification.mapClassMapping.containsKey(entity2))
            entity2 = RelationIdentification.mapClassMapping.get(entity2);
            */

        this.entity1 = entity1;
        this.entity2 = entity2;

        this.entityClass = "";

        this.relation = Constants.NONE;

        this.scoreCos_AbsCat = 0;
        this.scoreCos_CatAbs = 0;

        this.scorePmi_E1E2 = 0;

        this.scoreCos_CatCat = 0;
        this.scoreCos_AbsAbs = 0;

        this.ratio_TtlCat = 0;
        this.ratio_CatTtl = 0;

        this.ratio_CatCat = 0;

        this.additionalFeatures1[0] = 0;
        this.additionalFeatures1[1] = 0;
        this.additionalFeatures1[2] = 0;

        this.additionalFeatures2[0] = 0;
        this.additionalFeatures2[1] = 0;
        this.additionalFeatures2[2] = 0;
    }

    /**
     *
     */
    public Instance(Instance instance) {

        this.entity1 = instance.entity1;
        this.entity2 = instance.entity2;

        this.relation = instance.relation;

        this.entityClass = instance.entityClass;

        this.scoreCos_AbsCat = instance.scoreCos_AbsCat;
        this.scoreCos_CatAbs = instance.scoreCos_CatAbs;

        this.scorePmi_E1E2 = instance.scorePmi_E1E2;

        this.scoreCos_CatCat = instance.scoreCos_CatCat;
        this.scoreCos_AbsAbs = instance.scoreCos_AbsAbs;

        this.ratio_TtlCat = instance.ratio_TtlCat;
        this.ratio_CatTtl = instance.ratio_CatTtl;

        this.ratio_CatCat = instance.ratio_CatCat;

        this.textLine = instance.textLine;

        this.additionalFeatures1[0] = instance.additionalFeatures1[0];
        this.additionalFeatures1[1] = instance.additionalFeatures1[1];
        this.additionalFeatures1[2] = instance.additionalFeatures1[2];
        this.additionalFeatures2[0] = instance.additionalFeatures2[0];
        this.additionalFeatures2[1] = instance.additionalFeatures2[1];
        this.additionalFeatures2[2] = instance.additionalFeatures2[2];
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        String res = this.relation + "\t" + this.entityClass + "\t"
                + this.entity1 + "\t" + this.entity2 + "\t" + this.ratio_TtlCat
                + "\t" + this.ratio_CatTtl + "\t" + this.ratio_CatCat + "\t"
                + this.additionalFeatures1[0] + "\t"
                + this.additionalFeatures1[1] + "\t"
                + this.additionalFeatures1[2] + "\t"
                + this.additionalFeatures2[0] + "\t"
                + this.additionalFeatures2[1] + "\t"
                + this.additionalFeatures2[2];
        return res;

    }

    public static void printArrayInstances(ArrayList<Instance> arrInstances) {

        for (Instance ins : arrInstances) {
            System.out.println(ins);
        }

    }

    public static void printInstanceScores(ArrayList<Instance> arrInstances) {

        for (Instance ins : arrInstances) {
            String res = ins.finalScore + "\t" + ins.relation + "\t"
                    + ins.entityClass + "\t" + ins.entity1 + "\t" + ins.entity2;
            System.out.println(res);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
            return true;
        if (!(other instanceof Instance))
            return false;
        Instance otherA = (Instance) other;
        return (entity1.equals(otherA.entity1))
                && ((entity2 == null) ? otherA.entity2 == null : entity2
                .equals(otherA.entity2));

    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + entity1.hashCode();
        hash = hash * 31 + (entity2 == null ? 0 : entity2.hashCode());
        return hash;
    }
}