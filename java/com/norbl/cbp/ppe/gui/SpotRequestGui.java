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
 * SpotRequestGui.java
 *
 * Created on May 9, 2011, 12:31:03 AM
 */

package com.norbl.cbp.ppe.gui;

import com.norbl.util.gui.*;
import com.norbl.util.*;

/**
 *
 * @author Barnet Wagman
 */
public class SpotRequestGui extends javax.swing.JFrame {

    String networkName;
    String instancetType;
    String imageID;
    int nInstances;
    double spotPrice;

        /** Creates new form SpotRequestGui */
    public SpotRequestGui(String networkName,
                          String instancetType,
                          String imageID,
                          int nInstances,
                          double spotPrice) {
        this.networkName = networkName;
        this.instancetType = instancetType;
        this.imageID = imageID;
        this.nInstances = nInstances;
        this.spotPrice = spotPrice;
    }

    public void createGui() {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                initComponents();

                networkNameLabel.setText(networkName);
                instanceTypeLabel.setText(instancetType);
                amiLabel.setText(imageID);
                nInstanceLabel.setText(Integer.toString(nInstances));
                maxSpotPriceLabel.setText(StringUtil.f2(spotPrice));
                statusLabel.setText("-");

                GuiUtil.centerOnScreen(SpotRequestGui.this);
                SpotRequestGui.this.setVisible(true);
                SpotRequestGui.this.setAlwaysOnTop(true);
            }});
    }

    public void setStatus(final String s) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                statusLabel.setText(s);
             }});
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        instanceTypeLabel = new javax.swing.JLabel();
        amiLabel = new javax.swing.JLabel();
        nInstanceLabel = new javax.swing.JLabel();
        maxSpotPriceLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        networkNameLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Arial", 1, 15)); // NOI18N
        jLabel1.setText("Spot request pending");

        jLabel2.setText("Network name:");

        jLabel3.setText("Instance type:");

        jLabel4.setText("AMI:");

        jLabel5.setText("N instances requested:");

        jLabel6.setText("Max spot price:");

        jLabel7.setText("Status:");

        instanceTypeLabel.setText("jLabel8");

        amiLabel.setText("jLabel8");

        nInstanceLabel.setText("jLabel8");

        maxSpotPriceLabel.setText("jLabel8");

        statusLabel.setText("jLabel8");

        networkNameLabel.setText("jLabel8");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusLabel)
                            .addComponent(maxSpotPriceLabel)
                            .addComponent(nInstanceLabel)
                            .addComponent(amiLabel)
                            .addComponent(instanceTypeLabel)
                            .addComponent(networkNameLabel)))
                    .addComponent(jLabel1))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(networkNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(instanceTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(amiLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(nInstanceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(maxSpotPriceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) throws Exception {
        GuiMetrics.init();
        SwingDefaults.setDefaults();
        SpotRequestGui srg = new SpotRequestGui("networkName",
                                                 "xlarge.c", // instanceType,
                                                  "zzzzz3z", // imageID,
                                                  6, // nInstances,
                                                  1.23 // spotPrice
                                                  );
        srg.createGui();
        srg.setStatus("status ehhhh");

        for ( int i = 0; i < 100; i++ ) {
            srg.setStatus("state " + Integer.toString(i));
            try { Thread.sleep(1000L); } catch(InterruptedException ix) {}
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel amiLabel;
    private javax.swing.JLabel instanceTypeLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel maxSpotPriceLabel;
    private javax.swing.JLabel nInstanceLabel;
    private javax.swing.JLabel networkNameLabel;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

}