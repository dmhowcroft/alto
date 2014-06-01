/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.util;

import de.up.ling.tree.Tree;
import java.util.List;

/**
 *
 * @author koller
 */
public class Util {

    public static Tree<String> makeBinaryTree(String symbol, List<String> leaves) {
        return makeBinaryTree(symbol, leaves, 0);
    }

    private static Tree<String> makeBinaryTree(String symbol, List<String> leaves, int pos) {
        int remaining = leaves.size() - pos;

        if (remaining == 1) {
            return Tree.create(leaves.get(pos));
        } else {
            Tree<String> left = Tree.create(leaves.get(pos));
            Tree<String> right = makeBinaryTree(symbol, leaves, pos + 1);
            return Tree.create(symbol, new Tree[]{left, right});
        }
    }

    public static String getFilenameExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        
        return extension;
    }
    
    public static String formatTime(long timeInNs) {
        if (timeInNs < 1000) {
            return timeInNs + " ns";
        } else if (timeInNs < 1000000) {
            return timeInNs / 1000 + " \u03bcs";
        } else if (timeInNs < 1000000000) {
            return timeInNs / 1000000 + " ms";
        } else {
            StringBuffer buf = new StringBuffer();

            if (timeInNs > 60000000000L) {
                buf.append(timeInNs / 60000000000L + "m ");
            }

            timeInNs %= 60000000000L;

            buf.append(String.format("%d.%03ds", timeInNs / 1000000000, (timeInNs % 1000000000) / 1000000));
            return buf.toString();
        }
    }
    
    public static String formatTimeSince(long start) {
        return formatTime(System.nanoTime()-start);
    }
}