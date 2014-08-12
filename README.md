tracelogger
===========
This library helps to avoid errors and helps to debug errors during the development of complex software systems on the 
Java Virtual Machine (JVM) by providing clearly arranged (trace-)logs. That is, in contrast to conventional loggers the 
call stack of the to be observed methods is presented by indenting the trace messages dependent on the actual depth of the call stack. 
A distinction is made between fine granular trace messages (entering and exiting of methods, debugging output) and conventional log 
messages (error, warning and info notifications). For production the latter ones can be redirected to a conventional logging system like 
log4j whereas the trace messages will be discarded. Several strategies to access tracers can be employed. 
Tracers can be accessed by name, can be mapped on threads and be retrieved by invoking a blocking queue. 
Unlike conventional java loggers, these tracers aren't linked with the package hierarchy but rather linked with threads.

Note that the mentioned call stack contains only the to be observed methods and is therefore separate from the call stack managed by
the JVM. At every push and pop of a method image a corresponding entry together with the class name and the system ID of the owning 
object will be generated within the trace-log. In the event of a pop the consumed (CPU-)time will additionally be quoted.

The sequential line-oriented view of conventional loggers is working against the generation of meaningful dumps of complex data structures.
However the tracers provided by this library grant direct access to [PrintStream](http://docs.oracle.com/javase/8/docs/api/java/io/PrintStream.html)s
which can be used in the usual manner.
