/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.norbl.cbp.ppe.usermonitor;

import com.norbl.util.http.*;
import java.io.*;
import org.apache.http.*;
import org.apache.http.protocol.*;
import org.apache.http.entity.*;

/** After 'Co-Branded Service' (CBUI) authorization (AWS site), the user's
 *  browser is passed to 'returnURL', which is the {@link SimpleHttpServer}
 *  running in {@link UserMonitor}. This method handles that redirction.  It
 *  records authorization information in the db and then displaus a 
 *  static message indicating that authorization is complete.
 *
 * @author Barnet Wagman
 */
public class PostAuthorizationHandler implements HttpRequestHandler {  
    
    UserDb userDb;
    
    public PostAuthorizationHandler(UserDb userDb) {
        this.userDb = userDb;
    }
    
    public void handle(HttpRequest request, 
                       HttpResponse response,
                       HttpContext context) throws HttpException, IOException {
     
        URIObj uio = new URIObj(request.getRequestLine().getUri());                
        
        if ( !uio.hasLocalPath() ) {
            System.err.println("No local path in uri=\n" + 
                                request.getRequestLine().getUri());
            return;
        }
        
        if ( !uio.localPathEls[0].equals(UserMonitor.POST_AUTH_URI_FLAG) ) {
            sendErrorPage(response);
            return;
        }
        
        String uid = uio.localPathEls[1];
        if ( (uid == null) || (uid.length() < 3) ) {
            sendErrorPage(response);
            return;
        }
        
            // Write tokens to the db
        userDb.markAuthorized(uid,
                              uio.getParam("settlementTokenID"),
                              uio.getParam("creditSenderTokenID"),
                              uio.getParam("creditInstrumentID"));
                        
            // Return web page
        response.setStatusCode(HttpStatus.SC_OK);
               
        EntityTemplate body = new EntityTemplate(new ContentProducer() {
                    
                    public void writeTo(final OutputStream outstream) throws IOException {
                        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8"); 
                        writer.write(billingCompleteHtml);                       
                        writer.flush();
                    }
                    
                });
                body.setContentType("text/html; charset=UTF-8");
                response.setEntity(body);
    }    
    
    void sendErrorPage(HttpResponse response) {
             // Return web page
        response.setStatusCode(HttpStatus.SC_OK);
        
        /* D */
        EntityTemplate body = new EntityTemplate(new ContentProducer() {
                    
            public void writeTo(final OutputStream outstream) throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8"); 
                writer.write("<html><body><h1>");
                writer.write("All done.");                        
                writer.write("</h1></body></html>");
                writer.flush();
            }

        });
        body.setContentType("text/html; charset=UTF-8");
        response.setEntity(body);        
    }
    
    // ----------------------------------------------------
    
    private String billingCompleteHtml =
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" +
        "<html>" +
        "<head>" +
        "<meta content=\"text/html; charset=ISO-8859-1\"" +
        "http-equiv=\"Content-Type\">" +
        "<title></title>" +
        "</head>" +
        "<body>" +
        "<br>" +
        "<br>" +
        "<br>" +
        "<h3 style=\"margin-left: 40px;\">Billing authorization complete</h3>" +
        "<div style=\"margin-left: 40px;\"><br>" +
        "<br>" +
        "<br>" +
        "You can close this window.<br>" +
        "</div>" +
        "</body>" +
        "</html>";
    
    private String errorHtml =
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">" +
        "<html>" +
        "<head>" +
        "<meta content=\"text/html; charset=ISO-8859-1\"" +
        "http-equiv=\"Content-Type\">" +
        "<title></title>" +
        "</head>" +
        "<body>" +
        "<br>" +
        "<br>" +
        "<br>" +
        "<h3 style=\"margin-left: 40px;\">An error occurred during billing" +
        "authorization.<br>" +
        "</h3>" +
        "<div style=\"margin-left: 40px;\"><br>" +
        "<br>" +
        "<br>" +
        "You may need to repeat the process.<br>" +
        "</div>" +
        "</body>" +
        "</html>";
}

