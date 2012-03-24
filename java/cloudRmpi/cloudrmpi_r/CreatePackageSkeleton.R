# Creates the R package skeleton.  This code should only be called once.
# WARNING: it creates templates for everything, including DESCRIPTION.
# To propagate changes in code or documentation, use the ant script.

# WARNING: package files CANNOT contains source() commands that refer to files 
# in the package.  E.g. RReClient.R cannot contains 'source("RReMessage.R")'

setwd("/home/moi/h/ppe/projects/cloudrmpi_201202/src/cloudrmpi_r")

package.skeleton(name="cloudRmpi",
                 path="/home/moi/h/ppe/cloudRmpi_package_factory",
                 code_files=c("cloudRmpi-package.R", 
                              "PPEClient.R"
                              ),
                 force=TRUE
                 )

# ^ Note that rreval-package.R is really a doc file. roxygen will turn into an .Rd