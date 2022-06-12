Custom Images
=========

TODO basic description of plugin use

* Bullet points
* Of plugin use cases

Images is open source! We welcome contributions; the project is licensed using the MIT License.

Compiling
---------

1. Ensure that you have the project setup properly with Maven
2. Compile from the parent project of Images that all modules are contained within
3. Run `mvn clean package`

Compiling Dependencies
---------

You might see maven builds fail due to missing dependencies on, e.g. spigot-api-1.19-R0.1. 
These dependencies are not available on maven central, and as such you will need to build and install them 
in your local maven repository. To do this follow these steps.

1. Download the spigotmc BuildTools.jar from https://hub.spigotmc.org/jenkins/job/BuildTools/.
2. Familiarize yourself with https://www.spigotmc.org/wiki/buildtools/.
3. Run `java -jar BuildTools.jar --compile SPIGOT --remapped -rev 1.19` from inside the foler where you palced BuildTools.jar
4. Adapt rev to any version you need the dependency for.

You might have to switch Java installations in between as different spigot versions rely on different Java versions.

Contributing
------------

We happily accept contributions, especially through pull requests on GitHub.
Submissions must be licensed under the MIT License.

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for important guidelines to follow.

Links
-----

* [Visit our plugin page](https://www.spigotmc.org/resources/custom-images.53036/)