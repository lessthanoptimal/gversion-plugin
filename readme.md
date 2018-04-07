gversion-plugin is a Gradle plugin for auto generating a version java class. The class will include information on the gradle version and GIT version. This information is collected from Gradle and by invoking commandline commands. If the commandline cannot be access it will gracefully fail.

To include the plugin in your project

```groovy
apply plugin: 'com.peterabeles.gversion'
 
gversion {
    gversion_file_path = "src/main/java/"
    gversion_package = "com.your.package"
    gversion_class_name = "MyVersion"      // optional. If not specified GVersion is used
    date_format = "yyyy-MM-dd HH:mm:ss"    // optional. This is the default
}
 
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath "com.peterabeles:gversion:1.0"
    }
}
```

This will create a version file at src/main/java/com/your/project/MyVersion.java

## Tasks

| Task Name   |   Description   |
| ------------|-----------------|
| createVersionFile      | Creates the version file |
| checkForVersionFile    | Checks to see if the version file has been created and throws an exception if not |
| checkDependsOnSNAPSHOT | If not a SNAPSHOT itself it will fail if there are any dependencies on snapshots  |

## Automatic Invoking

To ensure that your version file is always up to date it's recommended that you make a task that's called before compile invokes it.

For a Java project you can do the following:
```groovy
project.compileJava.dependsOn(createVersionFile)
```

For an Android project here's how you shouold do it:
```groovy
project(':app').preBuild.dependsOn(createVersionFile)
```
## Contact

This plugin is written by Peter Abeles and has been released into the Public Domain. Use github to post bugs and feature requests.