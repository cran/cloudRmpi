/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.gui.networkspec;

import com.amazonaws.services.ec2.model.*;
import com.norbl.cbp.ppe.*;
import com.norbl.util.*;
import com.norbl.cbp.ppe.gui.*;
import com.norbl.cbp.ppe.usermonitor.*;
import com.norbl.util.gui.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.lang.reflect.*;

/** Gathers info and provides logic for entering a network spec.
 *  The actual gui is drawn by {@link NetworkSpecGuiFrame}; this
 *  class provides all the logic.<p>
 * 
 *  Usage:
 *  <ul>
 *  <li>The constructor creates the initial choices that will be
 *  displayed for each param to be specified in the gui.  This includes
 *  disabling certain choices.</li>
 *  <li>The {@link #fillSpec() } methods is then called to create
 *  the gui in a separate thread.  This method blocks until 
 *  the user submits or cancels the spec.</li>
 *  <li>This class is the gui's action handler.
 *      <ul><li>When the user makes a choice, an action is fired that
 *          invokes the {@link #updateRe()} method.  This method
 *          may check the users's input (for some params) and
 *          resets the 'enable' state of all choices based on which
 *          choices are now feasible and/or relevant.  E.g choosing
 *          an instance type limits the feasible amis.</li>
 *          <li>When the user enters ok, parameters are checked.  If ok.
 *              the spec is checked, and if ok user gets the final
 *              approval dialog.</li>
 *          <li>If the user entes 'cancel' the spec gui is disposed of.
 *          </li></ul>
 * </li></ul>
 * @author Barnet Wagman
 */
public class NetworkSpecGui implements ActionListener {
    
    public enum CmdType { choice,                         
                          cancel, forward,
                          nil  };
    
    public static final String CANCEL = "cancel";
    public static final String FINISH = "finish";
    
    PPEManager ppeManager;
    Class amiChoiceClass;
    Class guiFrameClass;
    Constructor guiFrameCons;
    Constructor amiChoiceConstructor;
    String networkName;
    
    List<String> keypairNames;
    
    JFrame networkSpecFrame;
    
    private SpecErrorFrame sef;
    
    private boolean notDone;
    private boolean specComplete;
    
    // --------- The choices -------------------------
    
    // Choices fall into two categories: multiple choice and 'fill in'.
    
        // Multiple choice: handled by choiceSets.  Note that the choiceSet
        // object maintains a 'selected' choice, retreivable with the
        // getSelected() method.  This will return null if there no
        // selection has been made yet.
    
    ChoiceSet instanceTypeChoices;
    ChoiceSet amiChoices;
    ChoiceSet availabilityZoneChoices;
    ChoiceSet securityGroupChoices;
    ChoiceSet keyPairsChoices;
    
        // Fill in: these choices create JPanels directly.
    
    SpotPriceChoice spotPriceChoice;
    NInstancesChoice nInstancesChoice;
    NetworkNameChoice networkNameChoice;
    SlotsPerHostChoice slotsPerHostChoice;
    
    // -------------------------------------------------
    
    /** Each choice in the gui has a unique ID.  When an action is
     *  fired, the action command is given it's ID, which is then
     *  retrieved from this ht.
     * 
     */
    private HashMap<String,Choice> actionChoiceHt;
    private static int lastIDNumber = -1;
    
    public NetworkSpecGui(PPEManager ppeManager,
                          Class amiChoiceClass,
                          Class guiFrameClass,
                          String networkName) throws Exception {
        
        this.ppeManager = ppeManager;
        this.amiChoiceClass = amiChoiceClass;
        this.networkName = networkName;                
        
        amiChoiceConstructor =
            amiChoiceClass.getConstructor(new Class[] {AmiPPE.class});
        
        guiFrameCons = guiFrameClass.getConstructor(new Class[] {NetworkSpecGui.class} );
        
        keypairNames = ppeManager.ec2w.getKeypairNames();
        
        notDone = true;
        
        createChoices();
    }
    
    /** {@link #fillSpec()} launches the network spec gui in a 
     *  separate thread. It then sleeps until params have been
     *  entered.
     * 
     * @return 
     */
    public boolean fillSpec() {

        specComplete = false;
               
        java.awt.EventQueue.invokeLater(new Runnable() { public void run() {
            try {
                networkSpecFrame = (JFrame)
                    guiFrameCons.newInstance(new Object[] { NetworkSpecGui.this } );    
            }
            catch(Exception x) { GuiUtil.exceptionMessage(x.getCause()); }
            finally { InProgressFrame.end(); }
        }});
        
        while ( notDone ) {
             try { Thread.sleep(500L); }
             catch(InterruptedException ix) {}
        }
        return(specComplete);
    }
    
