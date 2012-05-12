\name{ppe.connectToRStudio}
\alias{ppe.connectToRStudio}
\title{Connect to RStudio server.}
\usage{
  ppe.connectToRStudio(hostName = NULL,
    userName = "ec2-user", networkName = NULL,
    pemFile = NULL, portPfApp = 4470, portRemote = 8787,
    portLocal = 8787, portNetworkManager = 4461,
    timeout = 2, verbose = FALSE)
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

  \item{portPfApp}{number of the port used for
  communications with cloudrmpi.PortForwardingServer, the
  Java app that handles manages port forwarding.}

  \item{portRemote}{number of port that RStudio server is
  using, 8787 on cloudRmpi AMIs that support RStudio.}

  \item{portLocal}{number of the local port that is
  forwarded to the remote port (that the RStudio server is
  using on the remote node).  This number may be the same
  as portRemote.}

  \item{portNetworkManager}{port number of port used for
  communcations with the network manager.}

  \item{timeout}{socket timeout (in seconds).}

  \item{verbose}{for debugging.}
}
\description{
  RStudio server is an IDE for R that runs in a web
  browser.  This function connects to an instance of
  RStudio server running on the master node of an EC2
  network.
}
\details{
  The function first start creates a connection to the
  master node via ssh port forwarding and then launches
  your default web browser displaying an RStudio server
  session.  To use RStudio, you will need to login with
  Username=rsu, Password=rsu.  Note that this is actually
  quite secure.  Access to the remote host is via ssh
  (using your keypair), the RStudio login is superflous
  (but impossible to disable). See the CloudRmpi manual for
  details.

  Note that the RStudio session is logged in as user 'rsu'
  and initially the working directory is /home/rsu.  Due to
  constraints imposed by the ssh and the RStudio login
  mechanism, as user rsu you will not be able to access the
  /home/ec2-user. And you will not be able to ssh to the
  system as rsu only as ec2-user.  However, both RStudio
  and ppe-ompi network manager provide shell access to the
  host. Once in shell, you can use sudo or su (neither
  require passwords).  Or you can ssh to the host as
  ec2-user and the use sudo or su. See the cloudRmpi manual
  for details.
}
\author{
  Barnet Wagman
}

