/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.up.ling.irtg.codec.bolinas_hrg

import org.junit.*
import java.util.*
import java.io.*
import de.up.ling.irtg.*
import de.up.ling.irtg.InterpretedTreeAutomaton
import de.up.ling.irtg.automata.*
import de.up.ling.irtg.automata.TreeAutomaton
import static org.junit.Assert.*
import de.saar.chorus.term.parser.*;
import de.up.ling.tree.*;
import de.up.ling.irtg.algebra.*;
import de.up.ling.irtg.hom.*;
import static de.up.ling.irtg.util.TestingTools.*;


/**
 *
 * @author koller
 */
class BolinasHrgInputCodecTest {
    @Test
    public void testHrg1() {
        String hrg = '''\n\
START -> (. :N_0_root_1$ ); 0.5
N_0_root_1 -> (. :N_0_0$  :ARG0 (. :N_1_1$ ) :N_2$ (.sheep ) ); 0.002
N_0_0 -> (."need-01" ); 0.002
N_1_1 -> (.i ); 0.032
N_2 -> (. :ARG1 .*0 ); 0.112
        ''';
        
        assertAmrInHrgLanguage(hrg, I_NEED_SHEEP)
    }
    
    @Test
    public void testHrg2() {
        String hrg = '''START -> (. :N_0_root_1$ ); 0.5
N_0_root_1 -> (. :N_0_0$  :ARG0 (. :N_1_1$ ) :N_2$ (.sheep ) ); 0.002
N_0_0 -> (."need-01" ); 0.002
N_1_1 -> (.i ); 0.032
N_2 -> (. :ARG1 .*1 ); 0.112
        ''';
        
        assertAmrInHrgLanguage(hrg, I_NEED_SHEEP)
    }
    
    @Test
    public void testHrg3() {
        String hrg = '''START -> (. :N_0_root_1$ ); 0.5
N_0_root_1 -> (. :N_0_0$  :ARG0 (. :N_1_1$ ) :N_2$ (.sheep ) ); 0.002
N_0_0 -> (."need-01" ); 0.002
N_1_1 -> (.i ); 0.032
N_2 -> (. :ARG1 .*2 ); 0.112
        ''';
        
        assertAmrInHrgLanguage(hrg, I_NEED_SHEEP)
    }
    
    
    /**
     * Test String taken from the  
     * public Release 1.4 of the AMR Bank. (1,562 sentences from The Little
     * Prince; November 14, 2014) which were generated by
     *  -The Linguistic Data Consortium SDL
     *  -The University of Colorado's Center for Computational Language and Education Research (CLEAR)
     *  -The University of Southern California's Information Sciences Institute (ISI)
     *   and Computational Linguistics at USC. 
     */
    private static final String I_NEED_SHEEP = """(n / need-01
  :ARG0 (i / i)
  :ARG1 (s / sheep))""";
    
    private void assertAmrInHrgLanguage(String hrg, String amr) {
        InterpretedTreeAutomaton irtg = phrg(hrg);        
        TreeAutomaton chart = irtg.parse(["Graph":amr])        
        assert ! chart.isEmpty();     
    }
    
    private InterpretedTreeAutomaton phrg(String hrg) {
        BolinasHrgInputCodec ic = new BolinasHrgInputCodec();
        return ic.read(hrg);
    }

    @Test
    public void testBoyGirl()
    {
        InterpretedTreeAutomaton irtg = phrg(BOY_GIRL_GRAMMAR);
        
        assertEquals(irtg.getAutomaton().getFinalStates().size(),1);
        
        
        System.out.println(irtg.toString());
        //TODO
    } 
    
    /**
     * Example Grammar taken from the bolinas toolkit. The original license
     * agreement statement from the bolinas toolkit (applicable only to the string assigned to BOY_GIRL_GRAMMAR)
     * is as follows:
     * 
     * Bolinas is provided under the MIT License (MIT)
     *
     * Copyright (C) 2013 Jacob Andreas, Daniel Bauer, David Chiang, Karl Moritz Hermann, Kevin Knight
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
    */
    private static final BOY_GIRL_GRAMMAR = """# Example from Figure 3
T -> (. :want' :arg0 (x. :E\$) :arg1 (. :T\$ x.));
T -> (. :believe' :arg0 (. :girl') :arg1 (. :T\$ .*)); 
T -> (. :want' :arg1 .*);
E -> (. :boy');

#Example from Figure 4
T -> (. :want' :arg0 (x. :E\$) :arg1 (. :X\$ x.));
X -> (. :believe' :arg0 (x. :girl') :arg1 (. :Y\$ x. .*)); 
Y -> (. :want' :arg0 .*2 :arg1 .*1);""";
}