    public NetworkSpec choicesToNetworkSpec() {
        
        NetworkSpec s = new NetworkSpec();

        InstanceTypeChoice instanceType = (InstanceTypeChoice)
                                instanceTypeChoices.getSelected();
        if ( instanceType != null )
            s.instanceType = 
                instanceType.instanceType.instanceType; // (String) instanceType.getValue();

        AmiChoice ami = (AmiChoice) amiChoices.getSelected();        
        if ( ami != null ) s.imageID = (String) ami.getValue();

        AvailabilityZoneChoice az =
                (AvailabilityZoneChoice) availabilityZoneChoices.getSelected();
        if ( az != null ) s.availabilityZone = az.getLabel();
        else s.availabilityZone = AvailabilityZoneChoice.ANY_ZONE;

        SecurityGroupChoice sg = (SecurityGroupChoice)
                            securityGroupChoices.getSelected();
        if ( sg != null ) s.securityGroupName = (String) sg.getValue();

        KeyPairChoice kp = (KeyPairChoice) keyPairsChoices.getSelected();
        if ( kp != null ) s.keyName = (String) kp.getValue();

        if ( spotPriceChoice.selected ) {
            s.useSpotInstances = true;
            s.spotPrice = spotPriceChoice.getPrice();
        }
        else s.useSpotInstances = false;

        s.nInstances = nInstancesChoice.getNInstances();

        s.networkName = (String) networkNameChoice.getValue();
        
        s.slotsPerHost = slotsPerHostChoice.getNSlots();         
        
        String uid = ppeManager.paramsEc2.uid;
        if ( uid == null )
            uid = ppeManager.paramHt.get(ParamsEc2.ParamName.uid.toString());
        if ( uid == null ) uid = ConstantsUM.NIL_USER_DATA;    
            
        s.userData = uid;
        
        return(s);
    }
    
        // --------------------------------------------------------

    private void createChoices() throws Exception {
        
        actionChoiceHt = new HashMap<String, Choice>();
    
        instanceTypeChoices = new ChoiceSet();
        /* D */ System.out.println("# NetworkSpecGui # " +
                    "ppeMan? " + (ppeManager == null) + " " +
                " instanceTypes? " + (ppeManager.instanceTypes == null));
        for ( Ec2InstanceType eit : ppeManager.instanceTypes ) {                             
            instanceTypeChoices.add(register(new InstanceTypeChoice(eit),
                                             instanceTypeChoices));
        }
        
        amiChoices = new ChoiceSet();
        for ( AmiPPE ami : ppeManager.amis ) {                        
            AmiChoice c = createAmiChoice(ami);
            c.setEnabled(ami.isUsableReBilling(ppeManager.paramsEc2));            
            amiChoices.add(register(c,amiChoices));
        }
                        
        amiChoices.add(register(createAmiChoice(null),amiChoices)); // Creates 'other'
        
        availabilityZoneChoices = new ChoiceSet();
        availabilityZoneChoices.add(register(new AvailabilityZoneChoice(null),
                                             availabilityZoneChoices));
        for ( AvailabilityZone az : ppeManager.availabilityZones ) {           
            availabilityZoneChoices.add(register(new AvailabilityZoneChoice(az),
                                                 availabilityZoneChoices));
        }
        
        securityGroupChoices = new ChoiceSet();
        securityGroupChoices.add(
            register(new SecurityGroupChoice(SecurityGroupChoice.Type.defaultGroup),
                     securityGroupChoices));
        securityGroupChoices.add(
            register(new SecurityGroupChoice(SecurityGroupChoice.Type.other),
                     securityGroupChoices));
        
        keyPairsChoices = new ChoiceSet();
        for ( String kp : keypairNames ) {
            keyPairsChoices.add(register(new KeyPairChoice(kp),keyPairsChoices));
        }
        keyPairsChoices.add(register(new KeyPairChoice(),keyPairsChoices));
        
        spotPriceChoice = (SpotPriceChoice) setup(new SpotPriceChoice(),true);
        nInstancesChoice = (NInstancesChoice) setup(new NInstancesChoice());
        slotsPerHostChoice = (SlotsPerHostChoice) setup(new SlotsPerHostChoice());
        
        if ( networkName != null )
            networkNameChoice = (NetworkNameChoice) 
                                setup(new NetworkNameChoice(networkName));
        else networkNameChoice = (NetworkNameChoice) 
                                  setup(new NetworkNameChoice(
                                           NiM.createUniqueDefaultNetworkName()));
    }
    
    private AmiChoice createAmiChoice(AmiPPE ami) throws Exception {       
        return((AmiChoice)
                amiChoiceConstructor.newInstance(new Object[] {ami}));                   
    }
    
    private String nextChoiceID() {
        int x = ++lastIDNumber;
        return("ac_" + Integer.toString(x));
    }
    
