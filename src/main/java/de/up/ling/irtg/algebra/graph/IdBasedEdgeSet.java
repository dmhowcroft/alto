/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.algebra.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.BitSet;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * An interface for in-boundary edge sets based on their integer ID.
 * There are implementations using Bytes and shorts instead of integers in an effort to save memory.
 * @author groschwitz
 */
interface IdBasedEdgeSet {
    
    /**
     * Adds the edge.
     * @param edge
     */
    void add(int edge);

    /**
     * Returns true iff this set contains the given edge.
     * @param edge
     * @return
     */
    boolean contains(int edge);

    /**
     * Returns true iff this set and the other set are disjoint. 
     * @param other
     * @return
     */
    boolean disjunt(IdBasedEdgeSet other);

    /**
     * Adds all edges of other to this set.
     * @param other
     */
    void addAll(IdBasedEdgeSet other);

    /**
     * Adds the given edge to this set.
     * @param edge
     * @param graphInfo
     */
    void add(GraphEdge edge, GraphInfo graphInfo);

    /**
     * Returns true iff this set contains the given edge
     * @param edge
     * @param graphInfo
     * @return
     */
    boolean contains(GraphEdge edge, GraphInfo graphInfo);

    /**
     * Returns true iff this set contains all the edges in the array
     * @param other
     * @return
     */
    boolean containsAll(int[] other);

    
    /**
     * deals with the consequences for the edges, if source is forgotton at vNr, assuming this is the last source at this node.
     * Needs a different copy of the IdBasedEdgeSet before the forget as reference.
     * returns the corresponding summand for the EdgeID of the BoundaryRepresentation.
     * @param vNr
     * @param source
     * @param reference
     * @param br
     * @param graphInfo
     * @return
     */
    IntList smartForgetIncident(int vNr, int source, IdBasedEdgeSet reference, BoundaryRepresentation br, GraphInfo graphInfo);
    
    
    /**
     * Clones the set.
     * @return 
     */
    IdBasedEdgeSet clone();
    
    /**
     * appends all edge names to the stringBuilder
     * @param result
     * @param graphInfo
     */
    void appendAll(StringBuilder result, GraphInfo graphInfo);


    /**
     * returns all the bitsets of the maps image whose key is an edge in this set.
     * @param map
     * @return 
     */
    List<BitSet> getCorrespondingBitSets(Int2ObjectMap<BitSet> map);
    
    /**
     * returns the number of edges in this set.
     * @return
     */
    int size();
   
    /**
     * returns the first edge in this set (no particular order, but the same while the set is unchanged).
     * @return
     */
    int getFirst();
    
    /**
     * returns true iff no edge is in this set.
     * @return
     */
    boolean isEmpty();
    
    /**
     * applies the action to all edges in this set.
     * @param action
     */
    void forEach(IntConsumer action);
    
    
}