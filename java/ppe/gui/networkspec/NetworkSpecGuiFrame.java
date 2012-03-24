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

package ppe.gui.networkspec;

import ppe.gui.*;
import java.awt.event.*;
import javax.swing.*;
import nbl.utilj.*;

/** Standard height for JTextFields is 26.
 *
 * @author Barnet Wagman
 */
public class NetworkSpecGuiFrame extends javax.swing.JFrame 
    implements ActionListener {

    static java.awt.Font TITLE_FONT = new java.awt.Font("Arial", 1, 13);

    public enum CmdType { choice,                         
                          cancel, forward,
                          nil };
    NetworkSpecGui spec;

    SpecErrorFrame sef;

    boolean notDone;

    /** Creates new form NFX1 */
    public NetworkSpecGuiFrame(NetworkSpecGui spec) {
        this.spec = spec;
        notDone = true;
    }

    public void create() {  
        
        GuiMetrics.init();
        SwingDefaults.setIcon(this);
        
            // Create components that need to be assigned to frame variables
            // in initComponents().
        spec.nInstancesChoice.createCC();
        spec.networkNameChoice.createCC();
        spec.slotsPerHostChoice.createCC();
        spec.spotPriceChoice.createCC();       
     
            // ^ These must precede initComponents, because they
            //   create JPanels that are used in initComponents().
        initComponents();           
        
            // Setup the non-menu components
        setupChoice(spec.nInstancesChoice);
        setupChoice(spec.networkNameChoice);
        setupChoice(spec.slotsPerHostChoice);
        setupChoice(spec.spotPriceChoice);
        
            // Build the choice menus
        int nr0 = Math.max(spec.instanceTypeChoices.size(),
                           spec.amiChoices.size());
        instanceTypeRBMenu.build(spec.instanceTypeChoices, this,nr0);                               
        amiRBMenu.build(spec.amiChoices, this,nr0);

        buildZoneMenu();
        
        int nr1 = Math.max(spec.securityGroupChoices.size(),
                           spec.keyPairsChoices.size());
        securityGroupRBMenu.build(spec.securityGroupChoices,this,nr1);
        keypairRBMenu.build(spec.keyPairsChoices, this,nr1);
        
            // Configure buttons
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(
                (new AC(CmdType.cancel,null,null,null)).toString());

        continueButton.addActionListener(this);
        continueButton.setActionCommand(
                (new AC(CmdType.forward,null,null,null)).toString());
        
            // Set fonts
        instanceTypeTitle.setFont(TITLE_FONT);
        amiTitle.setFont(TITLE_FONT);
        securityGroupTitle.setFont(TITLE_FONT);
        keypairTitle.setFont(TITLE_FONT);        
              
        GuiUtil.centerOnScreen(this);        
        this.setVisible(true);
        this.setAlwaysOnTop(true);
    }

    /** This is in a function because it can be called by other methods 
     *  if the zone menu needs to be changed.
     */
    void buildZoneMenu() {
        zoneRBMenu.build(spec.availabilityZoneChoices,this,
                         spec.availabilityZoneChoices.size());
        setVisible(true);
    }

    void setupChoice(Choice c) {
        c.cc.addActionListener(this);
        Object val = c.getValue();
        c.cc.setActionCommand(
            new NetworkSpecGuiFrame.AC(
                    NetworkSpecGuiFrame.CmdType.choice,
                    c,
                    c.getLabel(),
                    (val != null)?val.toString():"null").toString());
        c.cc.setEnabled(true);
        c.setSelected(false);
    }
    
    static public class AC {
        CmdType cmdType;
        String choiceType;
        String label;
        String value;

        public AC(CmdType cmdType, Choice choice, String label, String value) {
            this.cmdType = cmdType;
            if ( choice != null )
                this.choiceType = choice.getChoiceType();
            this.label = label;
            this.value = value;
        }
        
        public AC(ActionEvent ev) {
            this(ev.getActionCommand());
        }
        public AC(String cmd) {
            String[] a = cmd.split("#");
            cmdType = CmdType.valueOf(a[0]);
            choiceType = nonEmptyOrNull(a[1]);
            if ( a.length > 2 )
                label = nonEmptyOrNull(a[2]);
            if ( a.length > 3 )
                value = nonEmptyOrNull(a[3]);
            else value = null;
        }

        public String toString() {
            return( cmdType.toString() + "#" + ssn(choiceType) +
                    "#" + ssn(label) + "#" + ssn(value) );
        }

        private String ssn(String x) {
            if ( ( x == null) || (x.trim().length() < 0) ) return(" ");
            else return(x);
        }

        private String nonEmptyOrNull(String x) {
            if ( x == null ) return(x);
            else if ( x.trim().length() < 1 ) return(null);
            else return(x);
        }
    }

    private AC ac;
    public void actionPerformed(ActionEvent ev) {

        ac = new AC(ev);
       
        switch(ac.cmdType) {
            case choice:
                (new Thread() { public void run() {
                    spec.updateRe(ac.choiceType,ac.label); } }).start();
                break;            
            case cancel:
                spec.specComplete = false;
                notDone = false;
                this.dispose();
                break;
            case forward:
                if ( sef != null ) sef.dispose();
                (new Thread() { public void run() {
                    String message = spec.checkSpec();
                    if ( message != null )
                        (new SpecErrorFrame(message)).create();
                    else
                        (new GetOkToProceed()).create();
                } }).start();
                break;
            case nil:
            default:
        }      
    }

    private static String OTP_CANCEL = "Cancel";
    private static String OTP_MODIFY_SPEC = "Modify spec";
    private static String OTP_GO = "Create the network";

    class GetOkToProceed {

        public void create() {
            try {
            NetworkSpecGuiFrame.this.setVisible(false);
            if ( sef != null ) sef.dispose();
            int idx =
                JOptionPane.showOptionDialog(null,
                                            spec.toHtmlString(),
                                            "Create ec2 network",
                                            JOptionPane.YES_NO_CANCEL_OPTION,
                                            JOptionPane.PLAIN_MESSAGE,
                                            null,
                                            new Object[] {
                                                OTP_CANCEL, OTP_MODIFY_SPEC, OTP_GO
                                            },
                                            null);
            if ( idx == 0 ) {
                NetworkSpecGuiFrame.this.dispose();
                spec.specComplete = false;
                notDone = false;
            }
            else if ( idx == 1 ) {
                NetworkSpecGuiFrame.this.setVisible(true);
            }
            else if ( idx == 2 ) {
                spec.specComplete = true;
                notDone = false;
            }
            else throw new RuntimeException("idx=" + idx);
            }
            catch(Exception xxx) {
                ppe.ExceptionHandler.gui(xxx);
                NetworkSpecGuiFrame.this.dispose();
                spec.specComplete = false;
                notDone = false;
            }
        }        
    }

    static String buildActionCommand(String ID,String value) {
        return( ID + "#" + value);
    }
    static String getActionCommandID(String ac) {
        return( ac.split("#")[0] );
    }
    static String getActionCommandValue(String ac) {
        return( ac.split("#")[1] );
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
        instanceTypeTitle = new javax.swing.JLabel();
        instanceTypeRBMenu = new ppe.gui.networkspec.ChoiceMenu();
        amiTitle = new javax.swing.JLabel();
        amiRBMenu = new ppe.gui.networkspec.ChoiceMenu();
        zoneTitle = new javax.swing.JLabel();
        zoneRBMenu = new ppe.gui.networkspec.ChoiceMenu();
        securityGroupTitle = new javax.swing.JLabel();
        securityGroupRBMenu = new ppe.gui.networkspec.ChoiceMenu();
        keypairTitle = new javax.swing.JLabel();
        keypairRBMenu = new ppe.gui.networkspec.ChoiceMenu();
        slotsPerHostPan = (LabeledTextFieldCC) spec.slotsPerHostChoice.getComponent();
        nInstancePan = (LabeledTextFieldCC) spec.nInstancesChoice.getComponent();
        networkNamePan = (LabeledTextFieldCC) spec.networkNameChoice.getComponent();
        spotPricePan = (JPanel) spec.spotPriceChoice.getComponent();
        cancelButton = new javax.swing.JButton();
        continueButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        titleLabel.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        titleLabel.setText("Specify ec2 network");

        instanceTypeTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        instanceTypeTitle.setText("Instance types");

        javax.swing.GroupLayout instanceTypeRBMenuLayout = new javax.swing.GroupLayout(instanceTypeRBMenu);
        instanceTypeRBMenu.setLayout(instanceTypeRBMenuLayout);
        instanceTypeRBMenuLayout.setHorizontalGroup(
            instanceTypeRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        instanceTypeRBMenuLayout.setVerticalGroup(
            instanceTypeRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        amiTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        amiTitle.setText("AMI ID");

        javax.swing.GroupLayout amiRBMenuLayout = new javax.swing.GroupLayout(amiRBMenu);
        amiRBMenu.setLayout(amiRBMenuLayout);
        amiRBMenuLayout.setHorizontalGroup(
            amiRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 118, Short.MAX_VALUE)
        );
        amiRBMenuLayout.setVerticalGroup(
            amiRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 170, Short.MAX_VALUE)
        );

        zoneTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        zoneTitle.setText("Availability zone");

        javax.swing.GroupLayout zoneRBMenuLayout = new javax.swing.GroupLayout(zoneRBMenu);
        zoneRBMenu.setLayout(zoneRBMenuLayout);
        zoneRBMenuLayout.setHorizontalGroup(
            zoneRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );
        zoneRBMenuLayout.setVerticalGroup(
            zoneRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 190, Short.MAX_VALUE)
        );

        securityGroupTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        securityGroupTitle.setText("Security group");

        javax.swing.GroupLayout securityGroupRBMenuLayout = new javax.swing.GroupLayout(securityGroupRBMenu);
        securityGroupRBMenu.setLayout(securityGroupRBMenuLayout);
        securityGroupRBMenuLayout.setHorizontalGroup(
            securityGroupRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 190, Short.MAX_VALUE)
        );
        securityGroupRBMenuLayout.setVerticalGroup(
            securityGroupRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        keypairTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        keypairTitle.setText("Keypair");

        javax.swing.GroupLayout keypairRBMenuLayout = new javax.swing.GroupLayout(keypairRBMenu);
        keypairRBMenu.setLayout(keypairRBMenuLayout);
        keypairRBMenuLayout.setHorizontalGroup(
            keypairRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 170, Short.MAX_VALUE)
        );
        keypairRBMenuLayout.setVerticalGroup(
            keypairRBMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        networkNamePan.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                networkNamePanComponentHidden(evt);
            }
        });

        spotPricePan.setLayout(new javax.swing.BoxLayout(spotPricePan, javax.swing.BoxLayout.LINE_AXIS));

        cancelButton.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        cancelButton.setText("Cancel");

        continueButton.setFont(new java.awt.Font("Arial", 1, 13)); // NOI18N
        continueButton.setText("Continue");
        continueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                continueButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(networkNamePan, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nInstancePan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(spotPricePan, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(slotsPerHostPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titleLabel)
                    .addComponent(cancelButton)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(instanceTypeRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(instanceTypeTitle)
                            .addComponent(securityGroupTitle))
                        .addGap(86, 86, 86)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keypairTitle)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(continueButton)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(amiTitle)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(zoneTitle))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(amiRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(71, 71, 71)
                                        .addComponent(zoneRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(keypairRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(securityGroupRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(titleLabel)
                        .addGap(29, 29, 29)
                        .addComponent(instanceTypeTitle))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zoneTitle)
                            .addComponent(amiTitle))))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(instanceTypeRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(amiRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(securityGroupTitle)
                            .addComponent(keypairTitle)))
                    .addComponent(zoneRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(securityGroupRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(slotsPerHostPan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(keypairRBMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(nInstancePan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(spotPricePan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16)
                .addComponent(networkNamePan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(continueButton))
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void continueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_continueButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_continueButtonActionPerformed

    private void networkNamePanComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_networkNamePanComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_networkNamePanComponentHidden

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) throws Exception {

//        mpiec2.Params params = new mpiec2.Params(new String[0]);
//        Ec2Wrangler w = new Ec2Wrangler(params);
//        final NetworkSpec spec = new NetworkSpec(w);
//
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new NetworkSpecGuiFrame(spec).setVisible(true);
//            }
//        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ppe.gui.networkspec.ChoiceMenu amiRBMenu;
    private javax.swing.JLabel amiTitle;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton continueButton;
    private ppe.gui.networkspec.ChoiceMenu instanceTypeRBMenu;
    private javax.swing.JLabel instanceTypeTitle;
    private javax.swing.JPanel jPanel1;
    private ppe.gui.networkspec.ChoiceMenu keypairRBMenu;
    private javax.swing.JLabel keypairTitle;
    private ppe.gui.networkspec.LabeledTextFieldCC nInstancePan;
    private ppe.gui.networkspec.LabeledTextFieldCC networkNamePan;
    private ppe.gui.networkspec.ChoiceMenu securityGroupRBMenu;
    private javax.swing.JLabel securityGroupTitle;
    private ppe.gui.networkspec.LabeledTextFieldCC slotsPerHostPan;
    private javax.swing.JPanel spotPricePan;
    private javax.swing.JLabel titleLabel;
    private ppe.gui.networkspec.ChoiceMenu zoneRBMenu;
    private javax.swing.JLabel zoneTitle;
    // End of variables declaration//GEN-END:variables

}
