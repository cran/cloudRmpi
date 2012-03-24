/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ppe.gui;

import java.net.*;
import ppe.ec2.*;
import java.awt.event.*;
import javax.swing.event.*;
import nbl.utilj.*;
import ppe.*;
import java.io.*;

/** Used to enter or modify ec2 params that are stored in the config file
 * <tt>ec2-config</tt>.  (Note that {@link ParamsEc2} contains all the params
 * that are associated with launching and managing instances but most are not
 * in the config.  This class only handles that params that are stored.)<p>
 * 
 * This class sets contains all the information and text blocks to be displayed,
 * but does not create the gui.  That's done by {@link Ec2ParamEditorFrame}.
 *
 * @author moi
 */
public class Ec2ParamEditor 
    implements ActionListener, HyperlinkListener, WindowStateListener {
    
        // The following vars hold params while the are being entered.
        // They are not loaded into the paramsHt, etc. until 'ok' is hit.
    private String accountNumber;
    private String accessKeyID;
    private String secretAccessKey;
    private String rsaKeyName;
    private File rsaKeyPairFile;
    
    private PPEManager ppeManager;
    
    public enum EntryControl {
        accountNumber,
        accessKeyID,
        secretAccessKey,
        rsaKeyName,
        rsaKeyPairFile,
        chooseFile,
        cancel,
        ok
    }
    
    Ec2ParamEditorFrame edFrame;
    
    public boolean isRunning;
    
    public Ec2ParamEditor(PPEManager ppeManager) { // ParamHt ht) {
        isRunning = true;
        this.ppeManager = ppeManager;
        ParamHt ht = ppeManager.paramHt;
        if ( ht == null ) return;
            // Initialize values using extant params values.
        accountNumber = ht.getValue(ParamsEc2.ParamName.awsUserID.toString());        
        accessKeyID = ht.getValue(ParamsEc2.ParamName.awsAccessKey.toString());
        secretAccessKey = ht.getValue(ParamsEc2.ParamName.awsSecretKey.toString());
        rsaKeyName = ht.getValue(ParamsEc2.ParamName.keyName.toString());
        String rsaKeyPairPath = ht.getValue(ParamsEc2.ParamName.rsaKeyPairFile.toString());
        if ( rsaKeyPairPath != null )
            rsaKeyPairFile = new File(rsaKeyPairPath);           
    }
    
    public void create() {
                    
            // Launch the gui        
        edFrame = new Ec2ParamEditorFrame(this);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                edFrame.create();
                edFrame.addWindowStateListener(Ec2ParamEditor.this);
                GuiUtil.centerOnScreen(edFrame);
                initializeFields();
                edFrame.setVisible(true);
            }});
    }
    
    void initializeFields() {
        edFrame.setAccountNumber(accountNumber);
        edFrame.setAccessKeyID(accessKeyID);
        edFrame.setSecretKey(secretAccessKey);
        edFrame.setRsaKeyName(rsaKeyName);
        if ( rsaKeyPairFile != null )
            edFrame.setRsaKeyPairFile(rsaKeyPairFile.getPath());
    }
    
    public void actionPerformed(ActionEvent e) {
        
        String com = e.getActionCommand();
        EntryControl ec = EntryControl.valueOf(com);
        
        switch(ec) {       
            case chooseFile:
                chooseFile();
                break;  
            case cancel:
                edFrame.dispose();
                isRunning = false;
                break;
            case ok:                           
                if ( saveParams() ) {
                    edFrame.dispose();
                    ppeManager.updateParams();
                    isRunning = false;
                }
                break;
            default:
                handleParam(ec);
        }       
    }

    public void windowStateChanged(WindowEvent e) {
        int s = e.getNewState();
        if ( (s == WindowEvent.WINDOW_CLOSING) ||
             (s == WindowEvent.WINDOW_CLOSED) ) {
            isRunning = false;
        }
    }
        
    
    private boolean handleParam(EntryControl ec) {
        try {
            switch(ec) {
                case accountNumber:
                    parseAccountNumber();
                    break;
                case accessKeyID:
                    accessKeyID = edFrame.getAccessKeyID().trim();
                    checkString(accessKeyID,"Access Key ID");                      
                    break;
                case secretAccessKey:
                    secretAccessKey = edFrame.getSecretKey().trim();
                    break;
                case rsaKeyName:
                    rsaKeyName = edFrame.getRsaKeyName().trim();
                    checkString(rsaKeyName,"RSA key name");  
                    break;
                case rsaKeyPairFile:
                    String pn = edFrame.getRsaKeyPairFile().trim();
                    File f = new File(pn);
                    if ( AbstractParams.checkForReadFileAccess(f) )
                        rsaKeyPairFile = f;                  
                    else {
                        rsaKeyPairFile = null;
                        return(false);
                    }
//                        if ( f.exists() ) rsaKeyPairFile = f;
//                    else throw new BadParamException(new String[] { f.getPath(),"does not exist." },
//                                         "File not found");
                    break;                              
                default:                 
            }
            return(true);
        }
        catch(BadParamException x) {
            GuiUtil.warning(x.m,x.title);
            return(false);
        }        
    }
    
    private void checkString(String s, String name) throws BadParamException {
        if ( (s == null) || (s.length() < 1) )
            throw new BadParamException(
                new String[] { name + " is blank." },
                "Param error");
    }
    
    public void hyperlinkUpdate(HyperlinkEvent e) {
        try {
            if ( HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()) ) {
                URL url = e.getURL();
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url.toString()));          
            }
        }
        catch(Exception xx) { GuiUtil.exceptionMessage(xx); }
    }
    
    void parseAccountNumber() throws BadParamException {
        String s = edFrame.getAccountNumber().trim();
        StringBuilder b = new StringBuilder();
        for ( int i = 0; i < s.length(); i++ ) {
            char c = s.charAt(i);
            if ( Character.isDigit(c) ) {
                b.append(c);
            }
        }
        String ns = b.toString();
        if ( ns.length() == 12 ) accountNumber = ns;
        else throw new BadParamException(new String[] { s,
                "is not a valid account number. An account number must have twelve digits." },
                "Invalid account number");
        }
    
        void chooseFile() {
            File f = GuiUtil.getFile("Specify the RSA keypair file",
                                     new File(SysProp.user_home.getVal()));
            if ( f == null ) return;
            else if ( f.exists() && !f.isDirectory() ) {
                edFrame.setRsaKeyPairFile(f.getPath());
            }
            else GuiUtil.warning(new String[] { f.getPath() + 
                    " does not exist or is a directory." },
                    "Improper file");
    }
        
    boolean saveParams() {
            // Get all the values
        int nBad = 0;
        for ( EntryControl ec : EntryControl.values() ) {
            if ( !handleParam(ec) ) ++nBad;
        }
        if ( nBad > 0 )
            return( GuiUtil.answerIsYes(new String[] {
                      "There were errors in the params; exit without saving changes?" },
                       "Quit now?") );
        else { // params are ok (well, not obviously wrong).
            writeParamsToFile();      
                // Update the params ht
            try {
                ppeManager.paramHt = new ParamHt(new String[]{});   
                return(true);
            }
            catch(Exception ifx) {
                GuiUtil.exceptionMessage(ifx);
                return(false);
            }                       
        }                       
    }    
    
    void writeParamsToFile() {
        BufferedWriter b = null;
        try { 
            File backupFile = null; 
            File f = new File(ParamHt.getDefaultConfigFilePath());
            if ( f.exists() ) {
                backupFile = UtilPPE.getBackupFilename(f);
                if ( !f.renameTo(backupFile) ) {
                    GuiUtil.warning(new String[] {
                        "Unable to rename " + f.getPath() + "; changes were not saved." },
                         "File rename error");
                    return;
                }     
            }
                // Write the params
            b = new BufferedWriter(new FileWriter(f));
            b.write(ParamsEc2.ParamName.awsUserID.toString() + 
                    "=" + accountNumber + "\n");
            b.write(ParamsEc2.ParamName.awsAccessKey.toString() + 
                    "=" + accessKeyID + "\n");
            b.write(ParamsEc2.ParamName.awsSecretKey.toString() +
                    "=" + secretAccessKey + "\n");
            b.write(ParamsEc2.ParamName.keyName.toString() +
                    "=" + rsaKeyName + "\n");
            b.write(ParamsEc2.ParamName.rsaKeyPairFile.toString() + 
                    "=" + rsaKeyPairFile.getPath());
            b.flush();
            
            GuiUtil.info(new String[] {
                "Wrote Ec2 params to " + f.getPath(),
                (backupFile != null)?("The old version was saved as " + backupFile.getPath())
                                    :"" },
                "Updated config file");   
       }
       catch(IOException iox) { GuiUtil.exceptionMessage(iox); }
       finally {
            if ( b != null ) {
                try { 
                    b.flush();
                    b.close();
                }
                catch(IOException ox) { GuiUtil.exceptionMessage(ox); }               
            }
       }
    }
    
    class BadParamException extends Exception {

        String[] m;
        String title;
        
        public BadParamException(String[] m, String title) {
            super();
            this.m = m;
            this.title = title;
        }        
    }
    
        // --------- Text ------------------------------------------
    
    String intro =
            "<html><body>" +
            "<p>" +
            "Enter or modify Ec2 parameters. These parameters are " +
            "<i>required</i> for launching and managing Ec2 instances." +
            "</p>" +
            "<p>" +
            "These parameters will be stored in  " +
            "<tt>" + ParamHt.getDefaultConfigFilePath() + "</tt>. " +
            "If you prefer, you can edit this file directly." +
            "<p>" +
            "Note that this file will contain your AWS 'Secret Key' " +
            "<i>en claire</i>, so it is important that it be secure. " +
            "It will be created with read and write permisson granted to " +
            "the owner only (0600 on unix systems) and we recommend that you " +
            "maintain restricted access to this file." +
            "</body></html>";
    
    String accountKeyText =
            "<html><body>" +
            "<p> " +
            "The following three parameters can be obtained from your " +
            "Amazon account " +
            "<a href=\"https://aws-portal.amazon.com/gp/aws/developer/account/index.html" + 
            "?ie=UTF8&amp;action=access-key\">Security Credentials</a> page." +
             "</body></html>";
    
    String accountNumberText =           
            "This is a twelve digit number located near the top right of the 'Security Credentials' " +
            "page\n(among other places). You can enter it with or without the embedded dashes.";
    
    String accessKeyNotes =
            "The 'Access Key ID' and 'Secret Access Key' are in the  'Access Credentials' section of the\n" +
            "'Access Keys' tab on your Amazon Security Credentials page.";
    
    String rsaNotes =
            "<html><body>" +
            "<p>" +
            "Your key name is displayed in " +
            "<a href=\"https://console.aws.amazon.com/ec2/home\">AWS Management Console</a> " +
            "(EC2 tab, NETWORK & SECURITY -> Key Pair).</p>" +
            "<p>" +
            "Ec2 instances are accessed using an RSA keypair, which is created and registered using the" +
            "EC2/Keypair page of the AWS Management Console. (Note that Amazon does not keep a copy" +
            "of the private key, only the public key.) The 'RSA key name' is the name you assigned to this" +
            "keypair. The 'RSA keypair file' is the file that contains the keypair (downloaded from Amazon" +
            "when you created the keypair).</p>" +
            "</body></html>";
}