    /** Needed for Choices that are not in a ChoiceSet/ChoiceMenu
     * 
     * @param c
     * @return 
     */
    private Choice setup(Choice c,boolean setLabelTextBold) {
        c.createCC();
        if ( (c.cc instanceof LabeledTextFieldCC) && setLabelTextBold ) {
            ((LabeledTextFieldCC) c.cc).setLabelTextBold();
        }
        c.cc.addActionListener(this);
        c = register(c,null); // register() mu stbe called to create ID before setting the ac
        c.cc.setActionCommand(c.ID);       
        return(c);
    }
    
    private Choice setup(Choice c) { return(setup(c,false)); }
    
    private Choice register(Choice c, ChoiceSet parent) {
        c.setParentChoiceSet(parent);
        c.setID(nextChoiceID());
        actionChoiceHt.put(c.ID,c);
        return(c);
    }
    
    private void updateRe(Choice cx) {
        
        if ( cx.parentChoiceSet != null ) { 
            for ( Choice c : cx.parentChoiceSet ) c.setSelected(false);
        }
        cx.setSelected(true);
            
        if ( cx instanceof InstanceTypeChoice ) {                    
            imposeConformityReInstanceTypeChoice(
                                        ((InstanceTypeChoice) cx));     
            slotsPerHostChoice.setNSlots(
                ((InstanceTypeChoice) cx).instanceType.nCores);
        }
        // Note that this class gets the region and availabbility 
        // zones from PPEManager.  Since all ami are in the same region
        // all zones will work.
        
        if ( cx instanceof SpotPriceChoice ) {
            SpotPriceChoice sc = (SpotPriceChoice) cx;
            sc.setSelected(sc.cc.isSelected());
        }
    }
    
    public void actionPerformed(ActionEvent ev) {         
        (new Thread(new ActionRunner(ev.getActionCommand()))).start();
    }
   
    private class ActionRunner implements Runnable {
        String cmd;
        
        ActionRunner(String cmd) { this.cmd = cmd; }
        
        public void run() {
            
            if ( sef != null ) sef.dispose();
            
            if ( cmd.equals(CANCEL) ) {
                networkSpecFrame.dispose();
                specComplete = false;
                notDone = false;
            }
            else if ( cmd.equals(FINISH) ) {
                String message = checkSpec();
                if ( message != null ) {                    
                    if ( sef != null ) sef.dispose();;
                    sef = new SpecErrorFrame(message);
                    sef.create();                  
                }                    
                else getOkToProceed();            
            }
            else {
                Choice c = actionChoiceHt.get(cmd);                   
                if ( c != null ) updateRe(c); 
            }
        }
    }
    
    private static String OTP_CANCEL = "Cancel";
    private static String OTP_MODIFY_SPEC = "Modify spec";
    private static String OTP_GO = "Create the network";
    
    private void getOkToProceed() {
        networkSpecFrame.setVisible(false);
        int idx = JOptionPane.showOptionDialog(networkSpecFrame,
                                            toHtmlString(),
                                            "Create ec2 network",
                                            JOptionPane.YES_NO_CANCEL_OPTION,
                                            JOptionPane.PLAIN_MESSAGE,
                                            null,
                                            new Object[] {
                                                OTP_CANCEL, OTP_MODIFY_SPEC, OTP_GO
                                            },
                                            null);
        if ( idx == 0 ) {
            specComplete = false;      
            notDone = false;
            networkSpecFrame.dispose();            
        }
        else if ( idx == 1 ) {
            networkSpecFrame.setVisible(true);
            notDone = true;
        }
        else if ( idx == 2 ) {
            specComplete = true;
            notDone = false;
            networkSpecFrame.dispose();
        }
        else {
            specComplete = false;
            notDone = false;
            Exception x  = new Exception("idx=" + idx);
            ExceptionHandler.display(x);
            networkSpecFrame.dispose();
        }
    }        
    
