/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.up.ling.irtg.automata.condensed;

import de.saar.basic.Pair;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.Algebra;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.automata.Rule;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.codec.InputCodec;
import de.up.ling.irtg.hom.Homomorphism;
import de.up.ling.irtg.signature.SignatureMapper;
import de.up.ling.irtg.util.IntInt2IntMap;
import de.up.ling.irtg.util.Util;
import de.up.ling.tree.ParseException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Creating a new automaton by intersecting a normal TreeAutomaton (left) 
 * and a CondensedTreeAutomaton (right).
 * This version uses a CKY algorithm, so you get the fastest result if the rules
 * in the left automaton can be accessed quickly button-up, while the right automaton
 * needs a top-down access. 
 * Typically it can be used to intersect the TreeAutomaton of an IRTG on the left
 * and the inversed homomorphism of a decomposition automaton that is generated by an 
 * algebra for a string, that should be parsed. In other words: Parsing an input 
 * with a given IRTG.
 * This class has been optimized for IRTGs representing PCFGs (May 2014).
 * Note that the right automaton must not have any recursion.
 * 
 * @author koller
 * @param <LeftState>
 * @param <RightState>
 */
public abstract class GenericCondensedIntersectionAutomaton<LeftState, RightState> extends TreeAutomaton<Pair<LeftState, RightState>> {
    
    private final TreeAutomaton<LeftState> left;
    private final CondensedTreeAutomaton<RightState> right;
    boolean DEBUG = false;
    private final SignatureMapper leftToRightSignatureMapper;

    private final IntInt2IntMap stateMapping;  // right state -> left state -> output state
    // (index first by right state, then by left state because almost all right states
    // receive corresponding left states, but not vice versa. This keeps outer map very dense,
    // and makes it suitable for a fast ArrayMap)
    
    abstract protected void collectOutputRule(Rule outputRule);
    abstract protected void addAllOutputRules();
    
    @FunctionalInterface
    protected static interface IntersectionCall {
        public TreeAutomaton intersect(TreeAutomaton left, CondensedTreeAutomaton right);
    }
    
    public GenericCondensedIntersectionAutomaton(TreeAutomaton<LeftState> left, CondensedTreeAutomaton<RightState> right, SignatureMapper sigMapper) {
        super(left.getSignature()); // TODO = should intersect this with the (remapped) right signature

        this.leftToRightSignatureMapper = sigMapper;
        
        this.left = left;
        this.right = right;

        finalStates = null;

        stateMapping = new IntInt2IntMap();
    }
    
    // Intersecting the two automatons using a CKY algorithm
    @Override
    public void makeAllRulesExplicit() {
        if (!isExplicit) {
            isExplicit = true;
            getStateInterner().setTrustingMode(true);

            Int2ObjectMap<IntSet> partners = new Int2ObjectOpenHashMap<IntSet>();

            long t1 = System.nanoTime();

            // Perform a DFS in the right automaton to find all partner states
            IntSet visited = new IntOpenHashSet();
            right.getFinalStates().forEach((q) -> {
                // starting the dfs by the final states ensures a topological order
                ckyDfsForStatesInBottomUpOrder(q, visited, partners);
            });
            
            // transfer all collected rules into the output automaton
            addAllOutputRules();

            // force recomputation of final states
            finalStates = null;

            if (DEBUG) {
                System.err.println("CKY runtime: " + (System.nanoTime() - t1) / 1000000 + "ms");
                System.err.println("Intersection automaton CKY:\n" + toString() + "\n~~~~~~~~~~~~~~~~~~");
            }
        }
    }
    
