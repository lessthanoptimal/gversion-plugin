## 1.10.1

- Fixed checkDependsOnSNAPSHOT so that it actually works in Gradle 7
- Upgraded build for Gradle 7

## 1.10

- checkDependsOnSNAPSHOT will not crash is there's no 'compile'
- If debug is true then it will print out the configurations or if there are no configurations

## 1.9

- There's now a flag to include the @Generated annotation. Java only for now.
- Java now have a return character at the end of the file

## 1.8.1 - 1.8.3

- Ensure that jars are built using Java 1.8 bytecode even if a newer JDK is used
- Fixed issue where console output wasn't fully read due to buffering

## 1.8.0

- Added current git branch to information
  * Thanks Blunderchips for the suggestion!
- Might have finally fixed that annoying error message
- Added YAML as a language
- Added ability to change the indent from tab to something else

## 1.7.0

- Added failDirtyNotSnapshot() Task

## 1.6.0 and 1.6.2

- Now indicates if the build is dirty, i.e. has uncommitted changes
  * Thanks rbresalier for the suggestion!

## 1.5.2

- Fixed issue when git has a longer version

## 1.5.1

- Java creates a private constructor to hide implicit public one

## 1.5

- Changes to file path
  * Now it is relative to the sub project it is called in
  * Global paths are now respected

## 1.4.1

- Updated Kotlin based on feedback
  * By default, types are not printed. This is configurable by setting the explicitType flag

## 1.4.0

- Support for multiple languages. Java and Kotlin

## 1.3.0

- For some unknown reason on an odroid device the working directory was
  not the project directory and instead the daemon's directory.

## 1.2.7 to 1.2.9

- Debug flag to print out extra information

## 1.2.6

- Creates output directory if it does not already exist

## 1.2.5

- Can get date from git version 1
  * Useful on older systems
- Gracefully handles exception when parsing date fails

## Version 1.2.3 and 1.2.4

- Properly closing stream. No more error messages.
- Added time out on running a process
- NOTE: You might need to kill the Gradle daemon to make it go away. 
  It seems to be caching the old plugin and using that instead of the new one
  ./gradlew --kill

## Version 1.2.2

- Found a scenario where the previous fix failed
  * Now correctly gets the root project

## Version 1.2.1

- Can be invoked from any project directory now
  * Project path is already relative to the project root directory

## Version 1.2

- Changed software license to be Unlicense and MIT
- Added date the git commit was made at

## Version 1.1

- changed parameter names to be more consistent with Gradle
- date can now specify time zone
- unix time is included
