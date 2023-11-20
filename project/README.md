Some of the Scala files in this directory are soft links
to the file in the src/main/scala/com/ossuminc/sbt/helpers 
directory. This is done so that the plugin build can use
some of the features of the plugin itself. Generally, don't
open these files from this path, but from the src directory
path instead. 
