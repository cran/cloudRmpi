\docType{package}
\name{cloudRmpi}
\alias{cloudRmpi}
\alias{cloudRmpi-package}
\title{Cloud-based MPI Parallel Proccessing for R (cloudRmpi)}
\description{
  Cloud-based MPI Parallel Proccessing for R (cloudRmpi)
}
\details{
  \tabular{ll}{ Package: \tab cloudRmpi\cr Type: \tab
  Package\cr Version: \tab 1.0--\cr Date: \tab
  2012-01-24\cr License: \tab GPL (>= 3)\cr }

  cloudRmpi is means for doing parallel processing in R,
  using MPI on a cloud-based network.  It currently
  supports the use of Amazon's Ec2 cloud computer service.
  cloudRmpi provides the means to launch and manage a
  cloud-based network and to access the an R session on
  network's master MPI node (using the rreval package which
  is imported by cloudRmpi). cloudRmpi should work with any
  MPI based R package (it has been tested with Rmpi,
  npRmpi, and snow).
}
\author{
  Barnet Wagman \email{bw@norbl.com}
}
\keyword{package}

