/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.amazonaws.services.ec2.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.simpledb.model.*;
import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.util.aws.*;
import com.norbl.util.gui.*;
import java.net.*;
import java.util.*;
import javax.swing.table.*;


/** Used to view users accounts and relate info.  This main creates a gui
 *  app.  Using this app requires nbl permission.
 *
 * @author Barnet Wagman
 */
public class UserMonitorClient {
    
    ParamHtPPE paramHt;
    ParamsEc2 paramsEc2;
    
    AmazonEC2Client ec2Client;
    AmazonS3Client s3Client;
    S3Access s3a;
    SDBAccess sdba;
    
    String monitorHostName;
    UserTableModel userTableModel;
    
    UserMonitorClientFrame frame;       
    
    UserDb.El[] els = new UserDb.El[] {
        UserDb.El.uid, UserDb.El.status, UserDb.El.status_time,
        UserDb.El.n_instance_hours_unbilled, UserDb.El.n_instance_hours_billed        
    };
    
    public UserMonitorClient() throws Exception {
        
        ec2Client = UtilApps.getEc2ClientNBL();
        s3Client = UtilApps.getS3ClientNBL();
        s3a = new S3Access(s3Client);
        sdba = UtilApps.getSDBAccess();
    
        monitorHostName = MonitorHostname.getHostNameFromS3();
        userTableModel = new UserTableModel();
        
        (new Thread(new AccountUpdater())).start();
    }
    
    public static void main(String[] argv) throws Exception {
        UserMonitorClient umc = new UserMonitorClient();
        umc.launchGui();
    }
    
    public void launchGui() throws Exception {
        frame = new UserMonitorClientFrame(this);
        frame.launch();
        (new Thread(new Pinger())).start();
    }

    public List<AmiPPE> getAmiData() {        
        List<AmiPPE> amis = new ArrayList<AmiPPE>();
        
        for ( String gn : 
              new String[] {        
                "ppe-runjrun-ami-zc",
                "ppe-cloudrmpi-ami-zc"
              } ) {
        
            amis.addAll(AmiPPE.retrieveAndInitAmiPPEs(s3a,gn,ec2Client));
        }
        return(amis);
    }
    
    public List<Item> getUserAccounts() {        
        return( sdba.getAll(UserDb.SDB_DOMAIN).getItems() );       
    }
    
    class UserTableModel extends AbstractTableModel {
        
        List<Item> uas;
        
        UserTableModel() { super(); }
        
        void setUserAcounts(List<Item> uas) { 
            this.uas = uas; 
        }
        
        public int getColumnCount() {
            return(els.length);
        }
        
        public int getRowCount() {
           if ( uas != null ) return(uas.size());
           else return(0);           
        }
        
        public String getColumnName(int column) {
            return(els[column].toString());
        }
                      
        public Object getValueAt(int iRow, int iCol) {
            if ( uas == null ) return(null);
            if ( !(iRow < uas.size()) ) return(null);
            Item it = uas.get(iRow);
            
            UserDb.El el = els[iCol];
            
            String val = getVal(it,el);
            if ( val == null ) return(null);
            
            if ( el.equals(UserDb.El.status_time) ) return(toTime(val));
            else return(val);
        }
         
        private String getVal(Item it, UserDb.El el) {
            String elName = el.toString();
            List<Attribute> ats = it.getAttributes();
            for ( Attribute at : ats ) {
                if ( at.getName().equals(elName) ) return(at.getValue());
            }
            return(null);
        }   
        
        private String toTime(String t) {
            long val = Long.parseLong(t);
            return(TimeUtil.toDateTimeString(val));
        }
        
    }
    
    class AccountUpdater implements Runnable {
        
        public void run() {            
            for (;;) {
                List<Item> uas = getUserAccounts();
                userTableModel.setUserAcounts(uas);
                userTableModel.fireTableDataChanged();
                try { Thread.sleep(1000L * 10L); }
                catch(InterruptedException ix) {}
            }
            
        }
    }
    
    class Pinger implements Runnable {
        
        public void run() {
            try {
                for(;;) {
                    Socket socket = new Socket(monitorHostName,ConstantsUM.JPORT_DEFAULT);

                    ReadWriteConnection con =  
                        new ReadWriteConnection(socket,ReadWriteConnection.HostType.client);
                    Message m = new Message.TestMessage(
                                    TimeUtil.toDateTimeString(System.currentTimeMillis()));
                    con.writeMessage(m);
                    Message r = (Message) con.readMessage();
                    
                    if ( frame != null ) frame.setTime(r.toString());
                    
                    try { Thread.sleep(1000L * 10L); }
                    catch(InterruptedException ix) {}
                }
            }
            catch(Exception xxx) {
                GuiUtil.exceptionMessage(xxx);                
            }
            
        }
        
    }
}
