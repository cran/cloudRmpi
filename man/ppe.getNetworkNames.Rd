\name{ppe.getNetworkNames}
\alias{ppe.getNetworkNames}
\title{Get network names}
\usage{
  ppe.getNetworkNames()
}
\value{
  character vector of network names.
}
\description{
  Get the names of all ppe-ompi networks owned by the user.
  Note this may include networks that are in the process of
  launching or that have recently terminated.  Use
  getNetworkStatus() to determine whether the network is
  available (or refer to the ppe-ompi network management
  application).
}
\author{
  Barnet Wagman
}

