\name{ppe.shutdown}
\alias{ppe.shutdown}
\title{Shutdown connections}
\usage{
  ppe.shutdown()
}
\description{
  Close all open connections to master nodes,terminates the
  ppe-ompi access application, terminates port forwarding
  to RStudio Server, and disconnects from the network
  manager.
}
\note{
  This function does not terminate ec2 instances.
}
\author{
  Barnet Wagman
}

