#
#    Copyright 2012 Northbranchlogic, Inc.
#
#    This file is part of cloudRmpi.
#
#    cloudRmpi is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#
#    cloudRmpi is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with cloudRmpi.  If not, see <http://www.gnu.org/licenses/>.
#
#    ----------------------------------------------------------------------
#
#    PPEClient.R
#
#    Client side functions for launching and accessing the ppe-ompi
#    (Open MPI Parallel Processing with EC2) network manager, ROmpiPPEManager,
#    which is a java application.
#
#    Functions used directly by users are:
#
#    Author: Barnet Wagman
#
#    ----------------------------------------------------------------------

#' Launch the ppe-ompi network manager application.
#' 
#' The network manager is a gui java app used for launching, monitoring and
#' managing networks of EC2 instances that supports R parallel processing 
#' ('ppe' is short for 'parallel processing with EC2').
#'
#' @author Barnet Wagman
#' @export
#' @param port number of port used for communcations with the network manager.
#' @param timeout socket timeout (in seconds).
#' @param verbose for debugging.
ppe.launchNetworkManager <- function(port=4461,timeout=2,verbose=FALSE) {
  
  setVerbose(verbose);
  
  defineMessageTypes();
  
  if ( clientAppIsRunning(port=port,timeout=timeout) ) {
    connectToNetworkManager(port = port, timeout = timeout);    
    if ( verboseOn() ) print(paste("The ompi-ppe network manager is already running, ",
                               "port=",port,sep=""));
    return();
  }
  else {
    cmd <- paste("java -classpath ",getClassPath(),
                 " cloudrmpi.ROmpiPPEApp",
                 " ppeManagerPort=",port,              
                 sep=""
                 );
  
    system(cmd,intern=FALSE,wait=FALSE,ignore.stderr=TRUE,ignore.stdout=TRUE);
    if ( verboseOn() ) print(paste("system:",cmd));       
  }
  
  tryCatch(rj <- connectToNetworkManager(port=port,timeout=timeout),
          error=function(e) {},
          warning=function(w) {}
  );
  if ( verboseOn() ) print(rj);
}

#' Start ppe-ompi client
#'
#' ppe.startClient() creates a connection to an R session running on the 
#' master node of a ppe-ompi network.
#'
#' Note that the connection (which is a socket connection) is not returned
#' by ppe.startClient() - it is stored in an environment dedicated to 
#' managing connections.  cloudRmpi functions retrieve connections based
#' based on a network's name or the name of a network's master node.  
#' You do not need to use connection objects directly.
#'
#' The connection ppe.startClient() creates is actually to locally running 
#' java app,rreval.RReClientApp.  If the app is not running, ppe.startClient() 
#' will launch it.
#'
#'
#' @param hostName the name of host that is the master node of the 
#' ppe-ompi network; usually NULL (the default). If this param is NULL 
#' and the network name
#' is NULL, the function will attempt to retrieve a connection from the 
#' connection manager environment.  This param needs to be supplied only
#' if you have more than one ppe-ompi network running.
#' @param userName name of user on the master node host under which
#' R is running.  By default the user name is "ec2-user" and you do not
#' need to specify this parameter.
#' @param networkName name of a ppe-ompi network; usually NULL (the default).
#' If this param is NULL and the hostName is NULL, the function will attempt 
#' to retrieve a connection from the connection manager environment.  This 
#' param is only used if you have more than one ppe-ompi network running; int
#' that case it is an alternative to specifying the hostName.
#' @param pemFile RSA keypair file used to access to master node of the
#' ppe-ompi network.  If NULL, the function obtains the file's pathname from
#' the network manager.  Normally you do not need to specify this parameters.
#' @param portRJ number of the port used for communications with 
#' rreval.RReClientApp, the Java app that handles communications
#' with the remote server on the master node of the ppe-ompi network.
#' (rreval.RReClientApp is launched by ppe.startClient() if it is not 
#' already running.)
#' @param portJJ number of the port used by the java app rreval.RReClientApp
#' to communicate with the the rreval.RReServerApp, the server side java app.
#' @param portPPEManager port number of port used for communcations with the network manager.
#' @param timeout socket timeout (in seconds).
#' @param verbose for debugging.
#' @author Barnet Wagman
#' @export
ppe.startClient <- function(hostName=NULL,
                            userName="ec2-user",
                            networkName=NULL,
                            pemFile=NULL,
                            portRJ=4460,
                            portJJ=4464,
                            portPPEManager=4461,
                            timeout=2,                      
                            verbose=FALSE
                      ) {
  setVerbose(verbose);
  
    # Start the app if needed
  ppe.launchNetworkManager(port=portPPEManager,timeout=timeout);
     
    # Get unspecified args
  if ( is.null(hostName) ) {
    hostName <- ppe.getMasterNodeURL(networkName=networkName);    
  }
  
  if ( is.null(pemFile) ) {
    pemFile <- sendCmdToNetworkManager(cmdName="getPemFile");
  }
  
  rre.startClient(hostName=hostName,userName=userName,pemFile=pemFile,
                  portRJ=portRJ,portJJ=portJJ,timeout=timeout,verbose=verbose);
}

