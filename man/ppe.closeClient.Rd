\name{ppe.closeClient}
\alias{ppe.closeClient}
\title{Close the connection to the master node}
\usage{
  ppe.closeClient(hostName = NULL, userName = "ec2-user",
    networkName = NULL, verbose = FALSE)
}
\arguments{
  \item{hostName}{the name of host that is the master node
  of the ppe-ompi network; usually NULL (the default). If
  this param is NULL and the network name is NULL, the
  function will attempt to retrieve a connection from the
  connection manager environment.  This param needs to be
  supplied only if you have more than one ppe-ompi network
  running.}

  \item{networkName}{name of a ppe-ompinetwork; usually
  NULL (the default). If this param is NULL and the
  hostName is NULL, the function will attempt to retrieve a
  connection from the connection manager environment.  This
  param is only used if you have more than one ppe-ompi
  network running; in that case it is an alternative to
  specifying the hostName.}

  \item{userName}{name of user on the master node host
  under which R is running.  By default the user name is
  "ec2-user" and you do not need to specify this
  parameter.}

  \item{verbose}{for debugging.}
}
\description{
  ppe.closeClient() closes the connection to the R session
  on the master node that was created by ppe.startClient().
  After this function is called, the R session on the
  master node can accept a connection from another client.
}
\author{
  Barnet Wagman
}

