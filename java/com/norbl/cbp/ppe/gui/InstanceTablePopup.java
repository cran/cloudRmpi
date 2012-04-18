/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.gui;

import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import ch.ethz.ssh2.*;
import com.norbl.util.gui.*;
import com.norbl.util.ssh.*;

/** Creates the popup menu for an instance (a line in a network 
 *  instance table).
 *
 * @author Barnet Wagman
 */
public class InstanceTablePopup implements ActionListener {

    InstanceTableModel tableModel;
    JTable table;
    ClickHandler clickHandler;
    PPEManager ppeManager;
    EbsVolumeWrangler ebsVolumeWrangler;
    
    public InstanceTablePopup(JTable table, InstanceTableModel tableModel,
                              EbsVolumeWrangler ebsVolumeWrangler) {       
        this.table = table;
        this.tableModel = tableModel;                
        this.ebsVolumeWrangler = ebsVolumeWrangler;
        clickHandler = new ClickHandler();
        table.addMouseListener(clickHandler);  
    }

    class ClickHandler extends MouseAdapter {

        public void mousePressed(MouseEvent e) {                    
            (new CreatePopup(e)).start();
        }
    }

    private com.norbl.cbp.ppe.InstanceStatus instanceStatus;
    
    class CreatePopup extends Thread {

        MouseEvent e;
        
        CreatePopup(MouseEvent e) {
            this.e = e;            
        }
        
        public void run() {
            try {                
                int idx = table.rowAtPoint(e.getPoint());               
                if ( idx < 0 ) return;
                instanceStatus =  tableModel.getInstanceStatus(idx);           
                if ( instanceStatus == null ) return;
              
                java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    createPopup(e);
                }});              
            }            
            catch(Exception xxx) { ExceptionHandler.gui(xxx); }
            
        }
    }
    
    private enum PopUpCmd { publicDns, launchShell };
    
    void createPopup(MouseEvent e) {
       
        JPopupMenu menu = new JPopupMenu("xxxx");
        
        JMenuItem publicDnsMi = new JMenuItem("Public DNS");        
        publicDnsMi.setActionCommand(PopUpCmd.publicDns.toString());
        publicDnsMi.addActionListener(this);
        menu.add(publicDnsMi);
                
        JMenuItem shellMi = new JMenuItem("Launch shell for this instance");
        shellMi.setActionCommand(PopUpCmd.launchShell.toString());
        shellMi.addActionListener(this);
        menu.add(shellMi);
        
        JMenu attachVolumeMenu = new JMenu("Attach EBS volume");
        JMenu detachVolumeMenu = new JMenu("Detach EBS volume");
        
        menu.add(attachVolumeMenu);
        menu.add(detachVolumeMenu);
        
        ebsVolumeWrangler.createAttachMenu(attachVolumeMenu,instanceStatus);
        ebsVolumeWrangler.createDetachMenu(detachVolumeMenu,instanceStatus);                                           
        
        menu.show(e.getComponent(),e.getX(),e.getY());
    }    
    
    public void actionPerformed(ActionEvent ev) {
     
        if ( instanceStatus == null ) return;
        
        try {        
            PopUpCmd cmd = PopUpCmd.valueOf(ev.getActionCommand());           
            switch(cmd) {
                case publicDns: showDns(); break;
                case launchShell: launchShell(); break;            
                default:
            }
        }         
        catch(Exception xxx) {}
    }
    
    void showDns() {            
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {                                                
                new ShowPublicDNSFrame(instanceStatus);
            }
        });       
    }      
    
    void launchShell() {
        (new Thread(
            new Runnable() {
                public void run() {
                    try {
                        Connection con = Ssh.connect(instanceStatus.getPublicDnsName(),
                                                    ConstantsEc2.EC2_USERNAME,
                                                    PPEManager.paramsEc2.rsaKeyPairFile, 
                                                    1000L * 60L * 10L);

                        SshMinimalShell shell = new SshMinimalShell(con);        
                        shell.connect();        
                    }
                    catch(Exception xxx) { GuiUtil.exceptionMessage(xxx); }                    
                }
            })
         ).start();
    }
}