#' Close the connection to the master node
#'
#' ppe.closeClient() closes the connection to the R session on the
#' master node that was created by ppe.startClient().  After this function
#' is called, the R session on the master node can accept a connection from
#' another client.
#'
#' @author Barnet Wagman
#' @export
#' @param hostName the name of host that is the master node of the 
#' ppe-ompi network;
#' usually NULL (the default). If this param is NULL and the network name
#' is NULL, the function will attempt to retrieve a connection from the 
#' connection manager environment.  This param needs to be supplied only
#' if you have more than one ppe-ompi network running.
#' @param networkName name of a ppe-ompinetwork; usually NULL (the default).
#' If this param is NULL and the hostName is NULL, the function will attempt 
#' to retrieve a connection from the connection manager environment.  This 
#' param is only used if you have more than one ppe-ompi network running; in
#' that case it is an alternative to specifying the hostName.
#' @param userName name of user on the master node host under which
#' R is running.  By default the user name is "ec2-user" and you do not
#' need to specify this parameter.
#' @param verbose for debugging.
ppe.closeClient <- function(hostName=NULL,
                            userName="ec2-user",
                            networkName=NULL,
                            verbose=FALSE) {
  setVerbose(verbose);
  
  if ( is.null(hostName) ) {
    hostName <- ppe.getMasterNodeURL(networkName=networkName);    
  }
  rre.closeClient(hostName=hostName,userName=userName)
}
 
#' Show clients
#'
#' Displays a list of active clients in this R session, i.e. the
#' names of ppe-ompi network master nodes to which there are open connections. 
#'
#' @author Barnet Wagman
#' @export
#' @return (hostName,userName) pairs in a data.frame.
ppe.showClients <- function() {
  rre.showClients();
}
 
#' Shutdown connections
#'
#' Close all open connections to master nodes,terminates the 
#' ppe-ompi access application, terminates port forwarding to
#' RStudio Server, and disconnects from the network manager.
#'
#' @note This function does not terminate ec2 instances.
#' @author Barnet Wagman
#' @export
ppe.shutdown <- function() {  
  tryCatch(rre.closeAllConnections(),
           error=function(e){},warning=function(w){});
  tryCatch(ppe.shutdownPortForwardingApp(),
           error=function(e){},warning=function(w){});
  tryCatch(ppe.disconnectNetworkManager(),
           error=function(e){},warning=function(w){});
}

#' Get network names
#' 
#' Get the names of all ppe-ompi networks owned by the user. Note
#' this may include networks that are in the process of launching
#' or that have recently terminated.  Use getNetworkStatus() to determine
#' whether the network is available (or refer to the ppe-ompi network
#' management application).
#'
#' @author Barnet Wagman
#' @export
#' @return character vector of network names.
ppe.getNetworkNames <- function() {
  
  nnS <- sendCmdToNetworkManager(cmdName="getNetworkNames");
  
  if ( is.null(nnS) || (nchar(trim(nnS)) < 1) ) { 
    stop(simpleError("No ompi-ppe networks were found."));
  }   
  else {
    nnms <- strsplit(x=nnS,split=" ");
    if ( length(nnms) > 0 ) return(unlist(nnms))
    else {
      stop(simpleError("No ompi-ppe networks were found."));
    }
  }
}
  
