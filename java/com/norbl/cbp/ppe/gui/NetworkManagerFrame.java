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

package com.norbl.cbp.ppe.gui;

import com.norbl.util.gui.*;
import com.norbl.cbp.ppe.*;
import javax.swing.*;
import java.awt.event.*;

/**
 *
 * @author Barnet Wagman
 */
public class NetworkManagerFrame extends javax.swing.JFrame 
    implements ActionListener {

    private enum Cmd {
        createNetwork, update, exit
    }

    PPEManager manager;
    NetworkTablePopup networkTablePopup;

    private WevHandler wevh;

    private boolean disposeCalled;

    String title;
    
    private JMenuItem billingIDMI;

        /** Creates new form NetworkManagerFrame */
    public NetworkManagerFrame(PPEManager manager, String title) {
        this.manager = manager;        
        this.title = title;
        manager.setNetworkManagerFrame(this);
        wevh = new WevHandler();
        this.addWindowListener(wevh);
        disposeCalled = false;
    }

    public JTabbedPane getInstanceTableTabPane() {
        return(instanceTableTabPane);
    }

        /** Must be called from the event dispatching thread.
         *
         */
    public void startGui() {

        this.setTitle(title);

        GuiMetrics.init();
        
        initComponents();       
        
            // ---------- Create op menu items ---------------------
        
        setupMI(createNetworkMI,PPEManager.Op.createNetwork);
        setupMI(updateMI,PPEManager.Op.updateNetworkInfo);
        
            // Volumes menu
        setupMI(listEbsVolumesMI,PPEManager.Op.listEbsVolumes);
        setupMI(updateEbsVolumesMI,PPEManager.Op.updateEbsVolumeList);           
        
            // Edit menu
        setupMI(ec2ParamsMI,PPEManager.Op.editEc2Parameters);
        setupMI(awsClientParams,PPEManager.Op.editAwsClientParameters);
        
            // Account menu       
        setupMI(authorizeInstanceBillingMI,PPEManager.Op.authorizeInstanceBilling);
        setupMI(cancelAuthorizationMI,PPEManager.Op.cancelInstanceBilling);
        billingIDMI = new JMenuItem();
        setDisplayedBillingID(manager.getBillingIDForDisplay());
        showIDMenu.add(billingIDMI);
        
        setupMI(aboutMI,PPEManager.Op.showAbout);
        setupMI(exitMI,PPEManager.Op.exit);       
        
            // Help menu
        setupMI(amiHelpMI,PPEManager.Op.showAmiWebpage);
        setupMI(manualMI,PPEManager.Op.showManualWebpage);
            
            // ------------------------------------------------------
        
        networkTable.setModel(manager.getNetworkTableModel());
        networkTable.setDefaultRenderer(JLabel.class, new XRenderer());
               
        networkTablePopup = 
            new NetworkTablePopup(manager,this,networkTable);

        manager.instanceTableManager.setTabbedPane(instanceTableTabPane);
       
        SwingDefaults.setIcon(this);
        
        this.setVisible(true);       
    }
    
    public void setDisplayedBillingID(String text) {
        billingIDMI.setText(text);
    }   

    class XRenderer extends javax.swing.table.DefaultTableCellRenderer {
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
            if ( value instanceof JLabel ) return((JLabel) value);
            else return( super.getTableCellRendererComponent(table, value, isSelected,
                                                             hasFocus, row, column) );
        }
    }

    void setupMI(JMenuItem mi, PPEManager.Op op) {
        mi.addActionListener(this);       
        mi.setActionCommand(
            new ActionCommandNetworkManager(op.toString(),null).toActionEventString());
        mi.setText(op.textMi);
    }

    class WevHandler extends WindowAdapter {        
        public void windowClosing(WindowEvent e) {        
            if ( !disposeCalled)
                (new Thread() { public void run() { manager.exit(); }}).start();
        }
        public void windowClosed(WindowEvent e) {           
            if ( !disposeCalled)
                (new Thread() { public void run() { manager.exit(); }}).start();
        }
    }

    public void disposeOfWindow() {
        if ( !disposeCalled ) {
            this.dispose();
            disposeCalled = true;
        }
    }
    
    /** Each action event contains an {@link ActionCommand} which is
     *  used to invoke an 'op' method in teh {@link PPEManager}.
     * 
     * @param ev 
     */
    public void actionPerformed(ActionEvent ev) {      
        manager.doOp(new ActionCommandNetworkManager(ev));        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPan = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        networkScrollPane = new javax.swing.JScrollPane();
        networkTable = new javax.swing.JTable();
        instanceTableTabPane = new javax.swing.JTabbedPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        networksMenu = new javax.swing.JMenu();
        createNetworkMI = new javax.swing.JMenuItem();
        updateMenu = new javax.swing.JMenu();
        updateMI = new javax.swing.JMenuItem();
        ebsVolumeMenu = new javax.swing.JMenu();
        listEbsVolumesMI = new javax.swing.JMenuItem();
        updateEbsVolumesMI = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        ec2ParamsMI = new javax.swing.JMenuItem();
        awsClientParams = new javax.swing.JMenuItem();
        accountMenu = new javax.swing.JMenu();
        authorizeInstanceBillingMI = new javax.swing.JMenuItem();
        cancelAuthorizationMI = new javax.swing.JMenuItem();
        showIDMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        amiHelpMI = new javax.swing.JMenuItem();
        manualMI = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenu();
        aboutMI = new javax.swing.JMenuItem();
        exitMenu = new javax.swing.JMenu();
        exitMI = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        mainPan.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 0, 0));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 22)); // NOI18N
        jLabel1.setText("ec2 network manager");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/norbl/cbp/ppe/gui/nbl_logo.jpg"))); // NOI18N

        networkTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        networkTable.setCellSelectionEnabled(true);
        networkScrollPane.setViewportView(networkTable);

        javax.swing.GroupLayout mainPanLayout = new javax.swing.GroupLayout(mainPan);
        mainPan.setLayout(mainPanLayout);
        mainPanLayout.setHorizontalGroup(
            mainPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 703, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addContainerGap())
            .addComponent(networkScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1046, Short.MAX_VALUE)
            .addComponent(instanceTableTabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1046, Short.MAX_VALUE)
        );
        mainPanLayout.setVerticalGroup(
            mainPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(38, 38, 38)
                .addComponent(networkScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(instanceTableTabPane, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE))
        );

        networksMenu.setText("Networks");

        createNetworkMI.setText("Create network");
        networksMenu.add(createNetworkMI);

        jMenuBar1.add(networksMenu);

        updateMenu.setText("Update");

        updateMI.setText("Update");
        updateMenu.add(updateMI);

        jMenuBar1.add(updateMenu);

        ebsVolumeMenu.setText("Volumes");

        listEbsVolumesMI.setText("jMenuItem1");
        ebsVolumeMenu.add(listEbsVolumesMI);

        updateEbsVolumesMI.setText("jMenuItem1");
        ebsVolumeMenu.add(updateEbsVolumesMI);

        jMenuBar1.add(ebsVolumeMenu);

        editMenu.setText("Edit");

        ec2ParamsMI.setText("jMenuItem1");
        editMenu.add(ec2ParamsMI);

        awsClientParams.setText("jMenuItem2");
        editMenu.add(awsClientParams);

        jMenuBar1.add(editMenu);

        accountMenu.setText("Account");

        authorizeInstanceBillingMI.setText("jMenuItem1");
        accountMenu.add(authorizeInstanceBillingMI);

        cancelAuthorizationMI.setText("jMenuItem1");
        accountMenu.add(cancelAuthorizationMI);

        showIDMenu.setText("Show billing ID");
        accountMenu.add(showIDMenu);

        jMenuBar1.add(accountMenu);

        helpMenu.setText("Help");

        amiHelpMI.setText("jMenuItem1");
        helpMenu.add(amiHelpMI);

        manualMI.setText("jMenuItem1");
        helpMenu.add(manualMI);

        jMenuBar1.add(helpMenu);

        aboutMenu.setText("About");

        aboutMI.setText("About");
        aboutMenu.add(aboutMI);

        jMenuBar1.add(aboutMenu);

        exitMenu.setText("Exit");

        exitMI.setText("Exit");
        exitMenu.add(exitMI);

        jMenuBar1.add(Box.createHorizontalGlue());

        jMenuBar1.add(exitMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {

        final NetworkManagerFrame f = new NetworkManagerFrame(null,"");

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                f.startGui();
                f.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMI;
    private javax.swing.JMenu aboutMenu;
    private javax.swing.JMenu accountMenu;
    private javax.swing.JMenuItem amiHelpMI;
    private javax.swing.JMenuItem authorizeInstanceBillingMI;
    private javax.swing.JMenuItem awsClientParams;
    private javax.swing.JMenuItem cancelAuthorizationMI;
    private javax.swing.JMenuItem createNetworkMI;
    private javax.swing.JMenu ebsVolumeMenu;
    private javax.swing.JMenuItem ec2ParamsMI;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMI;
    private javax.swing.JMenu exitMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JTabbedPane instanceTableTabPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem listEbsVolumesMI;
    private javax.swing.JPanel mainPan;
    private javax.swing.JMenuItem manualMI;
    private javax.swing.JScrollPane networkScrollPane;
    private javax.swing.JTable networkTable;
    private javax.swing.JMenu networksMenu;
    private javax.swing.JMenu showIDMenu;
    private javax.swing.JMenuItem updateEbsVolumesMI;
    private javax.swing.JMenuItem updateMI;
    private javax.swing.JMenu updateMenu;
    // End of variables declaration//GEN-END:variables

}
