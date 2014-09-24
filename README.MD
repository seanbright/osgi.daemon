# OSGi Daemon Launcher

Uses the [Apache Commons Daemon](http://commons.apache.org/proper/commons-daemon/)
library to launch any R4.2 compliant OSGi Framework.

The JAR implementing the Framework specification must be on the classpath.  If
multiple Framework specifications are on the classpath, the first one returned
by the [`ServiceLoader`](http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html)
implementation is used.

The [`example`](https://github.com/seanbright/osgi.daemon/tree/master/init.d/example)
init script has been tested on Ubuntu 14.04 (Trusty) with `jsvc` installed from
packages.  Just make any necessary changes to it, drop it in `/etc/init.d` and
run `update-rc.d whatever-you-called-the-file defaults`.

## Requirements

Requires at least Java 1.6 due to its dependence on the the `java.util.ServiceLoader`
class.

## License
OSGi Daemon Launcher is licensed under the
[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Bugs
Lots and lots, I'm sure.  Patches welcome.
