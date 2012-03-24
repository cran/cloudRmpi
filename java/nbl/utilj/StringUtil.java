/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nbl.utilj;

/**
 *
 * @author moi
 */
public class StringUtil {
    
    /** @return the stack trace and causal message as string
     * 
     * @param x
     * @return 
     */
    public static String toString(Throwable x) {
        return(exceptionStackToString(x) + "\n\n" +
               getExceptionMessage(x)
               );
    }
    
    public static String getExceptionMessage(Throwable x) {
        Throwable c = x.getCause();
        if ( (c != null) && (c.getMessage() != null) &&
             (c.getMessage().length() > 0) ) {
            return(c.getMessage());
        }
        else return( x.toString() );  
    }
    
    public static String exceptionStackToString(Throwable x) {
        if ( x == null ) return("NULL exception!");        
        return(throwableToString(x,""));
    }
    
    private static String throwableToString(Throwable x,String prefix) {
        
        String mess = x.toString() + "\n\n" +
                      stackTraceToString(x.getStackTrace());
        Throwable cause = x.getCause();
        if ( cause != null ) {
            mess += "\nCAUSE:\n" + throwableToString(cause,prefix + "    ");
        }
        return(mess);
    }
    
        /** Converts a <tt>StackTrace[]</tt> to a <tt>String</tt>, one
         *  item per line.
         */
    public static String stackTraceToString(StackTraceElement[] trace) {
        if ( trace == null ) return("NULL stack trace.");
        String s = "";
        for ( int i = 0; i < trace.length; i++ ) {
            if ( trace[i] != null )
                s += trace[i].toString() + "\n";
            else s += "null trace element at [" + i + "]";
        }
        return(s);
    }  
    
    public static String wordsToString(String[] words) {
	if ( words.length < 1 ) return("");
	String s = words[0];
	for (int i = 1; i < words.length; i++ ) s += " " + words[i];
	return(s);
    }
}