#' Get the status of a network
#'
#' @author Barnet Wagman
#' @export
#' @param networkName the name of a ppe-ompi network; usually NULL 
#' (the default). If this param is NULL the function will attempt to 
#' retrieve a connection from the connection manager environment.  
#' This param needs to be supplied only if you have more than one 
#' ppe-ompi network running. Use getNetworkNames() to get a list of 
#' all networks.
#' @return status message (character).
ppe.getNetworkStatus <- function(networkName=NULL) {  
  
  nn <- getFRNN(networkName);
    
  sendCmdToNetworkManager(cmdName="getNetworkStateDescription",
                          params=paste("networkName",nn,sep="="))
}

#' Get running network names
#'
#' Get the names of ppe-ompi networks owned by the user that have
#' status='running'.
#'  
#' @author Barnet Wagman
#' @export
#' @return character vector of network names.  
ppe.getRunningNetworkNames <- function() {
  nms <- ppe.getNetworkNames();
  rns <- c(); 
  for ( nm in nms ) {
    x <- ppe.getNetworkStatus(networkName=nm);
    if ( x == "running" ) rns <- c(rns,nm);
  }
  rns
}  

#' Get the URL of the ppi master node in the specfied network.
#'
#' @author Barnet Wagman
#' @export
#' @param networkName the name of a ppe-ompi network; usually NULL 
#' (the default). If this param is NULL the function will attempt to 
#' retrieve a connection from the connection manager environment.  
#' This param needs to be supplied only if you have more than one 
#' ppe-ompi network running. Use getNetworkNames() to get a list of 
#' all networks.
#' @return URL (character).
ppe.getMasterNodeURL <- function(networkName=NULL) { 
      
  nn <- getFRNN(networkName=networkName);    
  sendCmdToNetworkManager(cmdName="getMasterNodeURL",
                          params=paste("networkName",nn,sep="="))
}

#' Terminates EC2 instances
#'
#' Termimates all EC2 instances in the specified network.  This can
#' also be done via the ppe-ompi network managerment application.
#'
#' @author Barnet Wagman
#' @export
#' @param networkName the name of a ppe-ompi network; usually NULL 
#' (the default). If this param is NULL the function will attempt to 
#' retrieve a connection from the connection manager environment.  
#' This param needs to be supplied only if you have more than one 
#' ppe-ompi network running. Use getNetworkNames() to get a list of 
#' all networks.
#' @return message (character).
ppe.terminateNetwork <- function(networkName=NULL) {
  nn <- getFRNN(networkName);    
  sendCmdToNetworkManager(cmdName="terminateNetwork",
                          params=paste("networkName",nn,sep="="))
}
# -----------------------------------------------------------------

#' Get first running network name
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @return network name (string)
getFirstRunningNetworkName <- function() {  
  rns <- ppe.getRunningNetworkNames();
  if ( length(rns) > 0 ) { return(rns[1]); }
  else { return(NULL); }
}  
  
#' getFRNN
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param networkName
#' @return networkName
getFRNN <- function(networkName=NULL) {
  
  if ( !is.null(networkName) ) return(networkName);
  nn <- getFirstRunningNetworkName();
  if ( !is.null(nn) ) { return(nn); }
  else { stop("There are no ompi-ppe networks with 'running' status."); }
}

#' connectToNetworkManager
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param port
#' @param timeout
#' @param maxWait
connectToNetworkManager <- function(port=port,timeout=timeout,maxWait=10) {
  
  con <- getConNetworkManager();
  if ( !isGoodCon(con) ) {
    tmFin <- Sys.time() + maxWait;
    while ( tmFin >= Sys.time() ) {
        con <- tryCatch(conToNM(port=port,timeout=timeout),
                        error=function(e) { 
                          if ( verboseOn() ) print(paste("conNM error=",e));
                          closeEh(con);
                          return(NULL); 
                        },
                        warning=function(w) {}
                        );
        if ( !is.null(con) ) {
          if ( verboseOn() ) print(paste("Connected to ompi-ppe manager on port",
                               port));
          return();
        }                
        else { Sys.sleep(0.1); }
    }
    conToNM(port=port,timeout=timeout);
  }
  else if (verboseOn()) { print("Already connected.") }
}

