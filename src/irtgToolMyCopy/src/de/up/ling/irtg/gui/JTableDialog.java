/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.up.ling.irtg.gui;

import com.bric.window.WindowMenu;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author koller
 */
public class JTableDialog<E> extends javax.swing.JFrame {
    private DefaultTableModel model;

    /**
     * Creates new form JTableDialog
     */
    public JTableDialog(String title, List<E> data) {
        super();
        setTitle(title);

        initComponents();
        fillTable(data);

        jMenuBar1.add(new WindowMenu(this));
    }

    private void fillTable(List<E> data) {
        if (data != null && !data.isEmpty()) {
            E first = data.get(0);

            Vector<String> columnIdentifiers = new Vector<String>();
            Field[] fields = first.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                columnIdentifiers.add(fields[i].getName());
            }

            model = new DefaultTableModel();
            table.setModel(model);
            model.setColumnIdentifiers(columnIdentifiers);

            for (E element : data) {
                Vector<String> row = new Vector<String>();
                for (int i = 0; i < fields.length; i++) {
                    try {
                        row.add(fields[i].get(element).toString());
                    } catch (Exception ex) {
                        row.add("---");
                    }
                }

                model.addRow(row);
            }

            final Color alternateRowColor = new Color(204, 229, 255);
            table.setDefaultRenderer(Object.class, new TableCellRenderer() {
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
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        miCloseWindow = new javax.swing.JMenuItem();
        miCloseAllWindows = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        miQuit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setViewportView(table);

        fileMenu.setText("File");

        miCloseWindow.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.META_MASK));
        miCloseWindow.setText("Close Window");
        miCloseWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCloseWindowActionPerformed(evt);
            }
        });
        fileMenu.add(miCloseWindow);

        miCloseAllWindows.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.META_MASK));
        miCloseAllWindows.setText("Close All Windows");
        miCloseAllWindows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCloseAllWindowsActionPerformed(evt);
            }
        });
        fileMenu.add(miCloseAllWindows);
        fileMenu.add(jSeparator1);

        miQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.META_MASK));
        miQuit.setText("Quit");
        miQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miQuitActionPerformed(evt);
            }
        });
        fileMenu.add(miQuit);

        jMenuBar1.add(fileMenu);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void miCloseWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCloseWindowActionPerformed
        setVisible(false);
    }//GEN-LAST:event_miCloseWindowActionPerformed

    private void miCloseAllWindowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCloseAllWindowsActionPerformed
        GuiMain.closeAllWindows();
    }//GEN-LAST:event_miCloseAllWindowsActionPerformed

    private void miQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miQuitActionPerformed
        GuiMain.quit();
    }//GEN-LAST:event_miQuitActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JMenu fileMenu;
    protected javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem miCloseAllWindows;
    private javax.swing.JMenuItem miCloseWindow;
    private javax.swing.JMenuItem miQuit;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}