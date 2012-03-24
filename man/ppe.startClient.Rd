\name{ppe.startClient}
\alias{ppe.startClient}
\title{Start ppe-ompi client}
\usage{
  ppe.startClient(hostName = NULL, userName = "ec2-user",
    networkName = NULL, pemFile = NULL, portRJ = 4460,
    portJJ = 4464, portPPEManager = 4461, timeout = 2,
    verbose = FALSE)
}
\arguments{
  \item{hostName}{the name of host that is the master node
  of the ppe-ompi network; usually NULL (the default). If
  this param is NULL and the network name is NULL, the
  function will attempt to retrieve a connection from the
  connection manager environment.  This param needs to be
  supplied only if you have more than one ppe-ompi network
  running.}

  \item{userName}{name of user on the master node host
  under which R is running.  By default the user name is
  "ec2-user" and you do not need to specify this
  parameter.}

  \item{networkName}{name of a ppe-ompi network; usually
  NULL (the default). If this param is NULL and the
  hostName is NULL, the function will attempt to retrieve a
  connection from the connection manager environment.  This
  param is only used if you have more than one ppe-ompi
  network running; int that case it is an alternative to
  specifying the hostName.}

  \item{pemFile}{RSA keypair file used to access to master
  node of the ppe-ompi network.  If NULL, the function
  obtains the file's pathname from the network manager.
  Normally you do not need to specify this parameters.}

  \item{portRJ}{number of the port used for communications
  with rreval.RReClientApp, the Java app that handles
  communications with the remote server on the master node
  of the ppe-ompi network. (rreval.RReClientApp is launched
  by ppe.startClient() if it is not already running.)}

  \item{portJJ}{number of the port used by the java app
  rreval.RReClientApp to communicate with the the
  rreval.RReServerApp, the server side java app.}

  \item{portPPEManager}{port number of port used for
  communcations with the network manager.}

  \item{timeout}{socket timeout (in seconds).}

  \item{verbose}{for debugging.}
}
\description{
  ppe.startClient() creates a connection to an R session
  running on the master node of a ppe-ompi network.
}
\details{
  Note that the connection (which is a socket connection)
  is not returned by ppe.startClient() - it is stored in an
  environment dedicated to managing connections.  cloudRmpi
  functions retrieve connections based based on a network's
  name or the name of a network's master node. You do not
  need to use connection objects directly.

  The connection ppe.startClient() creates is actually to
  locally running java app,rreval.RReClientApp.  If the app
  is not running, ppe.startClient() will launch it.
}
\author{
  Barnet Wagman
}

