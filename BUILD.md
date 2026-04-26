You need:
* jdk-21 (e.g. `sudo apt install openjdk-21-jdk`)
* maven (e.g. `sudo apt install maven`)

Note tests are currently not fully working.

To buid:
```
cd imagej
mvn -Denforcer.skip -Dmaven.test.skip=true install
``
