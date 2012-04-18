/*
    Copyright 2011 Northbranchlogic, Inc.

    This file is part of Parallel Processing with EC2 (ppe).

    ppe is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ppe is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ppe.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * AboutGui.java
 *
 * Created on Mar 18, 2010, 1:40:31 PM
 */

package com.norbl.cbp.ppe.gui;

import com.norbl.util.gui.*;
import javax.swing.*;

/**
 *
 * @author moi
 */
public class AboutGui extends javax.swing.JFrame {

    /** Creates new form AboutGui */
    public AboutGui() {
        initComponents();
    }
    
    public void setTitle(String s) {
        titleLabel.setText(s);
    }
    
    public void setVersion(String s) {
        versionLabel.setText(s);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(java.awt.Color.white);

        titleLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 36)); // NOI18N
        titleLabel.setText("ppe-ompi");

        versionLabel.setText("<html>Version 1.2<br><br><br> &copy; 2012, Barnet Wagman<br>Northbranchlogic, Inc. <html>");

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/norbl/cbp/ppe/gui/nbl_230_linear_sharp_12.jpg"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(titleLabel)
                            .addComponent(versionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(versionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55)
                .addComponent(jLabel3)
                .addGap(28, 28, 28))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GuiMetrics.init();
                AboutGui ag = new AboutGui();
                GuiUtil.centerOnScreen(ag);
                setNBLIcon(ag);
                ag.setVisible(true);
            }
        });
    }

        // ------------ Decoration ---------------------

    public static void setNBLIcon(JFrame f) {
        try {
            java.awt.image.BufferedImage img =
                javax.imageio.ImageIO.read((new Object()).getClass().getResource(
                                    "/ppe/gui/nb_icon_sharp_35.jpeg"));
            f.setIconImage(img);
        }
        catch(Exception x) { throw new RuntimeException(x); }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

}