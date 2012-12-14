*The World’s Most Advanced Sandbox™* (TWMAS) is a portable sandbox designed to safely run untrusted Java bytecode. Unlike traditional sandboxes, many of which use non-portable operating system (OS) facilities to run the untrusted code in a separate process with restricted privileges, TWMAS does not make use of the OS. Rather, it provides a way for a host Java Virtual Machine (JVM) to execute untrusted code directly, using the Java SecurityManager to block access to dangerous capabilities (Filesystem, Network, System, etc.) and instruction-rewriting in order to bound the number of bytes of memory allocated and the number of instruction executed.

A full explanation can be found [here](https://docs.google.com/document/d/1-gFHzZR0X8cDG6CWDgktRAs4pcvxHOQKFiUZ9_2mUhE/edit#)

The bulk of the functionality is in code is in [src/sandbox](src/sandbox):

- [src/main/sandbox/agent](src/main/sandbox/agent) contains the code for attaching an javaagent to the JVM on startup.
- [src/main/sandbox/classloader](src/main/sandbox/classloader) contains a custom classloader that measures the size of different objects
- [src/main/sandbox/instrumentation](src/main/sandbox/instrumentation) contains the code which using the [ASM bytecode library](http://asm.ow2.org/) to instrument the bytecode
- [src/main/sandbox/runtime](src/main/sandbox/runtime) contains the classes needed at run-time to support the functionality of the sandbox (e.g. the method calls which account for instruction/memory usage)
- [src/test/sandbox](src/test/sandbox) contains the test script which runs the unit tests and exercises the functionality of the sandbox

The test cases, which are Java source files which get compiled-on-the-fly executed within the sandbox, are in [resources](resources)

The test cases can be run using [Apache Ant](http://ant.apache.org/), via `ant run`.


