/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Ec2ParamEditorFrame.java
 *
 * Created on Feb 7, 2012, 4:04:32 PM 
 */
package com.norbl.cbp.ppe.gui;

import com.norbl.util.gui.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.html.*;

/**
 *
 * @author moi
 */
public class Ec2ParamEditorFrame extends javax.swing.JFrame {

    Ec2ParamEditor ed;
    
    /** Creates new form Ec2ParamEditorFrame */
    public Ec2ParamEditorFrame(Ec2ParamEditor ed) {
        this.ed = ed;        
    }
    
    public void create() {
        SwingDefaults.setIcon(this);
        initComponents();
        configControl(accountNumberEntryField, Ec2ParamEditor.EntryControl.accountNumber);
        configControl(accessKeyIDEntryField,Ec2ParamEditor.EntryControl.accessKeyID);
        configControl(secretKeyIDAccessField, Ec2ParamEditor.EntryControl.secretAccessKey);
        configControl(rsaKeyNameEntryField, Ec2ParamEditor.EntryControl.rsaKeyName);
        configControl(rsaKeypairFileEntryField, Ec2ParamEditor.EntryControl.rsaKeyPairFile);
        configControl(chooseFileButton,Ec2ParamEditor.EntryControl.chooseFile);
        configControl(cancelButton,Ec2ParamEditor.EntryControl.cancel);
        configControl(okButton,Ec2ParamEditor.EntryControl.ok);
        
    }
    
    void setHtmlFont(JEditorPane jep) {
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";       
        ((HTMLDocument) jep.getDocument()).getStyleSheet().addRule(bodyRule);
    }
    
    public String getAccountNumber() { return(accountNumberEntryField.getText()); }    
    public String getAccessKeyID() { return(accessKeyIDEntryField.getText()); }
    public String getSecretKey() { return(secretKeyIDAccessField.getText()); }
    public String getRsaKeyName() { return(rsaKeyNameEntryField.getText()); }
    public String getRsaKeyPairFile() { return(rsaKeypairFileEntryField.getText()); }
    
    public void setAccountNumber(String s) { accountNumberEntryField.setText(s); }
    public void setAccessKeyID(String s) { accessKeyIDEntryField.setText(s); }
    public void setSecretKey(String s) { secretKeyIDAccessField.setText(s); }
    public void setRsaKeyName(String s) { rsaKeyNameEntryField.setText(s); }    
    public void setRsaKeyPairFile(String pathname) { rsaKeypairFileEntryField.setText(pathname); }
            
    void configControl(JTextField c,Ec2ParamEditor.EntryControl ec) {
        c.addActionListener(ed);
        c.setActionCommand(ec.toString());
    }
    
