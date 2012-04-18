/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe;

import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;
import com.norbl.util.gui.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;
import javax.swing.*;

/**
 *
 * @author Barnet Wagman
 */
public class EbsVolumeWrangler {

    AmazonEC2Client aec;
    public List<Volume> volumes;
    VolumeTableModel vtm;
    

    public EbsVolumeWrangler(AmazonEC2Client aec) {
        this.aec = aec;
    }
    
    public void retrieveVolumeInfo() {              
        volumes = aec.describeVolumes().getVolumes(); 
        if ( vtm != null ) vtm.fireTableDataChanged();
    }
    
    public boolean attachVolume(String instanceID,
                                String volumeID,
                                String device) {
        try {
            AttachVolumeRequest req = 
                    new AttachVolumeRequest(volumeID, instanceID, device);
            aec.attachVolume(req);
            return(true);
        }
        catch(Exception ix) {
            GuiUtil.exceptionMessageOnly(ix);
            return(false);
        }        
    }
    
    public boolean detachVolume(String volumeID) {
        try {
            DetachVolumeRequest req = new DetachVolumeRequest(volumeID);
            aec.detachVolume(req);
            return(true);
        }
        catch(Exception ix) {
            GuiUtil.exceptionMessageOnly(ix);
            return(false);
        }        
    }
    
    public List<Volume> getDetachableVolumes(String availabilityZone) {
        List<Volume> vs = new ArrayList<Volume>();
        if ( volumes == null ) return(vs);
        NEXT: for ( Volume v : volumes ) {            
            if ( v.getAvailabilityZone().equals(availabilityZone) &&
                 isInUse(v) && !isRootVolume(v) ) {
                vs.add(v);
            }
        }
        return(vs);
    }
    
    public List<Volume> getAttachableVolumes(String availabilityZone) {
        List<Volume> vs = new ArrayList<Volume>();
        if ( volumes == null ) return(vs);
        for ( Volume v : volumes ) {            
            if ( v.getAvailabilityZone().equals(availabilityZone) &&
                 isAvailable(v) ) {
                vs.add(v);
            }
        }
        return(vs);
    }
         
    public String getDevice(Volume v) {      
        if ( !isInUse(v) ) return("");
        List<VolumeAttachment> vas = v.getAttachments();
        for ( VolumeAttachment va : vas ) {
            String d = va.getDevice();           
            if ( (d != null) && (d.length() > 0) ) return(d);
        }
        return("");
    }
    
    private boolean isRootVolume(Volume v) {
        
        if ( isAvailable(v) ) return(false);
        List<VolumeAttachment> vas = v.getAttachments();
        for ( VolumeAttachment va : vas ) {
            if ( va.getDevice().startsWith("/dev/sda") ) return(true);
        }
        return(false);
    }
    
    private boolean isInUse(Volume v) {
        return( v.getState().equals("in-use") );
    }
    
    private boolean isAvailable(Volume v) {
        return( v.getState().equals("available") );
    }
    
    public TableModel getTableModel() {
        if (vtm == null) vtm = new VolumeTableModel();
        return(vtm);
    }
 
    enum Col {
        volumeID("Volume ID"),
        availabilityZone("Availablity zone"),
        state("State"),
        size("Size GB"),
        createTime("Create time"),
        mountDevice("Device"),
        tags("Tags");
        
        String title;
        Col(String title) { this.title = title; }
    }
    
    public class VolumeTableModel extends AbstractTableModel {
        
        public int getColumnCount() {
            return(Col.values().length);
        }
        
        public String getColumnName(int iCol) {
            return(Col.values()[iCol].title);
        }

        public int getRowCount() {
            if ( volumes != null ) return(volumes.size());
            else return(0);
        }

        public Object getValueAt(int iRow, int iCol) {

            if ( (volumes == null) || (volumes.size() < 1) ) return(null);
            Volume v = volumes.get(iRow);
            Col c = Col.values()[iCol];

            switch(c) {
                case volumeID: return(v.getVolumeId());
                case availabilityZone: return(v.getAvailabilityZone());
                case state: return(v.getState());
                case size: return(v.getSize());
                case createTime: return(v.getCreateTime());
                case mountDevice: return(getDevice(v));
                case tags: return(tagsToString(v.getTags()).trim());
                default: return(null);
            }       
        }    
    }    
    
    public void createAttachMenu(JMenu parent,
                                 com.norbl.cbp.ppe.InstanceStatus instanceStatus) {
        (new Attacher(parent, instanceStatus)).go();                 
    }
    
    public void createDetachMenu(JMenu parent,
                                 com.norbl.cbp.ppe.InstanceStatus instanceStatus) {
        (new Detacher(parent, instanceStatus)).go();                 
    }
    
    abstract class Tacher {
        
        String az;
        List<Volume> vs;
        JMenuItem parentMi;
        com.norbl.cbp.ppe.InstanceStatus instanceStatus;
        
        Tacher(JMenuItem parentMi,
               com.norbl.cbp.ppe.InstanceStatus instanceStatus) {            
            this.parentMi = parentMi;
            this.instanceStatus = instanceStatus;
            az = instanceStatus.instance.getPlacement().getAvailabilityZone();            
        }
        
        abstract void go();        
    }
    
    class Attacher extends Tacher implements ActionListener {
        
