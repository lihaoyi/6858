The World's Most Advanced Sandbox™
=================================

*The World’s Most Advanced Sandbox™* (TWMAS) is a portable sandbox designed to safely run untrusted Java bytecode. Unlike traditional sandboxes, many of which use non-portable operating system (OS) facilities to run the untrusted code in a separate process with restricted privileges, TWMAS does not make use of the OS. Rather, it provides a way for a host Java Virtual Machine (JVM) to execute untrusted code directly, using the Java SecurityManager to block access to dangerous capabilities (Filesystem, Network, System, etc.) and instruction-rewriting in order to bound the number of bytes of memory allocated and the number of instruction executed.

A full explanation can be found [here](https://docs.google.com/document/d/1-gFHzZR0X8cDG6CWDgktRAs4pcvxHOQKFiUZ9_2mUhE/edit#)

The bulk of the functionality is in code is in [src/sandbox](6858/tree/master/src/sandbox):

- [src/main/sandbox/agent](6858/tree/master/src/main/sandbox/agent) contains the code for attaching an javaagent to the JVM on startup.
- [src/main/sandbox/classloader](6858/tree/master/src/main/sandbox/classloader) contains a custom classloader that measures the size of different objects
- [src/main/sandbox/instrumentation](6858/tree/master/src/main/sandbox/instrumentation) contains the code which using the [ASM bytecode library](http://asm.ow2.org/) to instrument the bytecode
- [src/main/sandbox/runtime](6858/tree/master/src/main/sandbox/runtime) contains the classes needed at run-time to support the functionality of the sandbox (e.g. the method calls which account for instruction/memory usage)
- [src/test/sandbox](6858/tree/master/src/test/sandbox) contains the test script which runs the unit tests and exercises the functionality of the sandbox
- [resources](6858/tree/master/resources) contains the Java source files for our demo applications which are exercised by our tests and referenced in our report.

The test cases can be run using [Apache Ant](http://ant.apache.org/), via `ant run`.


