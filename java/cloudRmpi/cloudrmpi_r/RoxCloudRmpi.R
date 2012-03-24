# This file calls reoxygenize to create .Rd files from the source files.
# It should be used from within the ant script only.

# NOTE/WARNING: reoxygenize creates an cloudRmpi.Rd file from cloudRmpi-package.R 
# (which is really just doc, despite it being structured as src).
# BUT package.skeleton turns cloudRmpi-package.R into a 'template' 
# cloudRmpi-package.Rd file. This template will generate a terminal error when a 
# package is installed.  (or in 'R CMD check rreval').
# SO rreval-package.Rd must be deleted after roxygenize, before we build the 
# package.

library(roxygen2)

roxygenize(package.dir="/home/moi/h/ppe/cloudRmpi_package_factory/cloudRmpi",
           roxygen.dir="/home/moi/h/ppe/cloudRmpi_package_factory/cloudRmpi",
           copy.package=FALSE,#TRUE,#FALSE,
           unlink.target=FALSE,
           overwrite=TRUE
           )