        Attacher(JMenuItem parentMi,
                 com.norbl.cbp.ppe.InstanceStatus instanceStatus) {
            super(parentMi,instanceStatus);
        }
     
        void go() {           
            
            List<Volume> vs = getAttachableVolumes(az);
            if ( vs.size() < 1 ) {
                JMenuItem mi = 
                new JMenuItem("No attachable volumes in " + az);
                mi.setEnabled(true);    
                parentMi.add(mi);
                return;
            }
            
            for (Volume v : vs ) {
                JMenuItem mi = new JMenuItem();
                mi.setText(
                    (v.getVolumeId() + " " + 
                    v.getState() +  " " +    
                    az + " " + 
                    tagsToString(v.getTags())).trim());
                mi.setActionCommand(v.getVolumeId());
                mi.addActionListener(this);
                mi.setEnabled( v.getState().equals("available") );
                parentMi.add(mi);
            }       
            
        }
        
        public void actionPerformed(ActionEvent ev) {
            Volume v = getVolume(ev.getActionCommand());
            if ( v == null ) return;
            
            String instanceID = instanceStatus.instance.getInstanceId();
            
            String dev = okToAttach(instanceID,v);
            if ( dev != null ) {
                attachVolume(instanceID, v.getVolumeId(), dev);                            
                retrieveVolumeInfo();
            }
        }
    }
    
    class Detacher extends Tacher implements ActionListener {
        
        Detacher(JMenuItem parentMi,
                 com.norbl.cbp.ppe.InstanceStatus instanceStatus) {
            super(parentMi,instanceStatus);
        }
     
        void go() {            
            List<Volume> vs = getDetachableVolumes(az);
            if ( vs.size() < 1 ) {
                JMenuItem mi = 
                new JMenuItem("No detachable volumes in " + az);
                mi.setEnabled(true);    
                parentMi.add(mi);
                return;
            }
            
            for (Volume v : vs ) {
                JMenuItem mi = new JMenuItem();
                mi.setText(
                    (v.getVolumeId() + " " + 
                    v.getState() +  " " +    
                    az + " " +     
                    tagsToString(v.getTags())).trim());
                mi.setActionCommand(v.getVolumeId());
                mi.addActionListener(this);
                mi.setEnabled( v.getState().equals("in-use") );
                parentMi.add(mi);
            }       
            
        }
        
        public void actionPerformed(ActionEvent ev) {
            Volume v = getVolume(ev.getActionCommand());
            if ( v == null ) return;
            
            String instanceID = instanceStatus.instance.getInstanceId();
            
            if ( okToDetach(instanceID,v) ) {
                detachVolume(v.getVolumeId()); 
                retrieveVolumeInfo();
            }
        }
    }
    
    private static String tagsToString(List<Tag> tags) {
        StringBuilder s = new StringBuilder("");
        for ( Tag t : tags ) {
            s.append(t.getValue() + " ");            
        }
        return(s.toString().trim());
    }
    
    private Volume getVolume(String ID) {
        if (volumes == null) return(null);
        for ( Volume v : volumes ) {
            if ( v.getVolumeId().equals(ID) ) return(v);
        }
        return(null);
    }
    
    String okToAttach(String instanceID,Volume v) {
        
        String s = (String) JOptionPane.showInputDialog(
           GuiUtil.getIconFrame(),
           getAttachMessage(instanceID,v),
           "Attach volume",
           JOptionPane.OK_CANCEL_OPTION,
           null, // icon
           null, // selection values
           "/dev/sdf"                
         );
       
         if ( s == null ) return(null);
         else if ( s.length() > 2 ) return(s);
         else {
             if ( GuiUtil.answerIsYes(new String[] {
                 "'" + s + "' is not a valid device name.",
                 " ",
                 "Try again?"}, "Invalid device name") ) {
                 return(okToAttach(instanceID, v));
             }
             else return(null);
         }
    }
    
    boolean okToDetach(String instanceID,Volume v) {
        
        int x = JOptionPane.showConfirmDialog(
                        GuiUtil.getIconFrame(),
                        getDetachMessage(instanceID,v),
                        "Detach volume",       
                        JOptionPane.OK_CANCEL_OPTION
        );                
        return(x == JOptionPane.OK_OPTION);
    }
    
    private String[] getAttachMessage(String instanceID, Volume v) {
        return(new String[] {
            " ",
            "Attach volume " + v.getVolumeId() + 
                " to instance " + instanceID + "?",
            " ",
            "Specify the device below. AWS documentation recommends",
            "using /dev/sd[f-p].",
            "  ",
            "After attaching the volume, you will still need to mount it",
            "(and create a filesystem, if it is a newly created volume).",
            "You can launch a simple ssh shell from the instance's popup",
            "menu to issue these commands or use regular ssh externally.", 
            "  ",
            "Specify device:",
            " "
        });
    }  
    
     private String[] getDetachMessage(String instanceID, Volume v) {
        return(new String[] {
            " ",
            "Detach volume " + v.getVolumeId() + 
                " from instance " + instanceID + "?",
            " ",
            "Be sure that the device has been unmounted before detaching it.",
            "Note that the device will be umounted and detached when the ",
            "instance is terminated, so detaching it here is not usually",
            "necessary.",            
            " ",
            "Detaching a volume may take a little time (typically 10 to 20",
            "seconds).  You can use the AWS Management Console to check the",
            "status of your volumes."
           
        });
    }     
}
