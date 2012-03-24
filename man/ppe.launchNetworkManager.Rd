\name{ppe.launchNetworkManager}
\alias{ppe.launchNetworkManager}
\title{Launch the ppe-ompi network manager application.}
\usage{
  ppe.launchNetworkManager(port = 4461, timeout = 2,
    verbose = FALSE)
}
\arguments{
  \item{port}{number of port used for communcations with
  the network manager.}

  \item{timeout}{socket timeout (in seconds).}

  \item{verbose}{for debugging.}
}
\description{
  The network manager is a gui java app used for launching,
  monitoring and managing networks of EC2 instances that
  supports R parallel processing ('ppe' is short for
  'parallel processing with EC2').
}
\author{
  Barnet Wagman
}

