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

import ppe.ec2.*;
import ppe.ompi.*;
import com.amazonaws.services.ec2.model.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import ppe.gui.InProgressFrame;


/** Gathers info and provides logic behind the NetworkSpecFrame.
 *
 * @author Barnet Wagman
 */
public class NetworkSpecGui {

    Ec2Wrangler ec2w;
    List<Image> images;
    List<Ec2InstanceType> instanceTypes;
    List<String> keypairNames;

    boolean specComplete;
    public NetworkSpec spec;
    public OmpiSpec specOmpi;

        // ---- The choices -------------------

    ChoiceSet instanceTypeChoices;
    ChoiceSet amiChoices;
    ChoiceSet availabilityZoneChoices;
    ChoiceSet securityGroupChoices;
    ChoiceSet keyPairsChoices;

    SpotPriceChoice spotPriceChoice;
    NInstancesChoice nInstancesChoice;
    NetworkNameChoice networkNameChoice;
    SlotsPerHostChoice slotsPerHostChoice;

        // -------------------------------------

    public NetworkSpecGuiFrame networkSpecFrame;

    public NetworkSpecGui(Ec2Wrangler ec2w, 
                          NetworkSpec spec,
                          OmpiSpec specOmpi)
        throws FileNotFoundException, IOException {
        this.ec2w = ec2w;
        this.spec = spec.cloneSpec();
        this.specOmpi = specOmpi.cloneSpec();
        images = AmiDescription.getSupportedImages(ec2w.ec2Client);
        instanceTypes = Ec2InstanceType.getInstanceTypes();
        keypairNames = ec2w.getKeypairNames();

        createChoices();
    }

    void createChoices() {

        instanceTypeChoices = new ChoiceSet();
        for ( Ec2InstanceType eit : instanceTypes ) {
            instanceTypeChoices.add(new InstanceTypeChoice(eit));
        }
            // Select the instance specified in params, if any.
        if ( spec.instanceType != null ) {
            Choice c = instanceTypeChoices.getChoiceOrNull(
                                        spec.instanceType.toString());
            if ( c != null ) c.setSelected(true);
        }

        amiChoices = new ChoiceSet();
        for ( Image img : images ) {                     
            amiChoices.add(new AmiChoice(img,ec2w.ec2Client));
        }
//        amiChoices.add(new AmiChoice()); // 'other'
            // Select the ami specified in params, if any.
        AmiChoice paramSpecifiedAmi = null;
        if ( spec.imageID != null ) {
            Choice c = amiChoices.getChoiceOrNull(spec.imageID);
            if ( c != null ) {
                c.setSelected(true);
                paramSpecifiedAmi = (AmiChoice) c;
            }
            else {
                AmiChoice other = (AmiChoice) amiChoices.get(amiChoices.size()-1);
                other.setSelected(true);
                other.value = spec.imageID;
                paramSpecifiedAmi = other;
            }
        }

        availabilityZoneChoices = new ChoiceSet();
        createZoneChoices(paramSpecifiedAmi);
        
        keyPairsChoices = new ChoiceSet();
        for ( String kp : keypairNames ) {
            keyPairsChoices.add(new KeyPairChoice(kp));
        }
        keyPairsChoices.add(new KeyPairChoice());
            // Select the keyname specified in params, if any.
        if ( spec.keyName != null ) {
            Choice c = keyPairsChoices.getChoiceOrNull(spec.keyName);
            if ( c != null ) c.setSelected(true);
        }

        securityGroupChoices = new ChoiceSet();
        securityGroupChoices.add(
            new SecurityGroupChoice(SecurityGroupChoice.Type.defaultGroup));
        securityGroupChoices.add(
            new SecurityGroupChoice(SecurityGroupChoice.Type.other));

        spotPriceChoice = new SpotPriceChoice();
        nInstancesChoice = new NInstancesChoice();

        if ( spec.networkName == null )
            networkNameChoice = new NetworkNameChoice(
                                NiM.createUniqueDefaultNetworkName());
        else networkNameChoice = new NetworkNameChoice(spec.networkName);

        if ( specOmpi.slotsPerHost > 0 )
            slotsPerHostChoice = new SlotsPerHostChoice(specOmpi.slotsPerHost);
        else {
            slotsPerHostChoice = new SlotsPerHostChoice();
        }
    }

    /** {@link #fillSpec()} launches the network spec gui in a 
     *  separate thread. It then sleeps until params have been
     *  entered.
     * 
     * @return 
     */
    public boolean fillSpec() {

        specComplete = false;

        networkSpecFrame = new NetworkSpecGuiFrame(this);
//        ppe.gui.InProgressFrame.end();
        java.awt.EventQueue.invokeLater(new Runnable() { public void run() {
            networkSpecFrame.create();
            ppe.gui.InProgressFrame.end();
         }});

        while ( networkSpecFrame.notDone ) {
             try { Thread.sleep(500L); }
             catch(InterruptedException ix) {}
        }
        return(specComplete);
    }

