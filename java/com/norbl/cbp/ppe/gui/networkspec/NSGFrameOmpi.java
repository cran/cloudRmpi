/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.cbp.ppe.gui.networkspec;

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.ompi.*;
import com.norbl.util.gui.*;

/**
 *
 * @author moi
 */
public class NSGFrameOmpi extends javax.swing.JFrame {

    NetworkSpecGui nsg;
    
    /**
     * Creates new form NSGFrame1
     */
    public NSGFrameOmpi(NetworkSpecGui nsg) {
        
        this.nsg = nsg;
        
        // Text choices (that are not in ChoiceSets) are created in
        // initComponents 'custom code':
        // nsg.networkNameChoice, nsg.slotsPerHostChoice, 
        // nsg.spotPriceChoice, nsg.nInstancesChoice                
        
        initComponents();
        
        instanceTypeChoiceMenu.build(nsg.instanceTypeChoices, nsg);        
        amiChoiceMenu.build(nsg.amiChoices, nsg);
        if (amiChoiceMenu.otherCC != null ) 
            otherAmiPan.add(amiChoiceMenu.otherCC.getComponent()); 
        
        azChoiceMenu.build(nsg.availabilityZoneChoices,nsg);        
        sgChoiceMenu.build(nsg.securityGroupChoices,nsg);        
        keypairChoiceMenu.build(nsg.keyPairsChoices, nsg);
        
        cancelButton.addActionListener(nsg);
        cancelButton.setActionCommand(NetworkSpecGui.CANCEL);
        
        continueButton.addActionListener(nsg);
        continueButton.setActionCommand(NetworkSpecGui.FINISH);
        
        SwingDefaults.setIcon(this);
        GuiUtil.centerOnScreen(this);       
        this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        framePan = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        instanceTypePan = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        instanceTypeChoiceMenu = new com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu();
        amiPan = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        amiChoiceMenu = new com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu();
        otherAmiPan = new javax.swing.JPanel();
        availabilityZonePan = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        azChoiceMenu = new com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu();
        securityGroupPan = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        sgChoiceMenu = new com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu();
        keyPairPan = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        keypairChoiceMenu = new com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu();
        textEntryPan = new javax.swing.JPanel();
        networkNameCC = (LabeledTextFieldCC) nsg.networkNameChoice.getComponent();
        slotsPerHostCC = (LabeledTextFieldCC) nsg.slotsPerHostChoice.getComponent();
        spotPriceCC = (SpotPriceCC) nsg.spotPriceChoice.getComponent();
        numberInstancesCC = (LabeledTextFieldCC) nsg.nInstancesChoice.getComponent();
        cancelButton = new javax.swing.JButton();
        continueButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        framePan.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        titleLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 20)); // NOI18N
        titleLabel.setText("Specify ec2 network");

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        jLabel1.setText("Instance types");

        javax.swing.GroupLayout instanceTypeChoiceMenuLayout = new javax.swing.GroupLayout(instanceTypeChoiceMenu);
        instanceTypeChoiceMenu.setLayout(instanceTypeChoiceMenuLayout);
        instanceTypeChoiceMenuLayout.setHorizontalGroup(
            instanceTypeChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        instanceTypeChoiceMenuLayout.setVerticalGroup(
            instanceTypeChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 269, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout instanceTypePanLayout = new javax.swing.GroupLayout(instanceTypePan);
        instanceTypePan.setLayout(instanceTypePanLayout);
        instanceTypePanLayout.setHorizontalGroup(
            instanceTypePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(instanceTypePanLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 273, Short.MAX_VALUE))
            .addComponent(instanceTypeChoiceMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        instanceTypePanLayout.setVerticalGroup(
            instanceTypePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(instanceTypePanLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(instanceTypeChoiceMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        jLabel2.setText("AMIs");

        javax.swing.GroupLayout amiChoiceMenuLayout = new javax.swing.GroupLayout(amiChoiceMenu);
        amiChoiceMenu.setLayout(amiChoiceMenuLayout);
        amiChoiceMenuLayout.setHorizontalGroup(
            amiChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 244, Short.MAX_VALUE)
        );
        amiChoiceMenuLayout.setVerticalGroup(
            amiChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        otherAmiPan.setLayout(new javax.swing.BoxLayout(otherAmiPan, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout amiPanLayout = new javax.swing.GroupLayout(amiPan);
        amiPan.setLayout(amiPanLayout);
        amiPanLayout.setHorizontalGroup(
            amiPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(otherAmiPan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel2)
            .addComponent(amiChoiceMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        amiPanLayout.setVerticalGroup(
            amiPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(amiPanLayout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(amiChoiceMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(otherAmiPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel3.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        jLabel3.setText("Availability zone");

        javax.swing.GroupLayout azChoiceMenuLayout = new javax.swing.GroupLayout(azChoiceMenu);
        azChoiceMenu.setLayout(azChoiceMenuLayout);
        azChoiceMenuLayout.setHorizontalGroup(
            azChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        azChoiceMenuLayout.setVerticalGroup(
            azChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 141, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout availabilityZonePanLayout = new javax.swing.GroupLayout(availabilityZonePan);
        availabilityZonePan.setLayout(availabilityZonePanLayout);
        availabilityZonePanLayout.setHorizontalGroup(
            availabilityZonePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availabilityZonePanLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 14, Short.MAX_VALUE))
            .addComponent(azChoiceMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        availabilityZonePanLayout.setVerticalGroup(
            availabilityZonePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availabilityZonePanLayout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(azChoiceMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        jLabel4.setText("Security group");

        javax.swing.GroupLayout sgChoiceMenuLayout = new javax.swing.GroupLayout(sgChoiceMenu);
        sgChoiceMenu.setLayout(sgChoiceMenuLayout);
        sgChoiceMenuLayout.setHorizontalGroup(
            sgChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 202, Short.MAX_VALUE)
        );
        sgChoiceMenuLayout.setVerticalGroup(
            sgChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 67, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout securityGroupPanLayout = new javax.swing.GroupLayout(securityGroupPan);
        securityGroupPan.setLayout(securityGroupPanLayout);
        securityGroupPanLayout.setHorizontalGroup(
            securityGroupPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel4)
            .addComponent(sgChoiceMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        securityGroupPanLayout.setVerticalGroup(
            securityGroupPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(securityGroupPanLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sgChoiceMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel5.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        jLabel5.setText("Keypair");

        javax.swing.GroupLayout keypairChoiceMenuLayout = new javax.swing.GroupLayout(keypairChoiceMenu);
        keypairChoiceMenu.setLayout(keypairChoiceMenuLayout);
        keypairChoiceMenuLayout.setHorizontalGroup(
            keypairChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 203, Short.MAX_VALUE)
        );
        keypairChoiceMenuLayout.setVerticalGroup(
            keypairChoiceMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 92, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout keyPairPanLayout = new javax.swing.GroupLayout(keyPairPan);
        keyPairPan.setLayout(keyPairPanLayout);
        keyPairPanLayout.setHorizontalGroup(
            keyPairPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(keyPairPanLayout.createSequentialGroup()
                .addGroup(keyPairPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(keypairChoiceMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        keyPairPanLayout.setVerticalGroup(
            keyPairPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(keyPairPanLayout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(keypairChoiceMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        spotPriceCC.setLayout(new javax.swing.BoxLayout(spotPriceCC, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout textEntryPanLayout = new javax.swing.GroupLayout(textEntryPan);
        textEntryPan.setLayout(textEntryPanLayout);
        textEntryPanLayout.setHorizontalGroup(
            textEntryPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textEntryPanLayout.createSequentialGroup()
                .addGroup(textEntryPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(slotsPerHostCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numberInstancesCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(networkNameCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spotPriceCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        textEntryPanLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {networkNameCC, numberInstancesCC, slotsPerHostCC, spotPriceCC});

        textEntryPanLayout.setVerticalGroup(
            textEntryPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(textEntryPanLayout.createSequentialGroup()
                .addComponent(networkNameCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(slotsPerHostCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(spotPriceCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(numberInstancesCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        textEntryPanLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {networkNameCC, numberInstancesCC, slotsPerHostCC, spotPriceCC});

        cancelButton.setText("Cancel");

        continueButton.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        continueButton.setText("Continue");
        continueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                continueButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout framePanLayout = new javax.swing.GroupLayout(framePan);
        framePan.setLayout(framePanLayout);
        framePanLayout.setHorizontalGroup(
            framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(framePanLayout.createSequentialGroup()
                .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(framePanLayout.createSequentialGroup()
                        .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(framePanLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(cancelButton))
                            .addGroup(framePanLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(framePanLayout.createSequentialGroup()
                                        .addComponent(availabilityZonePan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(12, 12, 12)
                                        .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(securityGroupPan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(keyPairPan, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)))
                                    .addComponent(instanceTypePan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(amiPan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(textEntryPan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(continueButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(framePanLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(titleLabel)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        framePanLayout.setVerticalGroup(
            framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(framePanLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(titleLabel)
                .addGap(50, 50, 50)
                .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(framePanLayout.createSequentialGroup()
                        .addComponent(amiPan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(continueButton))
                    .addGroup(framePanLayout.createSequentialGroup()
                        .addComponent(instanceTypePan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32)
                        .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(framePanLayout.createSequentialGroup()
                                .addComponent(availabilityZonePan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                                .addComponent(cancelButton))
                            .addGroup(framePanLayout.createSequentialGroup()
                                .addGroup(framePanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(textEntryPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(framePanLayout.createSequentialGroup()
                                        .addComponent(securityGroupPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(41, 41, 41)
                                        .addComponent(keyPairPan, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(framePan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(framePan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void continueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_continueButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_continueButtonActionPerformed

    /** For testing only.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NSGFrameOmpi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NSGFrameOmpi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NSGFrameOmpi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NSGFrameOmpi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//                new NSGFrame1().setVisible(true);
//            }
//        });
        try {
            GuiMetrics.init();
            SwingDefaults.setDefaults();
        
            Region ec2Region = new Region();
            ec2Region.setEndpoint(OmpiPPEApp.US_EAST_ENDPOINT);        

            OmpiPPEManager m = new OmpiPPEManager(new String[] {},
                                                "ppe-ompi-ami-zc",                                                
                                                ec2Region);

            NetworkSpecGui g = new NetworkSpecGui(m,
                                                  AmiChoiceOmpi.class,
                                                  NSGFrameOmpi.class,
                                                 "test-network");
            g.fillSpec();
        }        
        catch(Exception x) { throw new RuntimeException(x); }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu amiChoiceMenu;
    private javax.swing.JPanel amiPan;
    private javax.swing.JPanel availabilityZonePan;
    private com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu azChoiceMenu;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton continueButton;
    private javax.swing.JPanel framePan;
    private com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu instanceTypeChoiceMenu;
    private javax.swing.JPanel instanceTypePan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel keyPairPan;
    private com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu keypairChoiceMenu;
    private com.norbl.cbp.ppe.gui.networkspec.LabeledTextFieldCC networkNameCC;
    private com.norbl.cbp.ppe.gui.networkspec.LabeledTextFieldCC numberInstancesCC;
    private javax.swing.JPanel otherAmiPan;
    private javax.swing.JPanel securityGroupPan;
    private com.norbl.cbp.ppe.gui.networkspec.ChoiceMenu sgChoiceMenu;
    private com.norbl.cbp.ppe.gui.networkspec.LabeledTextFieldCC slotsPerHostCC;
    private com.norbl.cbp.ppe.gui.networkspec.SpotPriceCC spotPriceCC;
    private javax.swing.JPanel textEntryPan;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