    /**
     * Iterate over all states in the right (condensed) automaton to find partner states in the left one.
     * @param q Current state
     * @param visited already visited states
     * @param partners already found partner states
     */
    private void ckyDfsForStatesInBottomUpOrder(int q, IntSet visited, final Int2ObjectMap<IntSet> partners) {
        if (!visited.contains(q)) {
            if (DEBUG) {
                System.err.println("StateRight: " + q);
            }
            visited.add(q);
            for (final CondensedRule rightRule : right.getCondensedRulesByParentState(q)) {
                if (DEBUG) {
                    System.err.println("Right rule: " + rightRule.toString(right));
                }
                int[] rightChildren = rightRule.getChildren();
                List<IntSet> remappedChildren = new ArrayList<IntSet>();

                // iterate over all children in the right rule
                for (int i = 0; i < rightRule.getArity(); ++i) {
                    // go into the recursion first to obtain the topological order that is needed for the CKY algorithm
                    ckyDfsForStatesInBottomUpOrder(rightChildren[i], visited, partners);

                    // take the right-automaton label for each child and get the previously calculated left-automaton label from partners.
                    remappedChildren.add(partners.get(rightChildren[i]));
                }

                // find all rules bottom-up in the left automaton that have the same (remapped) children as the right rule.
                left.foreachRuleBottomUpForSets(rightRule.getLabels(right), remappedChildren, leftToRightSignatureMapper, leftRule -> {
                    // create a new rule
                    Rule rule = combineRules(leftRule, rightRule);
                    if (DEBUG) {
                        System.err.println("Left rule: " + leftRule.toString(left));
                        System.err.println("Combined rule: " + rule.toString(this));
                    }
                    // transfer rule to staging area for output rules
                    collectOutputRule(rule);

                    // remember the newly found partneres if needed
                    IntSet knownPartners = partners.get(rightRule.getParent());

                    if (knownPartners == null) {
                        knownPartners = new IntOpenHashSet();
                        partners.put(rightRule.getParent(), knownPartners);
                    }

                    knownPartners.add(leftRule.getParent());
                });

            }
        }
    }

    private int addStatePair(int leftState, int rightState) {
        int ret = stateMapping.get(rightState, leftState);

        if (ret == 0) {
            ret = addState(new Pair(left.getStateForId(leftState), right.getStateForId(rightState)));
            stateMapping.put(rightState, leftState, ret);
        }

        return ret;
    }

    Rule combineRules(Rule leftRule, CondensedRule rightRule) {
        int[] childStates = new int[leftRule.getArity()];

        for (int i = 0; i < leftRule.getArity(); i++) {
            childStates[i] = addStatePair(leftRule.getChildren()[i], rightRule.getChildren()[i]);
        }

        int parentState = addStatePair(leftRule.getParent(), rightRule.getParent());

        return createRule(parentState, leftRule.getLabel(), childStates, leftRule.getWeight() * rightRule.getWeight());
    }
    
    @Override
    public boolean isBottomUpDeterministic() {
        return left.isBottomUpDeterministic() && right.isBottomUpDeterministic();
    }

    @Override
    public IntSet getFinalStates() {
        if (finalStates == null) {
            getAllStates(); // initialize data structure for addState

            finalStates = new IntOpenHashSet();
            collectStatePairs(left.getFinalStates(), right.getFinalStates(), finalStates);
        }

        return finalStates;
    }

    // get all states for this automaton, that are the result of the combination of a state in the
    // leftStates set and one in the rightStates set
    private void collectStatePairs(IntSet leftStates, IntSet rightStates, IntSet pairStates) {
        leftStates.forEach((leftState) -> {
            rightStates.stream().map((rightState) -> 
                    stateMapping.get(rightState, leftState)).filter((state) -> 
                            (state != 0)).forEach((state) -> {
                pairStates.add(state);
            });
        });
    }

    @Override
    public Iterable<Rule> getRulesBottomUp(int label, int[] childStates) {
        makeAllRulesExplicit();

        assert useCachedRuleBottomUp(label, childStates);

        return getRulesBottomUpFromExplicit(label, childStates);
    }

    @Override
    public Iterable<Rule> getRulesTopDown(int label, int parentState) {
        makeAllRulesExplicit();

        assert useCachedRuleTopDown(label, parentState);

        return getRulesTopDownFromExplicit(label, parentState);
    }