        /** Checks whether the network has been fully specified.
         *
         * @return null if the network is fully specified, or
         *  a message indicating what's missing.
         */
    private String checkSpec() {
        List<String> missing = new ArrayList<String>();
        List<String> invalid = new ArrayList<String>();

        InstanceTypeChoice instanceType = (InstanceTypeChoice)
                                instanceTypeChoices.getSelected();
        if ( instanceType == null ) missing.add("Instance type");

        AmiChoice ami = (AmiChoice) amiChoices.getSelected();
        if ( ami == null ) missing.add("AMI ID");

        SecurityGroupChoice sg = (SecurityGroupChoice)
                            securityGroupChoices.getSelected();
        if ( sg == null ) missing.add("Security group");

        KeyPairChoice kp = (KeyPairChoice) keyPairsChoices.getSelected();
        if ( (kp == null) || (kp.getValue() == null) )
                missing.add("Keypair");

        if ( spotPriceChoice.selected ) {
            String x = (String) spotPriceChoice.getValue();            
            if ( isEmpty(x) ) missing.add("Spot price");
            else if ( isInvalidPrice(x) )
                invalid.add("'" + x + "' is not a valid price.");
        }

        String ni = (String) nInstancesChoice.getValue();
        if ( isEmpty(ni) ) missing.add("n instances");
        else if ( isInvalidNInstances(ni) )
            invalid.add("'" + ni + "' is not a valid number of instances.");

        String networkName = (String) networkNameChoice.getValue();
        if ( isEmpty(networkName) ) missing.add("Network name");

        if ( slotsPerHostChoice.getValue() == null )
            missing.add("Slots per host");

        if ( (missing.size() < 1) && (invalid.size() < 1) )
            return(null);
        else {
            StringBuilder s = new StringBuilder("<html>\n");
            if ( missing.size() > 0 ) {
                s.append("The following required parameters were not specified:" +
                         "<ul>\n");
                for ( String m : missing ) {
                    s.append("<li>" + m + "</li>\n");
                }
                s.append("</ul>\n");
            }
            if ( invalid.size() > 0 ) {
                s.append("<br>");
                s.append("Invalid values:<ul>\n");
                for ( String i : invalid ) {
                    s.append("<li>" + i + "</li>\n");
                }
                s.append("</ul>\n");
            }
            s.append("</html>");
            return(s.toString());
        }
    }
    
    private boolean isEmpty(String s) {
        return( (s == null) || (s.trim().length() < 1) );
    }

    private boolean isInvalidPrice(String s) {
        try {
            double p = Double.parseDouble(s);
            return(p <= 0.0);
        }
        catch(NumberFormatException nfx) {
            return(true);
        }
    }

    private boolean isInvalidNInstances(String s) {
        try {
            double n = Integer.parseInt(s);
            return(n <= 0);
        }
        catch(NumberFormatException nfx) {
            return(true);
        }
    }
            
    private String toHtmlString() {
        NetworkSpec ns = choicesToNetworkSpec();
        if ( !ns.isComplete() )
            throw new RuntimeException("Incomplete NetworkSpec:<br>" +
                                       ns.listMissingParams());
        
        StringBuilder s = new StringBuilder("<html>" +
                "<b>ec2 network specification:</b><br><ul>");
               
        s.append("<li>Instance type:  " + ns.instanceType + "</li>");
        s.append("<li>AMI ID:  " + ns.imageID + "</li>");
        s.append("<li>Availability zone: " + ns.availabilityZone + "</li");
        s.append("<li>Security group:  " + ns.securityGroupName + "</li>");
        s.append("<li>Keypair:  " + ns.keyName + "</li>");
        if ( ns.useSpotInstances )
            s.append("<li>Spot price:  " + ns.spotPrice + "</li>");
        s.append("<li>N instances:  " + ns.nInstances + "</li>");
        s.append("<li>Slots per host:  " + ns.slotsPerHost + "</li>");

        s.append("</ul><br><b>Please note that if you proceed you will incur charges " +
                 "from Amazon for ec2 instances.</b><br><br></html>");

        return(s.toString());
    }
        
    private void imposeConformityReInstanceTypeChoice(InstanceTypeChoice c) {             
        if ( !c.cc.getRadioButton().isSelected() ) return;       
        if ( c.instanceType.isHvm() ) imposeHvmConformity(c);
        else if ( c.instanceType.isPv() ) imposeParavirtualConformity(c);
        else throw new RuntimeException("Bad instance type=" +
                c.instanceType.instanceType.toString() + " " +
                c.instanceType.vt);
    }
    
    private void imposeHvmConformity(InstanceTypeChoice selected) {       
      
        NEXT: for ( Choice c : amiChoices ) {            
            AmiChoice a = (AmiChoice) c;
            if ( a.ami == null ) continue NEXT; // 'other'
            if ( !a.ami.isUsableReBilling(PPEManager.paramsEc2) 
                 ||               
                 a.usesParavirtual() ) {               
                a.cc.setSelected(false);
                a.cc.setEnabled(false);                
            }
            else {               
                a.cc.setEnabled(true);
            }
        }
    }

    private void imposeParavirtualConformity(Choice selected) {       
        NEXT: for ( Choice c : amiChoices ) {
            AmiChoice a = (AmiChoice) c;
            if ( a.ami == null ) continue NEXT; // 'other'
            if ( !a.ami.isUsableReBilling(PPEManager.paramsEc2) 
                 ||
                 a.usesHvm() ) {               
                a.cc.setSelected(false);
                a.cc.setEnabled(false);                
            }
            else a.cc.setEnabled(true);
        }

        spotPriceChoice.setEnabled(true);
    }       
}