#' connectToNM
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param port
#' @param timeout
conToNM <- function(port=port,timeout=timeout) {
  con <- connectToApp(port=port,timeout=timeout);
  if ( isGoodCon(con) ) {
      assign(x="conNetworkManager",value=con,envir=.GlobalEnv);
      return(con);
  }
  else {
    stop(simpleError(paste("Failed to connect to the ompi-ppe manager on port",
                             port)));
  }
}

#' getConNetworkManager
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param port
#' @param timeout
#' @return connection
getConNetworkManager <- function() {
  if ( exists(x="conNetworkManager",envir=.GlobalEnv) ) {
    con <- get(x="conNetworkManager",envir=.GlobalEnv);
    if ( isGoodCon(con) ) return(con)
    else return(NULL);
  }
  else { return(NULL); }
}

#' sendCmdToNetworkManager
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param cmdNames
#' @param params
sendCmdToNetworkManager <- function(cmdName,params=NULL) {
  con <- getConNetworkManager();
  if ( !isGoodCon(con) ) stop(simpleError("Not connected to an ompi-ppe",
                                          "network manager."));
  sendAppCmd(con=con,
             cmd=createAppCmd(cmdName=cmdName,params=params))
}

#' shutdownNetworkManager
#' @keywords internal
#' @export
#' @author Barnet Wagman
shutdownNetworkManager <- function() {
  rc <- sendCmdToNetworkManager(cmdName="shutdown");
  con <- getConNetworkManager();
  if ( !is.null(con) ) {
    closeEh(con);
    rm(list=c("conNetworkManager"),envir=.GlobalEnv);
  }
  rc
}


#' launchRReServerAccessApp
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param portR
#launchPPEClientApp <- function(portR) {  
#  
#  cmd <- paste("java -classpath ",getClassPath(),
#               " cloudrmpi.CloudRmpiApp",
#               " portR=",portR,              
#               sep=""
#               );
#  
#  system(cmd,intern=FALSE,wait=FALSE,ignore.stderr=TRUE,ignore.stdout=TRUE);
#  if ( verboseOn() ) print(paste("system:",cmd));
#}

#' launchRReServerAccessApp
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param con
#' @param cmd
#' @param maxWaitSecs
#' @return reply message
sendPPEManagerCmd <- function(con,cmd,maxWaitSecs=-1) {  
  writeMessage(con=con,messageType=mType$ppeManagerCmd,obj=cmd,nMaxTries=1);
  readMessage(con,maxWaitSecs=maxWaitSecs)  
}

#' getClassPath.v0
#' Uses getJavaAppClassPath to get the class paths for cloudRmpi and cloudRmpiJars
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @return class path containing cloudRmpi jars and third party jars.
getClassPath.v0 <- function() {
  
  paste(getJavaAppClassPath("cloudRmpi"),
        getJavaAppClassPath("cloudRmpiJars"),
        sep=":"
        )
}

#' getClassPath
#' Uses getJavaAppClassPath to get the class paths for cloudRmpi and cloudRmpiJars
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @return class path containing cloudRmpi jars and third party jars.
getClassPath <- function() {
  
  if ( .Platform$OS == "windows" ) cpSep <- ";"
  else cpSep <- ":";
  
  cp <- paste(getJavaAppClassPath("cloudRmpi",quoted=FALSE),
              getJavaAppClassPath("cloudRmpiJars",quoted=FALSE),
              sep=cpSep
              );
  paste("\"",cp,"\"",sep="")
}

# --------- Added to support rstudio-server, cloudRmpi 1.2 -----------------

