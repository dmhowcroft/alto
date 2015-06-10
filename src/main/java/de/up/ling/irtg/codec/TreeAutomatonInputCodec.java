/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.codec;

import de.up.ling.irtg.automata.ConcreteTreeAutomaton;
import de.up.ling.irtg.automata.TreeAutomaton;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.atn.PredictionMode;

/**
<<<<<<< local
 * An input codec that reads a top-down tree automaton.
 * Rules take the following form:<p>
=======
 * A codec for reading tree automata. A string representation of a
 * tree automaton consists of a list of rules, one per line. Rules look
 * like this:<p>
>>>>>>> other
 * 
<<<<<<< local
 * <pre>
 * S! -> r(NP, VP) [0.3]
 * </pre>
=======
 * <code>A! -&gt; f(B,C,D)</code><p>
>>>>>>> other
 * 
<<<<<<< local
 * where S, NP, and VP are states and r is a terminal symbol.
 * S is a final state, as indicated by the exclamation mark.
 * Each rule can optionally be assigned a weight in square brackets;
 * in the example, the weight is 0.3. Rules that do not have an
 * explicit weight get a default weight of 1.
=======
 * Here A,B,C,D are states (= nonterminal symbols) and f is a three-place symbol
 * from the signature of the tree automaton. The exclamation mark after S
 * says that S is a final state (= start symbol).<p>
>>>>>>> other
 * 
<<<<<<< local
=======
 * Drop the brackets if f is a zero-place symbol, i.e. a rule for leaves
 * looks like this: <code>D -&gt; a</code>.
 * 
>>>>>>> other
 * @author koller
 */
@CodecMetadata(name = "auto", description = "Tree automata", extension = "auto", type = TreeAutomaton.class)
public class TreeAutomatonInputCodec extends InputCodec<TreeAutomaton> {

    private ConcreteTreeAutomaton<String> automaton = null;

    @Override
    public TreeAutomaton read(InputStream is) throws ParseException, IOException {
        TreeAutomatonLexer l = new TreeAutomatonLexer(new ANTLRInputStream(is));
        TreeAutomatonParser p = new TreeAutomatonParser(new CommonTokenStream(l));
        p.setErrorHandler(new ExceptionErrorStrategy());
        p.getInterpreter().setPredictionMode(PredictionMode.SLL);
        
        automaton = new ConcreteTreeAutomaton<>();

        try {
            TreeAutomatonParser.FtaContext result = p.fta();

            for (TreeAutomatonParser.Auto_ruleContext ruleContext : result.auto_rule()) {
                autoRule(ruleContext);
            }

            return automaton;
        } catch (RecognitionException e) {
            throw new ParseException(e.getMessage());
        }
    }

    private String autoRule(TreeAutomatonParser.Auto_ruleContext auto_rule) {
        String parent = state(auto_rule.state());
        List<String> children = statelist(auto_rule.state_list());
        String label = name(auto_rule.name());
        double weight = weight(auto_rule.weight());

        automaton.addRule(automaton.createRule(parent, label, children, weight));

        return label;
    }

    private List<String> statelist(TreeAutomatonParser.State_listContext rule_args) {
        List<String> ret = new ArrayList<String>();

        if (rule_args != null) {
            for (TreeAutomatonParser.StateContext sc : rule_args.state()) {
                ret.add(state(sc));
            }
        }

        return ret;
    }

    private String state(TreeAutomatonParser.StateContext sc) {
        String ret = name(sc.name());
        int state = automaton.addState(ret);

        if (sc.FIN_MARK() != null) {
            automaton.addFinalState(state);
        }

        return ret;
    }

    private String name(TreeAutomatonParser.NameContext nc) {
        boolean isQuoted = (nc instanceof TreeAutomatonParser.QUOTEDContext);

        assert !isQuoted || nc.getText().startsWith("'") || nc.getText().startsWith("\"") : "invalid symbol: -" + nc.getText() + "- " + nc.getClass();

        return CodecUtilities.extractName(nc, isQuoted);
    }

    private double weight(TreeAutomatonParser.WeightContext weight) {
        if (weight == null) {
            return 1;
        } else {
            return Double.parseDouble(CodecUtilities.stripOuterChars(weight.getText()));
        }
    }
}
