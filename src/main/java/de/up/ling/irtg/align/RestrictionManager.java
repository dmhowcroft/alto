/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.align;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import de.up.ling.irtg.automata.ConcreteTreeAutomaton;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.automata.condensed.ConcreteCondensedTreeAutomaton;
import de.up.ling.irtg.automata.condensed.CondensedTreeAutomaton;
import de.up.ling.irtg.hom.HomomorphismSymbol;
import de.up.ling.irtg.signature.Signature;
import de.up.ling.tree.Tree;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

/**
 *
 * @author christoph_teichmann
 */
public class RestrictionManager {
    /**
     * Ensures that there is no sequence of variable nodes and that we never start with a variable.
     */
    private final ConcreteTreeAutomaton<Boolean> variableSequenceing;
    
    /**
     * Makes sure that sequences of symbols that are only productive on one side are ordered
     * to have first the left productive and then the right productive ones.
     */
    private final ConcreteTreeAutomaton<Boolean> ordering;
    
    /**
     * Ensures that once we have produced a constant on one side, the only symbol
     * we accept, that is productive on that side, is the terminator.
     */
    private final ConcreteTreeAutomaton<Belnapian> termination;
    
    /**
     * Ensures that whenever we see a two sided split symbol, then there are at least
     * two children that have a variable.
     */
    private final ConcreteTreeAutomaton<Boolean> splitAtLeastTwo;
    
    /**
     * Ensures that whenever we have a symbol with children, then all the children that are
     * paired have a variable.
     */
    private final ConcreteTreeAutomaton<Boolean> splitOrderedPairing;
    
    /**
     * 
     */
    private CondensedTreeAutomaton fullRestriction = null;

    /**
     * 
     */
    private final IntSet seenLeft = new IntOpenHashSet();

    /**
     * 
     */
    private final IntSet seenRight = new IntOpenHashSet();
    
    /**
     * 
     */
    private final List children = new ObjectArrayList();
    
    /**
     * 
     */
    private final Signature sig;
    
    /**
     * 
     * @param sig
     */
    public RestrictionManager(Signature sig){
        this.sig = sig;
        
        this.ordering = new ConcreteTreeAutomaton<>(this.sig);
        this.ordering.addFinalState(this.ordering.addState(Boolean.FALSE));
        
        this.splitAtLeastTwo = new ConcreteTreeAutomaton<>(this.sig);
        this.splitAtLeastTwo.addFinalState(this.splitAtLeastTwo.addState(Boolean.FALSE));
        
        this.splitOrderedPairing = new ConcreteTreeAutomaton<>(this.sig);
        this.splitOrderedPairing.addFinalState(this.splitOrderedPairing.addState(Boolean.FALSE));
        
        this.termination = new ConcreteTreeAutomaton<>(this.sig);
        this.termination.addFinalState(this.termination.addState(Belnapian.BOTH_FALSE));
        
        this.variableSequenceing = new ConcreteTreeAutomaton<>(this.sig);
        this.variableSequenceing.addFinalState(this.variableSequenceing.addState(Boolean.FALSE));
    }

    /**
     * 
     * @return 
     */
    public ConcreteTreeAutomaton<Boolean> getVariableSequenceing() {
        return variableSequenceing;
    }

    /**
     * 
     * @return 
     */
    public ConcreteTreeAutomaton<Boolean> getOrdering() {
        return ordering;
    }

    /**
     * 
     * @return 
     */
    public ConcreteTreeAutomaton<Belnapian> getTermination() {
        return termination;
    }

    /**
     * 
     * @return 
     */
    public ConcreteTreeAutomaton<Boolean> getSplitAtLeastTwo() {
        return splitAtLeastTwo;
    }

    /**
     * 
     * @return 
     */
    public ConcreteTreeAutomaton<Boolean> getSplitOrderedPairing() {
        return splitOrderedPairing;
    }
    
    /**
     * 
     * @return 
     */
    public CondensedTreeAutomaton getRestriction(){
        if(fullRestriction != null){
            return fullRestriction;
        }else{
            TreeAutomaton cta = this.variableSequenceing.intersect(ordering)
                    .intersect(this.termination).intersect(this.splitAtLeastTwo)
                    .intersect(this.splitOrderedPairing);
            return this.fullRestriction = ConcreteCondensedTreeAutomaton.fromTreeAutomaton(cta);
        }
    }
    
    /**
     * 
     * @param symbol
     * @param mapping1
     * @param mapping2 
     */
    public void addSymbol(String symbol, Tree<String> mapping1, Tree<String> mapping2){
        String label1 = mapping1.getLabel();

        if(HomomorphismManager.VARIABLE_PATTERN.test(label1)){
            this.handleVariable(symbol);
        }
        
        if(mapping1.getChildren().isEmpty() || mapping2.getChildren().isEmpty()){
            if(HomomorphismSymbol.isVariableSymbol(label1) ||
                    HomomorphismSymbol.isVariableSymbol(mapping2.getLabel())){
                handleSingular(symbol,mapping1,mapping2);
            }else{
                handleTermination(symbol);
            }
        }
        
        handlePair(symbol,mapping1,mapping2);
    }