        /** Checks whether the network has been fully specified.
         *
         * @return null if the network is fully specified, or
         *  a message indicating what's missing.
         */
    public String checkSpec() {

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
       
        return(s);
    }

    public OmpiSpec choicesToOmpiSpec() {

        OmpiSpec s = new OmpiSpec();

        s.networkName = (String) networkNameChoice.getValue();
        s.slotsPerHost = slotsPerHostChoice.getNSlots();
        InstanceTypeChoice instanceType = (InstanceTypeChoice)
                                instanceTypeChoices.getSelected();
        if ( instanceType != null )
            s.instanceType = instanceType.instanceType.instanceType;
                    // (String) instanceType.getValue();


        s.rsaKeyPairFile = specOmpi.rsaKeyPairFile;
        s.disableHyperthreading = specOmpi.disableHyperthreading;

        return(s);
    }

    String toHtmlString() {

        NetworkSpec ns = choicesToNetworkSpec();
        if ( !ns.isComplete() )
            throw new RuntimeException("Incomplete NetworkSpec:<br>" +
                                       ns.listMissingParams());
        OmpiSpec os = choicesToOmpiSpec();
        if ( !os.isComplete() )
            throw new RuntimeException("Incomplete OmpiSpec:<br>" +
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
        s.append("<li>Slots per host:  " + os.slotsPerHost + "</li>");

        s.append("</ul><br><b>Please note that if you proceed you will incur charges " +
                 "from Amazon for ec2 instances.</b><br><br></html>");

        return(s.toString());
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

    public Choice getChoice(String choiceType, String label) {
    
        if ( instanceTypeChoices.hasChoiceType(choiceType) )
            return(instanceTypeChoices.getChoice(label));
        else if ( amiChoices.hasChoiceType(choiceType) )
            return(amiChoices.getChoice(label));
        else if ( securityGroupChoices.hasChoiceType(choiceType) )
            return(securityGroupChoices.getChoice(label));
        else if ( keyPairsChoices.hasChoiceType(choiceType) )
            return(keyPairsChoices.getChoice(label));
        else if ( spotPriceChoice.getChoiceType().equals(choiceType) )
            return(spotPriceChoice);
        else if ( nInstancesChoice.getChoiceType().equals(choiceType) )
            return(nInstancesChoice);
        else if ( networkNameChoice.getChoiceType().equals(choiceType) )
            return(networkNameChoice);
            else if ( slotsPerHostChoice.getChoiceType().equals(choiceType) )
                return(slotsPerHostChoice);
        else if ( availabilityZoneChoices.hasChoiceType(choiceType) )
                return(availabilityZoneChoices.getChoice(label));

        else throw new RuntimeException("Unknown choice className: " + choiceType);
    }

        /** Get all choices of the specified type
         *
         * @param choiceType
         * @return
         */
    private List<Choice> getChoices(String choiceType) {

        List<Choice> chs = new ArrayList<Choice>();

        if ( instanceTypeChoices.hasChoiceType(choiceType) )
            return(instanceTypeChoices);
        else if ( amiChoices.hasChoiceType(choiceType) )
            return(amiChoices);
        else if ( securityGroupChoices.hasChoiceType(choiceType) )
            return(securityGroupChoices);
        else if ( keyPairsChoices.hasChoiceType(choiceType) )
            return(keyPairsChoices);
        else if ( spotPriceChoice.getChoiceType().equals(choiceType) ) {
            chs.add(spotPriceChoice);
            return(chs);
        }
        else if ( nInstancesChoice.getChoiceType().equals(choiceType) ) {
            chs.add(nInstancesChoice);
            return(chs);
        }
        else if ( networkNameChoice.getChoiceType().equals(choiceType) ) {
            chs.add(networkNameChoice);
            return(chs);
        }
        else if ( slotsPerHostChoice.getChoiceType().equals(choiceType) ) {
            chs.add(slotsPerHostChoice);
            return(chs);
        }
        else if ( availabilityZoneChoices.hasChoiceType(choiceType) )
            return(availabilityZoneChoices);

        else throw new RuntimeException("Unknown choice className: " + choiceType);
    }

    public void updateRe(String choiceType, String label) {
       
        Choice c = getChoice(choiceType,label);
        
        List<Choice> choices = getChoices(choiceType);
        if ( choices.size() > 1)
            for ( Choice x : choices ) x.setSelected(false);
        c.setSelected(true);

        if ( c instanceof InstanceTypeChoice ) {
            imposeConformityReInstanceTypeChoice(
                                        ((InstanceTypeChoice) c));
            slotsPerHostChoice.setNSlots(
                ((InstanceTypeChoice) c).instanceType.nCores);
        }
        else if ( c instanceof AmiChoice ) {         
                // Create/update the choice of zones.
            (new CreateZoneChoicesAndSetSpotPriceChoice((AmiChoice) c)).start();
        }
    }

        // --------------------------------------------

    class CreateZoneChoicesAndSetSpotPriceChoice extends Thread {
        AmiChoice c;
        
        CreateZoneChoicesAndSetSpotPriceChoice(AmiChoice c) { this.c = c; }
        public void run() {
            InProgressFrame.showInProgress();
            createZoneChoices((AmiChoice) c);
            InProgressFrame.end();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    networkSpecFrame.buildZoneMenu();                    
                    setSpotPriceChooserReAmiChoice(c);
                }
            });
        }
    }
    
    class CreateZoneChoices extends Thread {
        AmiChoice c;

        CreateZoneChoices(AmiChoice c) { this.c = c; }
        public void run() {
            InProgressFrame.showInProgress();
            createZoneChoices((AmiChoice) c);
            InProgressFrame.end();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    networkSpecFrame.buildZoneMenu();
                }
            });
        }
    }

    void createZoneChoices(AmiChoice amiChoice) {

        if ( (amiChoice != null) && (amiChoice.image != null) &&
             (amiChoice.image.getImageId() != null) ) {
            String ami = amiChoice.image.getImageId();
            if ( ami == null ) return;

            availabilityZoneChoices.clear();
            List<AvailabilityZone> zones =
                AmiDescription.getAvailabilityZones(ec2w.ec2Client,ami);

               // First add the 'any zone' case.
            availabilityZoneChoices.add(new AvailabilityZoneChoice(null));
            for (AvailabilityZone z : zones )
                availabilityZoneChoices.add(new AvailabilityZoneChoice(z));
                  // Enable them all
            for ( Choice c : availabilityZoneChoices ) {
               c.setEnabled(true);               
           }
           
        }
        else { // Create disabled default place holder list.
           availabilityZoneChoices.clear();
              // First add the 'any zone' case.
           availabilityZoneChoices.add(new AvailabilityZoneChoice(null));

           for ( AvailabilityZone z :
                 ec2w.ec2Client.describeAvailabilityZones().getAvailabilityZones() ) {
                 availabilityZoneChoices.add(new AvailabilityZoneChoice(z));
           }
                 // Disable them all
           for ( Choice c : availabilityZoneChoices ) {
               c.setEnabled(false);
           }
        }        
    }
    
    void setSpotPriceChooserReAmiChoice(AmiChoice amiChoice) {       
        
        if ( amiChoice.isDevPay() &&                                 
             ((SpotPriceCC) spotPriceChoice.cc).isPopulated() ) {
            spotPriceChoice.depopulateCC(networkSpecFrame);            
        }   
        else if ( !amiChoice.isDevPay() &&
                  !((SpotPriceCC) spotPriceChoice.cc).isPopulated() ) {
            spotPriceChoice.repopulateCC(networkSpecFrame);
        }
    }
            
    void imposeConformityReInstanceTypeChoice(InstanceTypeChoice c) {

        if ( !c.cc.getRadioButton().isSelected() ) return;

        if ( c.instanceType.isHvm() ) imposeHvmConformity(c);
        else if ( c.instanceType.isPv() ) imposeParavirtualConformity(c);
        else throw new RuntimeException("Bad instance type=" +
                c.instanceType.instanceType.toString() + " " +
                c.instanceType.vt);
    }

    void imposeHvmConformity(InstanceTypeChoice selected) {        
        for ( Choice c : amiChoices ) {
            AmiChoice a = (AmiChoice) c;
            if ( a.usesParavirtual() ) {
                a.cc.setSelected(false);
                a.cc.setEnabled(false);                
            }
            else a.cc.setEnabled(true);
        }

//        spotPriceChoice.setEnabled(false);
//        spotPriceChoice.setSelected(false);
    }

    void imposeParavirtualConformity(Choice selected) {
        for ( Choice c : amiChoices ) {
            AmiChoice a = (AmiChoice) c;
            if ( a.usesHvm() ) {
                a.cc.setSelected(false);
                a.cc.setEnabled(false);                
            }
            else a.cc.setEnabled(true);
        }

        spotPriceChoice.setEnabled(true);
    }
}
