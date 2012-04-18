/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.norbl.util.http;

import java.net.*;
import java.io.*;
import org.apache.http.*;
import org.apache.http.impl.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;

/** A very simple http server. 
 * 
 *  @author moi
 */
public class SimpleHttpServer {
  
    public static class ReqListenerThread extends Thread {                
        
        private final ServerSocket serversocket;
        private final HttpParams params; 
        private final HttpService httpService;
        private final HttpRequestHandler reqHandler;
                        
        public ReqListenerThread(HttpRequestHandler reqHandler, 
                                 int port) throws IOException {            
                        
            this.reqHandler = reqHandler;
            this.serversocket = new ServerSocket(port);
            this.params = new SyncBasicHttpParams();
            
            this.params
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
            
             // Set up the HTTP protocol processor
            HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                    new ResponseDate(),
                    new ResponseServer(),
                    new ResponseContent(),
                    new ResponseConnControl()
            });
            
            // Set up request handlers
            HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
            reqistry.register("*",reqHandler);
            
            // Set up the HTTP service
            this.httpService = new HttpService(
                    httpproc, 
                    new DefaultConnectionReuseStrategy(), 
                    new DefaultHttpResponseFactory(),
                    reqistry,
                    this.params);
        }
        
        public void run() {
            System.out.println("SimpleHttpServer listening on port " + 
                               this.serversocket.getLocalPort());
            while (!Thread.interrupted()) {
                try {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    conn.bind(socket, this.params);

                    // Start worker thread
                    Thread t = new WorkerThread(this.httpService, conn);
                    t.setDaemon(true);
                    t.setPriority(MIN_PRIORITY);
                    t.start();
                } catch (InterruptedIOException ex) {
                    break;
                } catch (IOException e) {
                    System.err.println("I/O error initialising connection thread: " 
                            + e.getMessage());
                    break;
                }
            }
        }
    }
    
    static class WorkerThread extends Thread {

        private final HttpService httpservice;
        private final HttpServerConnection conn;
        
        public WorkerThread(
                final HttpService httpservice, 
                final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }
        
        public void run() {
            System.out.println("New connection thread");
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                System.err.println("Client closed connection");
            } catch (IOException ex) {
                System.err.println("I/O error: " + ex.getMessage());
            } catch (HttpException ex) {
                System.err.println("Unrecoverable HTTP protocol violation: " + ex.getMessage());
            } finally {
                try {
                    this.conn.shutdown();
                } catch (IOException ignore) {}
            }
        }

    }
    
        // --------------------------------------------------------------
    
    public static void main(String[] args) throws Exception {
        
        Thread t = new ReqListenerThread(new ReqHandler(), 80);
        t.setDaemon(false);
        t.start();
    }
    
    /** For testing only  
     * 
     */
    public static class ReqHandler implements HttpRequestHandler  {

        public ReqHandler() {}
        
        /** Note that handler() must be reentrant.
         * 
         * @param request
         * @param response
         * @param context
         * @throws HttpException
         * @throws IOException 
         */
        public void handle(
            final HttpRequest request, 
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

            URIObj uio = new URIObj(request.getRequestLine().getUri());
            
        /* D */ System.out.println(
//                    "    request=" + request + "\n" +
                "---------------------------------------------\n" +
                    "response=\n" + response + "\n" +
                    "context=\n" + context + "\n" +
                    "line method=\n" + request.getRequestLine().getMethod() + "\n" +
                    "line uri=\n" + uio + "\n" +
                    "---------------------------------------------\n" 
                );                                             
        }                
    }
}

/*
response=
HTTP/1.1 200 OK []
context=
org.apache.http.protocol.BasicHttpContext@68916a2
line method=
GET
line uri=
/?signature=T2YTvmHRKGWT7yOzBvym0aX3wArPRHKFWaHxho6c6S1wNe9fOr5jTQzdJmGxGtEdhRdAdVpyF6co
rbWuG8Y7a1idx+br18WYoyIJB81oQXiea2XtN2gMsaviI4itODZi8rhyHgapkdWzt5ghnB46BGBO
csj76Eqh/6g0LUN5/Vw
expiry=09/2017
signatureVersion=2
signatureMethod=RSA-SHA1
tokenID=I72X2BIXS4XNPIXVJQ5X6KAA8A8FIABKI3CTMVTHKEG9XD58T6FEDM2HMVS9RMW1
status=SC
callerReference=xyz@abc.com_uid_1332967425972

 */
