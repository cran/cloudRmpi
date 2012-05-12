\name{ppe.disconnectRStudio}
\alias{ppe.disconnectRStudio}
\title{Disconnect from RStudio server.}
\usage{
  ppe.disconnectRStudio(hostName = NULL,
    userName = "ec2-user", networkName = NULL,
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

  \item{verbose}{for debugging.}
}
\description{
  Disconnects from the RStudio server on the master node of
  the EC2 network and terminates port forwarding.
}
\author{
  Barnet Wagman
}

