/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.gui;

import com.google.common.collect.Iterators;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.IrtgParser;
import de.up.ling.irtg.algebra.Algebra;
import de.up.ling.irtg.binarization.RegularSeed;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;

/**
 *
 * @author koller
 */
public class RegularSeedChooser extends javax.swing.JDialog {

    private Map<String, InterpretationSeedPanel> isPanels;
    private InterpretedTreeAutomaton irtg;
    private Map<String, Algebra> selectedAlgebras;
    private Map<String, RegularSeed> selectedSeeds;

    /**
     * Creates new form RegularSeedChooser
     */
    public RegularSeedChooser(InterpretedTreeAutomaton irtg, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        getRootPane().setDefaultButton(okButton);

        ispContainer.setLayout(new BoxLayout(ispContainer, BoxLayout.Y_AXIS));

        List<Class> algebraClasses = new ArrayList<Class>();
        Iterators.addAll(algebraClasses, Algebra.getAllAlgebraClasses());
        Collections.sort(algebraClasses, new ClassByNameComparator());

        List<Class> regularSeedClasses = new ArrayList<Class>();
        Iterators.addAll(regularSeedClasses, RegularSeed.getAllRegularSeedClasses());
        Collections.sort(regularSeedClasses, new ClassByNameComparator());

        this.irtg = irtg;

        isPanels = new HashMap<String, InterpretationSeedPanel>();

        for (String interp : irtg.getInterpretations().keySet()) {
            InterpretationSeedPanel isp = new InterpretationSeedPanel(interp, algebraClasses, regularSeedClasses);
            ispContainer.add(isp);
            isPanels.put(interp, isp);
        }

        pack();
    }
    
    public static class ClassByNameComparator implements Comparator<Class> {
        public int compare(Class o1, Class o2) {
            return o1.getName().compareTo(o2.getName());
        }        
    }

    public Map<String, Algebra> getSelectedAlgebras() {
        return selectedAlgebras;
    }

    public Map<String, RegularSeed> getSelectedSeeds() {
        return selectedSeeds;
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ispContainer = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.jdesktop.layout.GroupLayout ispContainerLayout = new org.jdesktop.layout.GroupLayout(ispContainer);
        ispContainer.setLayout(ispContainerLayout);
        ispContainerLayout.setHorizontalGroup(
            ispContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        ispContainerLayout.setVerticalGroup(
            ispContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(ispContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(okButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(cancelButton)
                        .add(0, 215, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(ispContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okButton)
                    .add(cancelButton)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        selectedAlgebras = new HashMap<String, Algebra>();
        selectedSeeds = new HashMap<String, RegularSeed>();

        for (String interp : irtg.getInterpretations().keySet()) {
            try {
                Class ac = isPanels.get(interp).getSelectedAlgebra();
                Algebra a = irtg.getInterpretation(interp).getAlgebra();
                
                if( ac != null ) {
                    a = (Algebra) ac.newInstance();
                }
                
                Class rc = isPanels.get(interp).getSelectedRegularSeed();
                RegularSeed rs = (RegularSeed) rc.getConstructor(Algebra.class, Algebra.class).newInstance(irtg.getInterpretation(interp).getAlgebra(), a);
                
                selectedAlgebras.put(interp, a);
                selectedSeeds.put(interp, rs);
            } catch (Exception e) {
                GuiMain.log("Exception in constructing binarizer for interpretation " + interp + ": " + e.toString());
            }
        }
        
        setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        selectedAlgebras = null;
        selectedSeeds = null;
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel ispContainer;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