#' Connect to RStudio server.
#' 
#' RStudio server is an IDE for R that runs in a web browser.  This function
#' connects to an instance of RStudio server running on the master node of an
#' EC2 network.
#'
#' The function first start creates a connection to the master node via ssh port 
#' forwarding and then launches your default web browser displaying an RStudio 
#' server session.  To use RStudio, you will need to login with Username=rsu, 
#' Password=rsu.  Note that this is actually quite secure.  Access to the remote 
#' host is via ssh (using your keypair),
#' the RStudio login is superflous (but impossible to disable). See the CloudRmpi
#' manual for details.
#'
#' Note that the RStudio session is logged in as user 'rsu' and initially the
#' working directory is /home/rsu.  Due to constraints imposed by the ssh and
#' the RStudio login mechanism, as user rsu you will not be able to access
#' the /home/ec2-user. And you will not be able to ssh to the system as rsu
#' only as ec2-user.  However, both RStudio and ppe-ompi network manager provide shell
#' access to the host. Once in shell, you can use sudo or su (neither require
#' passwords).  Or you can ssh to the host as ec2-user and the use sudo or su.
#' See the cloudRmpi manual for details.
#'
#' @author Barnet Wagman
#' @export
#'
#' @param hostName the name of host that is the master node of the 
#' ppe-ompi network; usually NULL (the default). If this param is NULL 
#' and the network name
#' is NULL, the function will attempt to retrieve a connection from the 
#' connection manager environment.  This param needs to be supplied only
#' if you have more than one ppe-ompi network running.
#' @param userName name of user on the master node host under which
#' R is running.  By default the user name is "ec2-user" and you do not
#' need to specify this parameter.
#' @param networkName name of a ppe-ompi network; usually NULL (the default).
#' If this param is NULL and the hostName is NULL, the function will attempt 
#' to retrieve a connection from the connection manager environment.  This 
#' param is only used if you have more than one ppe-ompi network running; int
#' that case it is an alternative to specifying the hostName.
#' @param pemFile RSA keypair file used to access to master node of the
#' ppe-ompi network.  If NULL, the function obtains the file's pathname from
#' the network manager.  Normally you do not need to specify this parameters.
#' @param portPfApp number of the port used for communications with 
#' cloudrmpi.PortForwardingServer, the Java app that handles manages port forwarding.
#' @param portRemote number of port that RStudio server is using, 8787 on 
#' cloudRmpi AMIs that support RStudio.
#' @param portLocal number of the local port that is forwarded to the remote port
#' (that the RStudio server is using on the remote node).  This number may be the same
#' as portRemote.
#' @param portNetworkManager port number of port used for communcations with the network manager.
#' @param timeout socket timeout (in seconds).
#' @param verbose for debugging.
ppe.connectToRStudio <- function(hostName=NULL,
                                 userName="ec2-user",
                                 networkName=NULL,
                                 pemFile=NULL,
                                 portPfApp=4470,
                                 portRemote=8787,
                                 portLocal=8787,
                                 portNetworkManager=4461,
                                 timeout=2,
                                 verbose=FALSE
                                 ) {
  
  # Connect to network manager (launching if needed);
  ppe.launchNetworkManager(port=portNetworkManager,
                           timeout=timeout,verbose=verbose);
  
  # Connect to the portforwarding app (launching if needed)
  launchPortForwardingApp(port=portPfApp,timeout=timeout,verbose=verbose);
  
  
  # Get params from the network manager if needed
  if ( is.null(hostName) ) {
    hostName <- ppe.getMasterNodeURL(networkName=networkName); 
  }
  
  if ( is.null(pemFile) ) {
    pemFile <- sendCmdToNetworkManager(cmdName="getPemFile");
  }   
  
  # Start portforwarding
  cmd <- createAppCmd(cmdName="startPortForwarding",
                      c(paste("host",hostName,sep="="),
                        paste("username",userName,sep="="),
                        paste("portremote",portRemote,sep="="),
                        paste("pemfile",pemFile,sep="="),
                        paste("portlocal",portLocal,sep="=")
                        ));
  
  con.pf <- getConPortForwardingApp();
  if ( verboseOn() ) print(paste("con to port forwarding app ok?",isGoodCon(con.pf)));
  
  key <- paste(userName,"@",hostName,sep="");
  
  ret <- sendAppCmd(con=con.pf,cmd=cmd);
  if ( verboseOn() ) print(paste("cmd=",cmd," -> ",ret));
  
  if ( ret != key ) {
    stop(ret);
  }
  
  # Launch browser displaying rstudio server  
  browseURL(url=paste("http://localhost:",portLocal,sep=""));
  
  ret
  
}

