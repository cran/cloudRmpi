/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe;

import com.norbl.util.*;
import java.io.*;
import com.amazonaws.*;
import com.norbl.util.gui.*;
import java.util.*;
import com.norbl.cbp.ppe.gui.*;

/** Used to create a <tt>com.aws.ClientConfiguration</tt>
 *  object. This class stores/and retrieves params from
 *  config file <tt>~user/.ppe-aws-client-config</tt>.
 *
 * @author Barnet Wagman
 */
public class AwsClientConfigParams extends ParamHt {
    
    public static final String ACC_CONFIG_FILENAME = ".ppe-aws-client-config";
    
    PPEManager ppeManager;
    
    /** The params that can be set in <tt>com.aws.ClientConfiguration</tt>.
     *  The enum.toString() is used as the key in the config file and
     *  in the ht.
     */
    public enum AccParam {
        ConnectionTimeout,
        MaxConnections,
        MaxErrorRetry,
        Protocol,
        ProxyDomain,
        ProxyHost,
        ProxyPassword,
        setProxyPort,
        setProxyUsername,
        setProxyWorkstation,
//        SocketBufferSizeHintSend,
//        SocketBufferSizeHintReceive,
        SocketTimeout,
        UserAgent        
    }
    
    public AwsClientConfigParams(PPEManager ppeManager)
        throws InaccessibleFileException, IOException {
        super(new String[] {}, ACC_CONFIG_FILENAME);
        this.ppeManager = ppeManager;
    }   
   
    public ClientConfiguration buildClientConfiguration() {
        try {
            if (this.size() < 1 ) return(null);

            ClientConfiguration cc = new ClientConfiguration();

            String errs = "";
            int nSet = 0;
            for ( Iterator<String> it = this.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                String val = this.get(key);
                try {
                    AccParam ap = AccParam.valueOf(key);                
                    setParamVal(cc,ap,val);
                    ++nSet;
                }
                catch(IllegalArgumentException iax) {
                    errs += iax.getMessage() + " ";                 
                }
                catch(RuntimeException rx) {
                    GuiUtil.exceptionMessageOnly(rx);
                }
            }
            if ( nSet < 1 ) return(null);
            if ( errs.length() > 0 ) {
                GuiUtil.warning(new String[] {
                    "Undefined parameters were found in the AWS client",
                    " config file '" + ACC_CONFIG_FILENAME + "'",
                    " ",
                    errs.trim() },
                    "Undefined parameters"    
                    );            
            }
            return(cc);
        }
        catch(Exception xxx) {
            GuiUtil.exceptionMessageOnly(xxx);
            return(null);
        }
    }
    
    public void editParams() {
        
        final AwsClientConfigParamsFrame f =
                 new AwsClientConfigParamsFrame(this);
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {        
                f.go();
            }
        });
        
        while ( f.isActive() ) {
            try { Thread.sleep(500); }
            catch(InterruptedException ix) {}
        }
        
        if ( f.isCancelled()  ) {         
            return;
        }
        else {                     
            NEXT: for ( LabeledTextField p : f.params ) {                
                String val = p.getVal();                                 
                if ( (val != null) && (val.trim().length() < 1) )
                    val = null;
                this.put(p.getKey(),val);                              
            }           
            
            this.saveAndReload();                     
        }             
    }    
    
    private void setParamVal(ClientConfiguration cc, AccParam ap, String val) {
        
        switch (ap) {
            case ConnectionTimeout:
                cc.setConnectionTimeout(Integer.parseInt(val));
                break;
            case MaxConnections:
                cc.setMaxConnections(Integer.parseInt(val));
                break;
            case MaxErrorRetry:
                cc.setMaxErrorRetry(Integer.parseInt(val));
                break;
            case Protocol:
                cc.setProtocol(Protocol.valueOf(val));
                break;
            case ProxyDomain:
                cc.setProxyDomain(val);
                break;
            case ProxyHost:
                cc.setProxyHost(val);
                break;
            case ProxyPassword:
                cc.setProxyPassword(val);
                break;
            case setProxyPort:
                cc.setProxyPort(Integer.parseInt(val));
                break;
            case setProxyUsername:
                cc.setProxyUsername(val);
                break;
            case setProxyWorkstation:
                cc.setProxyWorkstation(val);
                break;
            case SocketTimeout:
                cc.setSocketTimeout(Integer.parseInt(val));
                break;
            case UserAgent:
                cc.setUserAgent(val);
                break;

            default:    
        }
    }          
    
    public static String getGuiText() {
        return(    
            "<html><body>" +            
            "If you access the internet through a proxy server, you <i>may</i> need " +
            "to set some Amazon client configuration parameters before you can " +
            "communicate with AWS and launch ec2 instances. See " +                      
            "Amazon's documentation for details.</a>" +
            "<br><br>" +
            "The values you enter below will be stored in a config file named" + 
            "<blockquote>" +
             ACC_CONFIG_FILENAME + "</blockquote>" +
            "in your home directly and will be used " +
            "whenever you launch a network. You can leave irrelevant fields blank." +
            "</body></html>"
         );
    }
}
