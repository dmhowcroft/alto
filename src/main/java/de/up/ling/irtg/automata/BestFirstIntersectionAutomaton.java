/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.up.ling.irtg.automata;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import de.saar.basic.CartesianIterator;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author koller
 */
public class BestFirstIntersectionAutomaton <LeftState, RightState> extends IntersectionAutomaton<LeftState, RightState> {
    private static final boolean DEBUG = false;
    private final EdgeEvaluator evaluator;
    
    public BestFirstIntersectionAutomaton(TreeAutomaton<LeftState> left, TreeAutomaton<RightState> right, EdgeEvaluator evaluator) {
        super(left, right);
        this.evaluator = evaluator;
    }
    
    // TODO - this is super inefficient and should be improved!
    // better to assign double priorities to elements of the queue directly
    private class EdgeEvaluatingIntComparator implements IntComparator {
        @Override
        public int compare(int outstate1, int outstate2) {
            double eval1 = evaluator.evaluate(outstate1, BestFirstIntersectionAutomaton.this);
            double eval2 = evaluator.evaluate(outstate2, BestFirstIntersectionAutomaton.this);
            return Double.compare(eval1, eval2);
        }

        @Override
        @SuppressWarnings("UnnecessaryUnboxing")
        public int compare(Integer o1, Integer o2) {
            return compare(o1.intValue(), o2.intValue());
        }
        
    }
    
    /**
     * Intersects the two automata bottom-up. The intersection algorithm uses a priority queue
     * for its agenda, on which edges (i.e., states of the output automaton) are
     * ordered in descending order with respect to the edge evaluator that was passed 
     * to the constructor. The intersection algorithm terminates once it discovers
     * the first final state of the output automaton. At this point, it will typically
     * NOT have computed the entire output automaton; the only guarantee is that
     * it has discovered enough of the automaton to generate at least one tree
     * from the first final state. If the edge evaluator is an admissible heuristic
     * in the sense of A* search, this tree is also guaranteed to be optimal.
     * 
     */
    @Override
    public void makeAllRulesExplicit() {
        if (!isExplicit) {
            isExplicit = true;

            getStateInterner().setTrustingMode(true);

            ListMultimap<Integer, Rule> rulesByChildState = left.getRuleByChildStateMap();  // int = left state ID
            IntPriorityQueue agenda = new IntHeapPriorityQueue(new EdgeEvaluatingIntComparator());
            IntSet seenStates = new IntOpenHashSet();
            
            SetMultimap<Integer, Integer> partners = HashMultimap.create(); // left state ID -> right state IDs

            // initialize agenda with all pairs of rules of the form A -> f
            int[] noRightChildren = new int[0];

            for (Rule leftRule : left.getRuleSet()) {
                if (leftRule.getArity() == 0) {
                    Iterable<Rule> preterminalRulesForLabel = right.getRulesBottomUp(remapLabel(leftRule.getLabel()), noRightChildren);

                    for (Rule rightRule : preterminalRulesForLabel) {
                        Rule rule = combineRules(leftRule, rightRule);
                        storeRule(rule);
                        evaluator.ruleAdded(rule);
                        
                        agenda.enqueue(rule.getParent());
                        seenStates.add(rule.getParent());
                        partners.put(leftRule.getParent(), rightRule.getParent());
                    }
                }
            }
            
            // compute rules and states bottom-up
            long unsuccessful = 0;
            long iterations = 0;
            
            agendaLoop:
            while (!agenda.isEmpty()) {                
                int state = agenda.dequeueInt();
                
                if( DEBUG ) {
                    System.err.println("dequeued: " + ppstate(state));
                }
                
                List<Rule> possibleRules = rulesByChildState.get(stateToLeftState.get(state));

                for (Rule leftRule : possibleRules) {
                    List<Set<Integer>> partnerStates = new ArrayList<Set<Integer>>();
                    
                    for (int leftState : leftRule.getChildren()) {
                        partnerStates.add(partners.get(leftState));
                    }

                    CartesianIterator<Integer> it = new CartesianIterator<Integer>(partnerStates); // int = right state ID
                    List<Integer> newStates = new ArrayList<Integer>();
                    
                    while (it.hasNext()) {
                        iterations++;

                        List<Integer> partnersHere = it.next();
                        Iterable<Rule> rightRules = right.getRulesBottomUp(remapLabel(leftRule.getLabel()), partnersHere);

                        if (!rightRules.iterator().hasNext()) {
                            unsuccessful++;
                        }

                        for (Rule rightRule : rightRules) {
                            Rule rule = combineRules(leftRule, rightRule);
                            storeRule(rule);
                            evaluator.ruleAdded(rule);
                            
                            if( left.getFinalStates().contains(leftRule.getParent())
                                    && right.getFinalStates().contains(rightRule.getParent())) {
                                // discovered rule whose parent is a final state in output automaton
                                // => stop computation here
                                break agendaLoop;
                            }

                            if (seenStates.add(rule.getParent())) {
                                newStates.add(rule.getParent());
                            }
                        }
                    }
                    
                    for (int newState : newStates) {
                        agenda.enqueue(newState);
                        partners.put(stateToLeftState.get(newState), stateToRightState.get(newState));
                    }
                }
            }

            // force recomputation of final states: if we printed any rule within the
            // intersection algorithm (for debugging purposes), then finalStates will have
            // a value at this point, which is based on an incomplete set of rules and
            // therefore wrong
            finalStates = null;

            if (DEBUG) {
                System.err.println(iterations + " iterations, " + unsuccessful + " unsucc");
            }

            getStateInterner().setTrustingMode(false);
        }
    }
}
