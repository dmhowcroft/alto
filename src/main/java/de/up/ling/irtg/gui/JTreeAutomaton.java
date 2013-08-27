/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.gui;

import com.bric.window.WindowMenu;
import de.saar.basic.StringTools;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.automata.Rule;
import de.up.ling.irtg.automata.TreeAutomaton;
import de.up.ling.irtg.corpus.Corpus;
import static de.up.ling.irtg.gui.GuiMain.formatTimeSince;
import static de.up.ling.irtg.gui.GuiMain.log;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author koller
 */
public class JTreeAutomaton extends javax.swing.JFrame {
    private TreeAutomaton automaton;
    private InterpretedTreeAutomaton irtg;
    private List<String> annotationsInOrder;
    private List<Rule> rulesInOrder;

    /**
     * Creates new form JInterpretedTreeAutomaton
     */
    public JTreeAutomaton(TreeAutomaton<?> automaton, TreeAutomatonAnnotator annotator) {
        initComponents();

        jMenuBar2.add(new WindowMenu(this));

        this.automaton = automaton;

        Vector<String> columnIdentifiers = new Vector<String>();
        columnIdentifiers.add("");
        columnIdentifiers.add("");
        columnIdentifiers.add("");
        columnIdentifiers.add("weight");

        annotationsInOrder = new ArrayList<String>();
        if (annotator != null) {
            annotationsInOrder.addAll(annotator.getAnnotationIdentifiers());
            columnIdentifiers.addAll(annotationsInOrder);
        }

        entries.setColumnIdentifiers(columnIdentifiers);

        fillEntries(automaton, annotator);

        TableColumnAdjuster tca = new TableColumnAdjuster(jTable1);
//        tca.setOnlyAdjustLarger(false);
        tca.adjustColumns();

        final Color alternateRowColor = new Color(204, 229, 255);
        jTable1.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(alternateRowColor);
                }
                return c;
            }
        });
    }

    private void fillEntries(TreeAutomaton<?> automaton, TreeAutomatonAnnotator annotator) {
        rulesInOrder = new ArrayList<Rule>(automaton.getRuleSet());

        for (Rule rule : rulesInOrder) {
            Vector<String> row = new Vector<String>();
            row.add(automaton.getStateForId(rule.getParent()).toString() + (automaton.getFinalStates().contains(rule.getParent()) ? "!" : ""));
            row.add("->");

            List<String> resolvedRhsStates = new ArrayList<String>();
            for (int childState : rule.getChildren()) {
                resolvedRhsStates.add(automaton.getStateForId(childState).toString());
            }

            String label = automaton.getSignature().resolveSymbolId(rule.getLabel());
            row.add(label
                    + (rule.getArity() > 0 ? "(" : "")
                    + StringTools.join(resolvedRhsStates, ", ")
                    + (rule.getArity() > 0 ? ")" : ""));

            row.add("[" + Double.toString(rule.getWeight()) + "]");

            if (annotator != null) {
                for (String anno : annotationsInOrder) {
                    row.add(annotator.getAnnotation(rule, anno));
                }
            }

            entries.addRow(row);
        }
    }

    public void setIrtg(InterpretedTreeAutomaton irtg) {
        this.irtg = irtg;

    }

    public void setParsingEnabled(boolean enabled) {
        miParse.setEnabled(enabled);

        miTrainEM.setEnabled(enabled);
        miTrainML.setEnabled(enabled);
        miTrainVB.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entries = new javax.swing.table.DefaultTableModel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        miOpenIrtg = new javax.swing.JMenuItem();
        miOpenAutomaton = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        miSaveAutomaton = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        miQuit = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        miShowLanguage = new javax.swing.JMenuItem();
        miParse = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        miTrainML = new javax.swing.JMenuItem();
        miTrainEM = new javax.swing.JMenuItem();
        miTrainVB = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTable1.setModel(entries);
        jTable1.setAutoCreateRowSorter(true);
        jScrollPane1.setViewportView(jTable1);

        jMenu3.setText("File");

        miOpenIrtg.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        miOpenIrtg.setText("Open IRTG ...");
        miOpenIrtg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miOpenIrtgActionPerformed(evt);
            }
        });
        jMenu3.add(miOpenIrtg);

        miOpenAutomaton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        miOpenAutomaton.setText("Open tree automaton ...");
        miOpenAutomaton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miOpenAutomatonActionPerformed(evt);
            }
        });
        jMenu3.add(miOpenAutomaton);
        jMenu3.add(jSeparator1);

        miSaveAutomaton.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_MASK));
        miSaveAutomaton.setText("Save tree automaton ...");
        miSaveAutomaton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSaveAutomatonActionPerformed(evt);
            }
        });
        jMenu3.add(miSaveAutomaton);
        jMenu3.add(jSeparator2);

        miQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        miQuit.setText("Quit");
        miQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miQuitActionPerformed(evt);
            }
        });
        jMenu3.add(miQuit);

        jMenuBar2.add(jMenu3);

        jMenu4.setText("Tools");

        miShowLanguage.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.META_MASK));
        miShowLanguage.setText("Show language ...");
        miShowLanguage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miShowLanguageActionPerformed(evt);
            }
        });
        jMenu4.add(miShowLanguage);

        miParse.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.META_MASK));
        miParse.setText("Parse ...");
        miParse.setEnabled(false);
        miParse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miParseActionPerformed(evt);
            }
        });
        jMenu4.add(miParse);
        jMenu4.add(jSeparator3);

        miTrainML.setText("Maximum likelihood training ...");
        miTrainML.setEnabled(false);
        miTrainML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miTrainMLActionPerformed(evt);
            }
        });
        jMenu4.add(miTrainML);

        miTrainEM.setText("EM training ...");
        miTrainEM.setEnabled(false);
        miTrainEM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miTrainEMActionPerformed(evt);
            }
        });
        jMenu4.add(miTrainEM);

        miTrainVB.setText("Variational Bayes training ...");
        miTrainVB.setEnabled(false);
        miTrainVB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miTrainVBActionPerformed(evt);
            }
        });
        jMenu4.add(miTrainVB);

        jMenuBar2.add(jMenu4);

        setJMenuBar(jMenuBar2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void miOpenIrtgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miOpenIrtgActionPerformed
        GuiMain.loadIrtg(this);
    }//GEN-LAST:event_miOpenIrtgActionPerformed

    private void miOpenAutomatonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miOpenAutomatonActionPerformed
        GuiMain.loadAutomaton(this);
    }//GEN-LAST:event_miOpenAutomatonActionPerformed

    private void miSaveAutomatonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSaveAutomatonActionPerformed
        GuiMain.saveAutomaton(automaton, this);
    }//GEN-LAST:event_miSaveAutomatonActionPerformed

    private void miQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miQuitActionPerformed
        GuiMain.quit();
    }//GEN-LAST:event_miQuitActionPerformed

    private void miShowLanguageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miShowLanguageActionPerformed
        JLanguageViewer lv = new JLanguageViewer();
        lv.setAutomaton(automaton, irtg);
        lv.setTitle("Language of " + getTitle());
        lv.pack();
        lv.setVisible(true);
    }//GEN-LAST:event_miShowLanguageActionPerformed

    private void miParseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miParseActionPerformed
        if (irtg != null) {
            final Map<String, String> inputs = JInputForm.getValues(annotationsInOrder, this);

            if (inputs != null) {
                new Thread() {
                    @Override
                    public void run() {
                        TreeAutomaton chart = null;

                        try {
                            long start = System.nanoTime();
                            chart = irtg.parse(inputs);
                            log("Computed parse chart for " + inputs + ", " + formatTimeSince(start));
                        } catch (ParserException ex) {
                            GuiMain.showError(JTreeAutomaton.this, "An error occurred while parsing the input objects " + inputs + ": " + ex.getMessage());
                        }

                        if (chart != null) {
                            JTreeAutomaton jta = new JTreeAutomaton(chart, null);
                            jta.setIrtg(irtg);
                            jta.setTitle("Parse chart: " + inputs);
                            jta.pack();
                            jta.setVisible(true);
                        }
                    }
                }.start();
            }
        }
    }//GEN-LAST:event_miParseActionPerformed

    private void updateWeights() {
        for (int i = 0; i < rulesInOrder.size(); i++) {
            entries.setValueAt("[" + rulesInOrder.get(i).getWeight() + "]", i, 3);
        }
    }

    private void miTrainMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miTrainMLActionPerformed
        Corpus corpus = GuiMain.loadAnnotatedCorpus(irtg, this);

        if (corpus != null) {
            long start = System.nanoTime();
            irtg.trainML(corpus);
            GuiMain.log("Performed ML training, " + GuiMain.formatTimeSince(start));
            updateWeights();
        }
    }//GEN-LAST:event_miTrainMLActionPerformed

    private void miTrainEMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miTrainEMActionPerformed
        new Thread() {
            @Override
            public void run() {
                Corpus corpus = GuiMain.loadUnannotatedCorpus(irtg, JTreeAutomaton.this);

                if (corpus != null) {
                    long start = System.nanoTime();
                    irtg.trainEM(corpus);
                    GuiMain.log("Performed EM training, " + GuiMain.formatTimeSince(start));
                    updateWeights();
                }
            }
        }.start();
    }//GEN-LAST:event_miTrainEMActionPerformed

    private void miTrainVBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miTrainVBActionPerformed
        new Thread() {
            @Override
            public void run() {
                Corpus corpus = GuiMain.loadUnannotatedCorpus(irtg, JTreeAutomaton.this);

                if (corpus != null) {
                    long start = System.nanoTime();
                    irtg.trainVB(corpus);
                    GuiMain.log("Performed VB training, " + GuiMain.formatTimeSince(start));
                    updateWeights();
                }
            }
        }.start();

    }//GEN-LAST:event_miTrainVBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.table.DefaultTableModel entries;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JMenuItem miOpenAutomaton;
    private javax.swing.JMenuItem miOpenIrtg;
    private javax.swing.JMenuItem miParse;
    private javax.swing.JMenuItem miQuit;
    private javax.swing.JMenuItem miSaveAutomaton;
    private javax.swing.JMenuItem miShowLanguage;
    private javax.swing.JMenuItem miTrainEM;
    private javax.swing.JMenuItem miTrainML;
    private javax.swing.JMenuItem miTrainVB;
    // End of variables declaration//GEN-END:variables
}
