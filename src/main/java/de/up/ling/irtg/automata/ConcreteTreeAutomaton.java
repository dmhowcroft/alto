/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.automata;

import com.google.common.base.Function;
import de.up.ling.irtg.signature.Signature;
import de.up.ling.irtg.signature.SignatureMapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author koller
 */
public class ConcreteTreeAutomaton<State> extends TreeAutomaton<State> {

//    private IntTrie<Int2ObjectMap<Set<Rule>>> ruleTrie = null;

    public ConcreteTreeAutomaton() {
        super(new Signature());
        isExplicit = true;
//        ruleTrie = new IntTrie<Int2ObjectMap<Set<Rule>>>();
    }

    @Override
    public int addState(State state) {
        return super.addState(state);
    }

    @Override
    public void addFinalState(int state) {
        super.addFinalState(state);
    }

    public void addRule(Rule rule) {
        storeRule(rule);
    }

    @Override
    public Set<Rule> getRulesBottomUp(int label, int[] childStates) {
        return getRulesBottomUpFromExplicit(label, childStates);
    }

    @Override
    public Iterable<Rule> getRulesTopDown(int label, int parentState) {
        return getRulesTopDownFromExplicit(label, parentState);
    }

    @Override
    public boolean isBottomUpDeterministic() {
        return explicitIsBottomUpDeterministic;
    }

    @Override
    public void foreachRuleBottomUpForSets(final IntSet labelIds, List<IntSet> childStateSets, final SignatureMapper signatureMapper, final Function<Rule, Void> fn) {
        explicitRulesBottomUp.foreachValueForKeySets(childStateSets, new Function<Int2ObjectMap<Set<Rule>>, Void>() {
            public Void apply(Int2ObjectMap<Set<Rule>> ruleMap) {
                for (int label : ruleMap.keySet()) {
                    if( labelIds.contains(signatureMapper.remapForward(label))) {
                        for (Rule rule : ruleMap.get(label)) {
                            fn.apply(rule);
                        }
                    }
                }

                return null;
            }
        });
    }
}
