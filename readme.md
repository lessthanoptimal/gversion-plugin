gversion-plugin is a Gradle plugin for auto generating a version java class. The class will include information which can only be obtained at compile time, such as build time, git SHA, and Gradle version. 
Command line applications are used to gather most of this information. If a command line operation fails a default value will be used instead. This has been tested in Linux, Windows, and Mac OS X.

Add the following to include gversion-plugin in your project.

```groovy
plugins {
  id "com.peterabeles.gversion" version "1.2"
}
 
gversion {
  srcDir       = "src/main/java/"
  classPackage = "com.your.package"
  className    = "MyVersion"                // optional. If not specified GVersion is used
  dateFormat   = "yyyy-MM-dd'T'HH:mm:ss'Z'" // optional. This is the default
  timeZone     = "UTC"                      // optional. UTC is default
}
```

After you invoke the Gradle task 'createVersionFile' it will create a class at src/main/java/com/your/project/MyVersion.java.
```java
/**
 * Automatically generated file containing build version information.
 */
public class MyVersion {
	public static final String MAVEN_GROUP = "com.your";
	public static final String MAVEN_NAME = "project_name";
	public static final String VERSION = "1.0-SNAPSHOT";
	public static final int GIT_REVISION = 56;
	public static final String GIT_SHA = "a0e41dd1a068d184009227083fa6ae276ef1846a";
	public static final String GIT_DATE = "2018-04-10T16:26:44Z";
	public static final String BUILD_DATE = "2018-04-11T12:19:03Z";
	public static final long BUILD_UNIX_TIME = 1523449143116L;
}
```

To ensure that your version file is always up to date it's recommended that you invoke this task before the project is compiled.

For a Java project you can do the following:
```groovy
project.compileJava.dependsOn(createVersionFile)
```

For an Android project here's how you shouold do it:
```groovy
project(':app').preBuild.dependsOn(createVersionFile)
```

## Tasks

A complete list of tasks that it adds is shown below.

| Task Name   |   Description   |
| ------------|-----------------|
| createVersionFile      | Creates the version file |
| checkForVersionFile    | Checks to see if the version file has been created and throws an exception if not |
| checkDependsOnSNAPSHOT | If not a SNAPSHOT itself it will fail if there are any dependencies on snapshots  |


## Developers

A few changes need to be done to work off a locally installed version. In the project that will be using the plugin,
change the plugin import statement to:
```groovy
apply plugin: 'com.peterabeles.gversion'

buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath "com.peterabeles.gversion:gversion:$VERSION"
    }
}
```
To install your own custom version locally type ```./gradlew install``` and it should appear in your local .m2 repo

## Contact

This plugin is written by Peter Abeles and has been released under a dual license: Public Domain and MIT. Please use github to post bugs and feature requests.