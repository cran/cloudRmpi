\name{ppe.terminateNetwork}
\alias{ppe.terminateNetwork}
\title{Terminates EC2 instances}
\usage{
  ppe.terminateNetwork(networkName = NULL)
}
\arguments{
  \item{networkName}{the name of a ppe-ompi network;
  usually NULL (the default). If this param is NULL the
  function will attempt to retrieve a connection from the
  connection manager environment. This param needs to be
  supplied only if you have more than one ppe-ompi network
  running. Use getNetworkNames() to get a list of all
  networks.}
}
\value{
  message (character).
}
\description{
  Termimates all EC2 instances in the specified network.
  This can also be done via the ppe-ompi network
  managerment application.
}
\author{
  Barnet Wagman
}

