\name{ppe.getMasterNodeURL}
\alias{ppe.getMasterNodeURL}
\title{Get the URL of the ppi master node in the specfied network.}
\usage{
  ppe.getMasterNodeURL(networkName = NULL)
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
  URL (character).
}
\description{
  Get the URL of the ppi master node in the specfied
  network.
}
\author{
  Barnet Wagman
}

