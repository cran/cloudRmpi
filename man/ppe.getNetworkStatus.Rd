\name{ppe.getNetworkStatus}
\alias{ppe.getNetworkStatus}
\title{Get the status of a network}
\usage{
  ppe.getNetworkStatus(networkName = NULL)
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
  status message (character).
}
\description{
  Get the status of a network
}
\author{
  Barnet Wagman
}

