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

/**
 * @author dxquang
 * Oct 13, 2009
 */
public class InferenceOutput {

    public String key;
    public double value;

    /**
     *
     */
    public InferenceOutput(String key, double value) {
        this.key = key;
        this.value = value;
    }
}