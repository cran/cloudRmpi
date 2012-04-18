/*
    Copyright 2012 Northbranchlogic, Inc.

    This file is part of Remove R Evaluator (rreval).

    rreval is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    rreval is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with rreval.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.norbl.cbp.ppe.usermonitor;

import java.net.*;
import java.io.*;

/** A socket connection to another java app and the associated 
 *  object stream reader and writer. Note that a {@link JReader}, {@link RWriter} 
 *  pair share the same connection (since both actually need to read and write.<p>
 * 
 *  A <tt>ConnectionJ</tt> is not itself secure.  Security is achieved 
 *  by doing communication via ssh port forwarding.
 *
 * @author Barnet Wagman
 */
public class ReadWriteConnection {
    
    public enum HostType { server, client }
    
    public static int MAX_WRITE_RETRIES = 16;
    public static long RETRY_NAP = 500L;
    
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;   
    
    boolean keepReading;

    public ReadWriteConnection(Socket socket, HostType type) throws IOException {
        
        this.socket = socket;
        
        if ( HostType.server.equals(type) ) {
            ois = new ObjectInputStream(socket.getInputStream()); 
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();   
        }
        else if ( HostType.client.equals(type) ) {
            oos = new ObjectOutputStream(socket.getOutputStream());                 
            ois = new ObjectInputStream(socket.getInputStream()); 
        }
        else throw new RuntimeException("Undefinded HostType=" + type);           
    }

    public synchronized void writeMessage(Serializable obj) throws Exception {
        Exception z = null;
        
        for ( int i = 0; i < MAX_WRITE_RETRIES; i++ ) {        
            try {               
                write(obj);                             
                if ( readAck() ) return;
                else { 
                    try { Thread.sleep(RETRY_NAP); }            
                    catch(InterruptedException ix) {}                
                }
            }
            catch(Exception xxx) {
                if ( (xxx instanceof RuntimeException) &&
                     !(xxx instanceof ClassCastException) ) {
                    throw ((RuntimeException) xxx);
                }
                else z = xxx;
            }
        }        
        if ( z != null ) {         
            throw z;
        }
        else {            
            throw new Exception("Write object failed after " +
                MAX_WRITE_RETRIES + " tries.");
        }
    }
    
    public Serializable readMessage() throws BadMessageException {
        keepReading = true;
        while (keepReading) {
            try {
                Serializable obj = (Serializable) read();
                writeAck(true);
                return(obj);
            }
            catch(BadMessageException bx) {
                // This means that something went wrong in read().
                // We may have received something other than a
                // Message.  In this case we do not send and ack.
                throw bx;
            }
            catch(Exception xxx) {
                writeAck(false);
            }
        }
        return(null);
    }
    
    public void close() {
        try {
            if ( ois != null ) ois.close();
            if ( oos != null ) oos.close();
            if ( socket != null ) socket.close();
        }
        catch(IOException iox) { throw new RuntimeException(iox); }
    }    
            
        // -------------------------------------------------------
    
    private void write(Serializable obj) throws Exception {        
        oos.writeObject(obj);
    } 
        
    private Serializable read() throws BadMessageException {
        try {
            Object obj = ois.readObject();
            Serializable s = (Serializable) obj;
            return(s);
        }
        catch(Exception xxx) {
            // If there's any problem with the object, throw an exception.
            throw new BadMessageException(xxx);
        }
    }    
           
    private void writeAck(boolean b) {
        try {
            write(new Boolean(b));
        }
        catch(Exception x) { throw new RuntimeException(x); }
    }
    
    private boolean readAck() throws Exception {
        Boolean ack = (Boolean) read();
        return(ack.booleanValue());
    }
}
