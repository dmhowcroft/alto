/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.up.ling.irtg.algebra

import org.junit.*
import java.util.*
import java.io.*
import de.up.ling.irtg.automata.*
import static org.junit.Assert.*
import de.saar.chorus.term.parser.*;
import de.up.ling.tree.*;
import de.up.ling.irtg.algebra.*;
import de.up.ling.irtg.hom.*;
import static de.up.ling.irtg.util.TestingTools.*;
import de.up.ling.irtg.*

import org.junit.*
import java.util.*
import java.io.*
import de.up.ling.irtg.automata.*
import static org.junit.Assert.*
import de.saar.chorus.term.parser.*;
import de.up.ling.tree.*;
import de.up.ling.irtg.algebra.*;
import de.up.ling.irtg.hom.*;
import static de.up.ling.irtg.util.TestingTools.*;
import de.up.ling.irtg.*
/**
 *
 * @author Danilo Baumgarten
 */
class PtbTreeAlgebraTest {
    @Test
    public void testPtbTreeAlgebra() {
        PtbTreeAlgebra pta = new PtbTreeAlgebra();
        Tree<String> tree = pta.parseString("( (`` ``) (INTJ (UH Yes) (. .) ))");
        assertEquals("INTJ('``'('``'),UH(yes),'.'('.'))", tree.toString());

        PtbTreeAlgebra.binarizeInit();
        tree = PtbTreeAlgebra.binarizeAndRelabel(tree);
        assertEquals("INTJ3('``1^INTJ3'('``'),'ART-UH1-.1^INTJ3'('UH1^INTJ3'(yes),'ART-.1^INTJ3'('.1^INTJ3'('.'))))", tree.toString());
    }
}