#' Disconnect from RStudio server.
#'
#' Disconnects from the RStudio server on the master node of the EC2 network
#' and terminates port forwarding.
#'
#' @author Barnet Wagman
#' @export
#'
#' @param hostName the name of host that is the master node of the 
#' ppe-ompi network; usually NULL (the default). If this param is NULL 
#' and the network name
#' is NULL, the function will attempt to retrieve a connection from the 
#' connection manager environment.  This param needs to be supplied only
#' if you have more than one ppe-ompi network running.
#' @param userName name of user on the master node host under which
#' R is running.  By default the user name is "ec2-user" and you do not
#' need to specify this parameter.
#' @param networkName name of a ppe-ompi network; usually NULL (the default).
#' If this param is NULL and the hostName is NULL, the function will attempt 
#' to retrieve a connection from the connection manager environment.  This 
#' param is only used if you have more than one ppe-ompi network running; int
#' that case it is an alternative to specifying the hostName.
#' @param verbose for debugging.
ppe.disconnectRStudio <- function(hostName=NULL,
                                  userName="ec2-user",
                                  networkName=NULL,
                                  verbose=FALSE) {
  
  # Connect to network manager (launching if needed)
  ppe.launchNetworkManager(verbose=verbose);
  if ( is.null(hostName) ) {
    hostName <- ppe.getMasterNodeURL(networkName=networkName); 
  }
  
  con.pf <- getConPortForwardingApp();
  if ( !isGoodCon(con.pf) ) stop("No connection to the port forwarding app");
  
  cmd <- createAppCmd(cmdName="terminatePortForwarding",
                      c(paste("host",hostName,sep="="),
                        paste("username",userName,sep="=")                    
                        ));
  
  sendAppCmd(con=con.pf,cmd=cmd)
  
}

#' Shut down the port forwarding Java app.
#'
#' This function is called by .Last, to it is not usually necessary to use
#' it directly.
#'
#' @author Barnet Wagman
#' @export
#'
#' @param maxWait maxiumum time (in seconds) to wait for a
#' connection to the port forwarding application.
ppe.shutdownPortForwardingApp <- function(maxWait=2) {
  
  tryCatch(shutdownPortForwardingApp(maxWait=maxWait),
           error=function(e){},warning=function(w){});
  
  tryCatch(rm(conPortForwardingApp,envir=.GlobalEnv),
           error=function(e){},warning=function(w){});
}

#' shutdownPortForwardingApp
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @paran maxWait
shutdownPortForwardingApp <- function(maxWait=2) {
  
  con.pf <- getConPortForwardingApp(maxWait=maxWait);
  if ( !isGoodCon(con.pf) ) {       
    if ( verboseOn() ) stop("No connection to the portforwarding app")
    else return;
  }
  
  cmd <- createAppCmd(cmdName="shutdown");
  
  if ( verboseOn() ) print(paste("cmd=",cmd));
  
  r <- sendAppCmd(con=con.pf,cmd=cmd);  
  closeEh(con.pf);
  
  if ( verboseOn() ) print(paste("cmd -> ",r));
}


#' Shutdowns down all connections and port forwarding.
#'
#' @author Barnet Wagman
#' @export
.Last <- function() {
  
  tryCatch( ppe.shutdownPortForwardingApp(maxWait=1),
            error=function(e) {},
            warning=function(w) {}
            );
  tryCatch(ppe.shutdown(),
           error=function(e) {},
           warning=function(w) {}
           );
  tryCatch(ppe.disconnectNetworkManager(),
           error=function(e) {},
           warning=function(w) {}
           );
}