    void configControl(JButton b,Ec2ParamEditor.EntryControl ec) {
        b.addActionListener(ed);
        b.setActionCommand(ec.toString());
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
        jScrollPane1 = new javax.swing.JScrollPane();
        introJEP = new javax.swing.JEditorPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        accountKeyJEP = new javax.swing.JEditorPane();
        accountNumberLabel = new javax.swing.JLabel();
        accountNumberEntryField = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        accountNumberNotes = new javax.swing.JTextArea();
        accessKeyIDLabel = new javax.swing.JLabel();
        accessKeyIDEntryField = new javax.swing.JTextField();
        secretKeyIDAccessField = new javax.swing.JTextField();
        secretAccessKeyLabel = new javax.swing.JLabel();
        rsaKeypairFileEntryField = new javax.swing.JTextField();
        rsaKeypairFileLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        rsaKeyNameLabel = new javax.swing.JLabel();
        rsaKeyNameEntryField = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        rsaNotes = new javax.swing.JEditorPane();
        chooseFileButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        titleLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 16)); // NOI18N
        titleLabel.setText("Required Ec2 Parameters");

        jScrollPane1.setBorder(null);

        introJEP.setContentType("text/html");
        introJEP.setEditable(false);
        introJEP.setText(ed.intro);
        setHtmlFont(introJEP);
        jScrollPane1.setViewportView(introJEP);

        jScrollPane2.setBorder(null);

        accountKeyJEP.setBorder(null);
        accountKeyJEP.setContentType("text/html");
        accountKeyJEP.setEditable(false);
        accountKeyJEP.setText(ed.accountKeyText);
        setHtmlFont(accountKeyJEP);
        accountKeyJEP.addHyperlinkListener(ed);
        jScrollPane2.setViewportView(accountKeyJEP);

        accountNumberLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        accountNumberLabel.setText("Account number");

        accountNumberEntryField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountNumberEntryFieldActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(null);

        accountNumberNotes.setColumns(20);
        accountNumberNotes.setEditable(false);
        accountNumberNotes.setRows(2);
        accountNumberNotes.setText(ed.accountNumberText);
        jScrollPane3.setViewportView(accountNumberNotes);

        accessKeyIDLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        accessKeyIDLabel.setText("Access Key ID");

        accessKeyIDEntryField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accessKeyIDEntryFieldActionPerformed(evt);
            }
        });

        secretAccessKeyLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        secretAccessKeyLabel.setText("Secret Access Key");

        rsaKeypairFileLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        rsaKeypairFileLabel.setText("RSA keypair file");

        okButton.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        okButton.setText("Ok");

        cancelButton.setText("Cancel");

        rsaKeyNameLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        rsaKeyNameLabel.setText("RSA key name");

        rsaKeyNameEntryField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rsaKeyNameEntryFieldActionPerformed(evt);
            }
        });

        jScrollPane4.setBorder(null);

        rsaNotes.setBorder(null);
        rsaNotes.setContentType("text/html");
        rsaNotes.setEditable(false);
        rsaNotes.setText(ed.rsaNotes);
        setHtmlFont(rsaNotes);
        rsaNotes.addHyperlinkListener(ed);
        jScrollPane4.setViewportView(rsaNotes);

        chooseFileButton.setText("Choose file");
        chooseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 809, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 809, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rsaKeyNameLabel)
                            .addComponent(rsaKeypairFileLabel)
                            .addComponent(chooseFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(accessKeyIDLabel)
                            .addComponent(accountNumberLabel)
                            .addComponent(secretAccessKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                            .addComponent(rsaKeypairFileEntryField, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                            .addComponent(accountNumberEntryField, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                            .addComponent(rsaKeyNameEntryField, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                            .addComponent(accessKeyIDEntryField, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                            .addComponent(secretKeyIDAccessField)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(titleLabel)
                .addGap(28, 28, 28)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountNumberLabel)
                    .addComponent(accountNumberEntryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accessKeyIDEntryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(accessKeyIDLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(secretKeyIDAccessField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(secretAccessKeyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rsaKeyNameEntryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rsaKeyNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rsaKeypairFileEntryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rsaKeypairFileLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chooseFileButton)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void accountNumberEntryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountNumberEntryFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_accountNumberEntryFieldActionPerformed

    private void accessKeyIDEntryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accessKeyIDEntryFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_accessKeyIDEntryFieldActionPerformed

    private void rsaKeyNameEntryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rsaKeyNameEntryFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rsaKeyNameEntryFieldActionPerformed

    private void chooseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFileButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chooseFileButtonActionPerformed

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) throws Exception {
//        GuiMetrics.init();
//        SwingDefaults.setDefaults();
//        ppe.ParamHt paramHt = new ppe.ParamHt(new String[] {});
//        Ec2ParamEditor ed = new Ec2ParamEditor(paramHt);
//        ed.create();
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(Ec2ParamEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(Ec2ParamEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(Ec2ParamEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(Ec2ParamEditorFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>

        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//                Ec2ParamEditor ed = new Ec2ParamEditor();
//                new Ec2ParamEditorFrame(ed).setVisible(true);
//            }
//        });
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField accessKeyIDEntryField;
    private javax.swing.JLabel accessKeyIDLabel;
    private javax.swing.JEditorPane accountKeyJEP;
    private javax.swing.JTextField accountNumberEntryField;
    private javax.swing.JLabel accountNumberLabel;
    private javax.swing.JTextArea accountNumberNotes;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton chooseFileButton;
    private javax.swing.JEditorPane introJEP;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField rsaKeyNameEntryField;
    private javax.swing.JLabel rsaKeyNameLabel;
    private javax.swing.JTextField rsaKeypairFileEntryField;
    private javax.swing.JLabel rsaKeypairFileLabel;
    private javax.swing.JEditorPane rsaNotes;
    private javax.swing.JLabel secretAccessKeyLabel;
    private javax.swing.JTextField secretKeyIDAccessField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}