    /**
     * 
     * @param symbol
     * @param mapping1
     * @param mapping2 
     */
    private void handleVariable(String symbol) {
        this.children.clear();
        this.children.add(Boolean.TRUE);
        this.variableSequenceing.addRule(this.variableSequenceing.createRule(Boolean.FALSE, symbol, children));
        
        this.children.clear();
        this.children.add(Boolean.FALSE);
        this.ordering.addRule(this.ordering.createRule(Boolean.TRUE, symbol, children));
        this.ordering.addRule(this.ordering.createRule(Boolean.FALSE, symbol, children));
        
        this.splitAtLeastTwo.addRule(this.splitAtLeastTwo.createRule(Boolean.TRUE, symbol, children));
        this.splitAtLeastTwo.addRule(this.splitAtLeastTwo.createRule(Boolean.FALSE, symbol, children));
        
        this.splitOrderedPairing.addRule(this.splitOrderedPairing.createRule(Boolean.TRUE, symbol, children));
        this.splitOrderedPairing.addRule(this.splitOrderedPairing.createRule(Boolean.FALSE, symbol, children));
        
        this.children.clear();
        this.children.add(Belnapian.BOTH_FALSE);
        this.termination.addRule(this.termination.createRule(Belnapian.BOTH_FALSE, symbol, children));
    }

    /**
     * 
     * @param symbol
     * @param mapping1
     * @param mapping2 
     */
    private void handleSingular(String symbol, Tree<String> mapping1, Tree<String> mapping2) {
        this.seenLeft.clear();
        this.seenRight.clear();
        
        addVariables(mapping1,this.seenLeft);
        addVariables(mapping2,this.seenRight);
        
        int vars = this.sig.getArityForLabel(symbol);
        
        this.children.clear();
        for(int i=0;i<vars;++i)
        {
            this.children.add(Boolean.FALSE);
        }
        this.variableSequenceing.addRule(this.variableSequenceing.createRule(Boolean.FALSE, symbol, children));
        this.variableSequenceing.addRule(this.variableSequenceing.createRule(Boolean.TRUE, symbol, children));
        
        this.children.clear();
        for(int i=0;i<vars;++i){
            this.children.add(this.seenRight.contains(i));
        }
        if(isLeft(mapping1,mapping2)){
            this.ordering.addRule(this.ordering.createRule(Boolean.FALSE, symbol, children));
        }else{
            this.ordering.addRule(this.ordering.createRule(Boolean.TRUE, symbol, children));
            this.ordering.addRule(this.ordering.createRule(Boolean.FALSE, symbol, children));
        }
        
        //TODO
        
        this.splitAtLeastTwo.addRule(this.splitAtLeastTwo.createRule(Boolean.TRUE, symbol, children));
        this.splitAtLeastTwo.addRule(this.splitAtLeastTwo.createRule(Boolean.FALSE, symbol, children));
        
        this.splitOrderedPairing.addRule(this.splitOrderedPairing.createRule(Boolean.TRUE, symbol, children));
        this.splitOrderedPairing.addRule(this.splitOrderedPairing.createRule(Boolean.FALSE, symbol, children));
        
        this.children.clear();
        this.children.add(Belnapian.BOTH_FALSE);
        this.termination.addRule(this.termination.createRule(Belnapian.BOTH_FALSE, symbol, children));
        // remember that singlar can also mean one terminal        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * 
     * @param symbol 
     */
    private void handleTermination(String symbol) {
        this.children.clear();
        
        this.variableSequenceing.addRule(this.variableSequenceing.createRule(Boolean.FALSE, symbol, children));
        this.variableSequenceing.addRule(this.variableSequenceing.createRule(Boolean.TRUE, symbol, children));
        
        this.ordering.addRule(this.ordering.createRule(Boolean.TRUE, symbol, children));
        this.ordering.addRule(this.ordering.createRule(Boolean.FALSE, symbol, children));
        
        this.splitAtLeastTwo.addRule(this.splitAtLeastTwo.createRule(Boolean.FALSE, symbol, children));
        
        this.termination.addRule(this.termination.createRule(Belnapian.BOTH_TRUE, symbol, children));
        
        this.splitOrderedPairing.addRule(this.splitOrderedPairing.createRule(Boolean.FALSE, symbol, children));
    }

    /**
     * 
     * @param symbol
     * @param mapping1
     * @param mapping2 
     */
    private void handlePair(String symbol, Tree<String> mapping1, Tree<String> mapping2) {
        this.children.clear();
        int arity = this.sig.getArityForLabel(symbol);
        
        for(int i=0;i<arity;++i){
            this.children.add(Boolean.FALSE);
        }
        
        this.variableSequenceing.addRule(this.variableSequenceing.createRule(Boolean.TRUE, symbol, children));
        this.variableSequenceing.addRule(this.variableSequenceing.createRule(Boolean.FALSE, symbol, children));
        
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void addVariables(Tree<String> mapping, IntSet variables) {
        List<Tree<String>> kids = mapping.getChildren();
        
        for(int i=0;i<kids.size();++i){
            variables.add(HomomorphismSymbol.getVariableIndex(kids.get(i).getLabel()));
        }
    }

    /**
     * 
     * @param mapping1
     * @param mapping2
     * @return 
     */
    private boolean isLeft(Tree<String> mapping1, Tree<String> mapping2) {
        return !mapping1.getChildren().isEmpty();
    }
    
    /**
     * 
     */
    public enum Belnapian{
       BOTH_TRUE,
       BOTH_FALSE,
       LEFT_TRUE,
       RIGHT_TRUE;
    }
}