#' Closes connection to the network manager.
#'
#' @author Barnet Wagman
#' @export
ppe.disconnectNetworkManager <- function() {
  
  if ( exists("conNetworkManager",envir=.GlobalEnv) ) {
    tryCatch(close(conNetworkManager),
             error=function(e){},warning=function(w){});
  }
  
  if ( exists("conNetworkManager",envir=.GlobalEnv) ) {
    tryCatch(rm(conNetworkManager,envir=.GlobalEnv),
           error=function(e){},warning=function(w){});
  }
}


#' launchPortForwardingApp
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param port
#' @param timeout
#' @param verbose
launchPortForwardingApp <- function(port,timeout,verbose) {
  
  if ( clientAppIsRunning(port=port,timeout=timeout) ) {
    con <- getConPortForwardingApp(port = port, timeout = timeout);    
    if ( !isGoodCon(con) ) stop(paste("Unable to connect to port forwarding app on port=",
                                      port));
    if ( verboseOn() ) print(paste("The portforwarding server app is already running, ",
                                   "port=",port,sep=""));
    return();
  }
  else {    
    cmd <- paste("java -classpath ",
                 getClassPath(),
                 " cloudrmpi.PortForwardingServer",
                 " port=",port,             
                 " verbose=",tolower(paste(verbose)),
                 sep=""
                 );
    if ( verboseOn() ) print(paste("cmd=",cmd));
    
    system(cmd,intern=FALSE,wait=FALSE,ignore.stderr=TRUE,ignore.stdout=TRUE);
    if ( verboseOn() ) print(paste("system:",cmd));      
    
    tryCatch(con <- getConPortForwardingApp(port=port,timeout=timeout),
             error=function(e) {},
             warning=function(w) {}
             );
    if ( !isGoodCon(con) ) stop(paste("Unable to connect to port forwarding app on port=",
                                      port));
  }
}

#' Gets existing connection or connects if possible.
#' If a new connection
#' is made, it is assign to conPortForwardingApp in .GlobalEnv as
#' well as being returned.
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param port
#' @param timeout
#' @param maxWait
getConPortForwardingApp <- function(port,timeout,maxWait=10) {
  
  if ( exists(x="conPortForwardingApp",envir=.GlobalEnv) ) {
    con <- get(x="conPortForwardingApp",envir=.GlobalEnv);
    if ( isGoodCon(con) ) return(con);
  }
  else { # Try to connect
    con <- connectToPF(port=port,timeout=timeout,maxWait=maxWait);
    if ( isGoodCon(con) ) {
      assign(x="conPortForwardingApp",value=con,envir=.GlobalEnv);
      return(con);
    }
    else stop(paste("Cannot connect to the port forwarding app on port=",port));
  }
}  

#' connectToPF
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param port
#' @param timeout
#' @param maxWait
connectToPF <- function(port,timeout,maxWait=10) {
  tmFin <- Sys.time() + maxWait;
  while ( tmFin >= Sys.time() ) {
    con <- tryCatch(conToPF(port=port,timeout=timeout),
                    error=function(e) {
                      if ( verboseOn() ) print(paste("conNM error=",e));
                      closeEh(con);
                      return(NULL); 
                    },
                    warning=function(w) {}
                    );
    if ( !is.null(con) && isGoodCon(con) ) {
      return(con);
    }
    else { Sys.sleep(0.1); }
  }
  stop(paste("Timed out connecting to the port forwarding app on port=",port));   
}  

#' Wrapper for socketConnection(), so we can catch exceptions.
#' @keywords internal
#' @export
#' @author Barnet Wagman
#' @param port
#' @param timeout
conToPF <- function(port,timeout) {
  socketConnection(port = port, server = FALSE, blocking = TRUE, 
                   open = "a+", timeout = timeout)
}

#' testPFMessaging
#' @keywords internal
#' @export
#' @author Barnet Wagman
testPFMessaging <- function() {
  con.pf <- getConPortForwardingApp();
  cmd <- createAppCmd(cmdName="test",params=c("message=The_test_message"));
  r <- sendAppCmd(con=con.pf,cmd=cmd);
  print(r);  
}

