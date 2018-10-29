## 1.2.7 and 1.28

- Debug flag to print out extra information

## 1.2.6

- Creates output directory if it does not already exist

## 1.2.5

- Can get date from git version 1
  * Useful on older systems
- Gracefully handles exception when parsing date fails

## Version 1.2.3 and 1.2.4

- Properly closing stream. No more error messages
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

- changed parameter names to be more consitent with Gradle
- date can now specify time zone
- unix time is included