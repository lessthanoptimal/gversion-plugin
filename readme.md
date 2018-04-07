gversion-plugin is a Gradle plugin for auto generating a version java class. The class will include information on the gradle version and GIT version. This information is collected from Gradle and by invoking commandline commands. If the commandline cannot be access it will gracefully fail.

To include the plugin in your project

```groovy
plugins {
  id "com.peterabeles.gversion" version "1.0.1"
}
 
gversion {
  gversion_file_path = "src/main/java/"
  gversion_package = "com.your.package"
  gversion_class_name = "MyVersion"      // optional. If not specified GVersion is used
  date_format = "yyyy-MM-dd HH:mm:ss"    // optional. This is the default
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
	public static final String BUILD_DATE = "2018-04-07 09:43:42";
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

To install your own custom version locally type ```./gradlew install```

## Contact

This plugin is written by Peter Abeles and has been released into the Public Domain. Use github to post bugs and feature requests.