    /**
     * Function to test the efficiency of this intersection algorithm by parsing each 
     * sentence in a text file with a given IRTG.
     * Args: /path/to/grammar.irtg, /path/to/sentences, interpretation, /path/to/result_file, "Additional comment"
     * @param args CMD arguments
     * @param showViterbiTrees 
     * @param icall what intersection should be used?
     * @throws FileNotFoundException
     * @throws ParseException
     * @throws IOException
     * @throws ParserException
     * @throws AntlrIrtgBuilder.ParseException
     */
    public static void main(String[] args, boolean showViterbiTrees, IntersectionCall icall) throws FileNotFoundException, ParseException, IOException, ParserException, de.up.ling.irtg.codec.ParseException {
        if (args.length != 5) {
            System.err.println("1. IRTG\n"
                    + "2. Sentences\n"
                    + "3. Interpretation\n"
                    + "4. Output file\n"
                    + "5. Comments");
            System.exit(1);
        }

        String irtgFilename = args[0];
        String sentencesFilename = args[1];
        String interpretation = args[2];
        String outputFile = args[3];
        String comments = args[4];
        long totalChartTime = 0;
        long totalViterbiTime = 0;

        // initialize CPU-time benchmarking
        long[] timestamp = new long[10];
        ThreadMXBean benchmarkBean = ManagementFactory.getThreadMXBean();
        boolean useCPUTime = benchmarkBean.isCurrentThreadCpuTimeSupported();
        if (useCPUTime) {
            System.err.println("Using CPU time for measuring the results.");
        }

        System.err.print("Reading the IRTG...");

        updateBenchmark(timestamp, 0, useCPUTime, benchmarkBean);
        
        InputCodec<InterpretedTreeAutomaton> codec = InputCodec.getInputCodecByExtension(Util.getFilenameExtension(irtgFilename));
        InterpretedTreeAutomaton irtg = codec.read(new FileInputStream(new File(irtgFilename)));
        Interpretation interp = irtg.getInterpretation(interpretation);
        Homomorphism hom = interp.getHomomorphism();
        Algebra alg = irtg.getInterpretation(interpretation).getAlgebra();

        updateBenchmark(timestamp, 1, useCPUTime, benchmarkBean);
        
//        irtg.getAutomaton().analyze();

        System.err.println(" Done in " + ((timestamp[1] - timestamp[0]) / 1000000) + "ms");
        try {
            File oFile = new File(outputFile);
            FileWriter outstream = new FileWriter(oFile);
            BufferedWriter out = new BufferedWriter(outstream);
            out.write("Testing IntersectionAutomaton with condensed intersection ...\n"
                    + "IRTG-File  : " + irtgFilename + "\n"
                    + "Input-File : " + sentencesFilename + "\n"
                    + "Output-File: " + outputFile + "\n"
                    + "Comments   : " + comments + "\n"
                    + "CPU-Time   : " + useCPUTime + "\n\n");
            out.flush();

            try {
                // setting up inputstream for the sentences
                FileInputStream instream = new FileInputStream(new File(sentencesFilename));
                DataInputStream in = new DataInputStream(instream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String sentence;
                int times = 0;
                int sentences = 0;

                while ((sentence = br.readLine()) != null) {
                    ++sentences;
                    System.err.println("\nSentence #" + sentences);
                    System.err.println("Current sentence: " + sentence);
                    updateBenchmark(timestamp, 2, useCPUTime, benchmarkBean);

                    // intersect
                    TreeAutomaton decomp = alg.decompose(alg.parseString(sentence));
                    CondensedTreeAutomaton inv = decomp.inverseCondensedHomomorphism(hom);

                    TreeAutomaton<String> result = icall.intersect(irtg.getAutomaton(), inv);

                    updateBenchmark(timestamp, 3, useCPUTime, benchmarkBean);

                    long thisChartTime = (timestamp[3] - timestamp[2]);
                    totalChartTime += thisChartTime;
                    System.err.println("-> Chart " + (thisChartTime / 1000000) + "ms, cumulative " + totalChartTime/1000000 + "ms");
                    out.write("Parsed \n" + sentence + "\nIn " + ((timestamp[3] - timestamp[2]) / 1000000) + "ms.\n\n");
                    out.flush();

                    if (result.getFinalStates().isEmpty()) {
                        System.err.println("**** EMPTY ****\n");
                    } else if(showViterbiTrees) {
                        System.err.println(result.viterbi());
                        updateBenchmark(timestamp, 4, useCPUTime, benchmarkBean);
                        long thisViterbiTime = timestamp[4]-timestamp[3];
                        totalViterbiTime += thisViterbiTime;
                        
                        System.err.println("-> Viterbi " + thisViterbiTime/1000000 + "ms, cumulative " + totalViterbiTime/1000000 + "ms");
                    }

                    times += (timestamp[3] - timestamp[2]) / 1000000;
                }
                out.write("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n Parsed " + sentences + " sentences in " + times + "ms. \n");
                out.flush();
            } catch (IOException ex) {
                System.err.println("Error while reading the Sentences-file: " + ex.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("Error while writing to file:" + ex.getMessage());
            ex.printStackTrace(System.err);
        }

    }

    // Saves the current time / CPU time in the timestamp-variable
    private static void updateBenchmark(long[] timestamp, int index, boolean useCPU, ThreadMXBean bean) {
        if (useCPU) {
            timestamp[index] = bean.getCurrentThreadCpuTime();
        } else {
            timestamp[index] = System.nanoTime();
        }
    }

}