gversion-plugin is a Gradle plugin for auto-generating a version class/file in multiple JVM Languages. Support provided 
for Java, Kotlin, and YAML. The class will include information which can only be obtained at compile time, such as 
build time, git SHA, and Gradle version. Command line applications are used to gather most of this information. 
If a command line operation fails a default value will be used instead. This has been tested in Linux, Windows, and Mac OS X.

Add the following to include gversion-plugin in your project.

```groovy
plugins {
  id "com.peterabeles.gversion" version "1.9"
}
 
gversion {
  srcDir       = "src/main/java/"           // path is relative to the sub-project by default
                                            // Gradle variables can also be used
                                            // E.g. "${project.rootDir}/module/src/main/java"
  classPackage = "com.your.package"
  className    = "MyVersion"                // optional. If not specified GVersion is used
  dateFormat   = "yyyy-MM-dd'T'HH:mm:ss'Z'" // optional. This is the default
  timeZone     = "UTC"                      // optional. UTC is default
  debug        = false                      // optional. print out extra debug information
  language     = "java"                     // optional. Can be Java, Kotlin, or YAML. Case insensitive.
  explicitType = false                      // optional. Force types to be explicitly printed
  indent       = "\t"                       // optional. Change how code is indented. 1 tab is default.
  annotate     = false                      // optional. Java only. Adds @Generated annotation
}
```


After you invoke the Gradle task 'createVersionFile' it will create a class at src/main/java/com/your/project/MyVersion.java.
```java
/**
 * Automatically generated file containing build version information.
 */
public final class MyVersion {
	public static final String MAVEN_GROUP = "com.your";
	public static final String MAVEN_NAME = "project_name";
	public static final String VERSION = "1.0-SNAPSHOT";
	public static final int GIT_REVISION = 56;
	public static final String GIT_SHA = "a0e41dd1a068d184009227083fa6ae276ef1846a";
	public static final String GIT_DATE = "2018-04-10T16:26:44Z";
	public static final String GIT_BRANCH = "master";
	public static final String BUILD_DATE = "2018-04-11T12:19:03Z";
	public static final long BUILD_UNIX_TIME = 1523449143116L;
	public static final int DIRTY = 0;
	private MyVersion(){}
}
```
DIRTY indicates if there are uncommitted versioned files in your repository. 0 means it's clean, 1 means it's
dirty, and -1 means something went wrong.

To ensure that your version file is always up to date it's recommended that you invoke this task before the project is compiled.

For a Java project you can do the following:
```groovy
project.compileJava.dependsOn(createVersionFile)
```

For an Android project here's how you should do it:
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
| failDirtyNotSnapshot   | Throws exception if git repo is dirty AND version does not end with SNAPSHOT      |

## Build Time Optimization

An unintended consequence of automatically generating source code every time you build is that it will make the
project dirty and force Gradle to be rebuilt it! For smaller projects you will probably not notice this, but for larger
projects that are broken up into many sub-projects and take minutes to build you might want to consider placing 
auto generated code in a different separate sub-project not depended on by other sub-projects or switching to the 
YAML version and read it at runtime.

## Developers

A few changes need to be done to work off a locally installed version. In the project using the plugin,
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

This plugin is writen by Peter Abeles and has been released under a dual license: Public Domain and MIT. Please use github to post bugs and feature requests.
