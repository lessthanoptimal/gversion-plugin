package com.peterabeles

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownProjectException

import java.text.DateFormat
import java.text.SimpleDateFormat

enum Language {
    YAML,
    JAVA,
    KOTLIN,
    PROPERTIES;

    static Language convert(String name) {
        switch (name.toUpperCase()) {
            case "JAVA": return JAVA
            case "KOTLIN": return KOTLIN
            case "YAML": return YAML
            case "PROPERTIES": return PROPERTIES
        }
        throw new IllegalArgumentException("Unknown language. " + name)
    }
}

class GVersionExtension {
    String srcDir
    String classPackage = ""
    String className = "GVersion"
    String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    String timeZone = "UTC"
    String language = "Java"
    String indent = "\t"
    // If the language allows implicit types should it explicitly state the types?
    boolean explicitType = false
    // Prints additional debug information
    boolean debug = false
    // Adds an annotation indicating the file is auto generated
    boolean annotate = false
}

class GVersion implements Plugin<Project> {

    GVersionExtension extension

    static String gversion_file_path(Project project, GVersionExtension extension) {
        if (extension.srcDir == null)
            throw new RuntimeException("Must set gversion_file_path")

        String relative_path

        if (!new File(extension.srcDir).isAbsolute()) {
            if (extension.debug) println("path relative to sub-project")
            // relative to sub-project
            relative_path = project.file(".").path
        } else {
            if (extension.debug) println("absolute path")
            // relative to system
            relative_path = ""
        }

        File gversion_file_path = new File(extension.srcDir,
                extension.classPackage.replace(".", File.separator))
        return new File(relative_path, gversion_file_path.path).getPath()
    }

    int executeGetSuccess(String command) {
        def proc = command.execute(null, new File(System.getProperty("user.dir")))
        try {
            proc.consumeProcessOutput()
            proc.waitForOrKill(5000)
            return proc.exitValue()
        } catch (IOException e) {
            if (extension.debug) {
                e.printStackTrace(System.err)
            }
            return -1
        }
    }

    String executeGetOutput(String command, String DEFAULT) {
        def proc = command.execute(null, new File(System.getProperty("user.dir")))
        try {
            def output = new ByteArrayOutputStream(4096)
            proc.consumeProcessOutput(output, new ByteArrayOutputStream(4096))
            proc.waitForOrKill(5000)
            def text = output.toString().trim()
            if (text.isBlank()) {
                if (extension.debug) {
                    System.err.println("command returned an empty string")
                    System.err.println("pwd     = " + new File(".").getAbsolutePath())
                    System.err.println("command = " + command)
                }
                return DEFAULT
            }
            if (proc.exitValue() != 0) {
                if (extension.debug) {
                    System.err.println("command returned non-zero value: " + proc.exitValue())
                    System.err.println("pwd     = " + new File(".").getAbsolutePath())
                    System.err.println("command = " + command)
                    System.err.println("output  = " + text)
                }
                return DEFAULT
            }
            return text
        } catch (IOException e) {
            if (extension.debug) {
                e.printStackTrace(System.err)
            }
            return DEFAULT
        }
    }

    static int[] parseGitVersion(String text) {
        int[] version = new int[3]

        if (text == null)
            return version
        String[] words = text.split("\\s+")
        if (words.length != 3)
            return version

        words = words[2].split("\\.")
        if (words.length < 1)
            return version

        for (int i = 0; i < Math.min(version.length, words.length); i++) {
            version[i] = Integer.parseInt(words[i])
        }

        return version
    }

    void apply(Project project) {
        // Add the 'greeting' extension object
        extension = project.extensions.create('gversion', GVersionExtension)

        project.ext.checkProjectExistsAddToList = { whichProject, list ->
            try {
                project.project(whichProject)
                list.add(whichProject)
            } catch (UnknownProjectException ignore) {
            }
        }

        // Force the release build to fail if it depends on a SNAPSHOT
        project.task('checkDependsOnSNAPSHOT') {
            doLast {
                if (project.version.endsWith("SNAPSHOT")) {
                    if (extension.debug)
                        println("Skipping checkDependsOnSNAPSHOT. Project is a SNAPSHOT!")
                    return
                }

                // Print out a warning if this is doing nothing
                if (extension.debug && project.configurations.isEmpty())
                    println("WARNING: checkDependsOnSNAPSHOT: No configurations to check!")

                project.configurations.findAll { config ->
                    config.isCanBeResolved() && (config.name.startsWith("compile") || config.name.startsWith("runtime"))
                }.each { a ->
                    if (extension.debug)
                        println("checkDependsOnSNAPSHOT: project.configurations=" + a.name)

                    a.each {
                        if (it.toString().contains("SNAPSHOT"))
                            throw new Exception("Release build contains snapshot dependencies: " + it)
                    }
                }
            }
        }

        // Throw an exception if the repo is dirty and it's a release version
        project.task('failDirtyNotSnapshot') {
            doLast {
                if (project.version.endsWith("SNAPSHOT"))
                    return

                def dirty_value = executeGetSuccess('git diff --quiet --ignore-submodules=dirty')
                if (dirty_value != 0) {
                    throw new Exception("Git dirty check failed. Check in your code!")
                }
            }
        }

        project.task('checkForVersionFile') {
            doLast {
                def f = new File(gversion_file_path(project, extension), extension.className + ".java")
                if (!f.exists()) {
                    throw new RuntimeException("GVersion.java does not exist. Call 'createVersionFile'")
                }
            }
        }

        // Creates a resource file containing build information
        project.task('createVersionFile') {
            doLast {
                // For some strange reasons it was using the daemon's home directory instead of the project's!
                System.setProperty("user.dir", project.projectDir.toString())

                if (extension.debug) {
                    println "gversion.debug     = true"
                    println "project.name       " + project.name
                    println "rootProject path   " + project.rootProject.file(".").path
                    println "sub-project path   " + project.file(".").path
//                    println "pwd            "+ executeGetOutput("pwd","pwd failed")
                }

                def gversion_file_path = gversion_file_path(project, extension)
                println("createVersionFile called. Path " + gversion_file_path)

                // Make sure the output directory exists. If not create it
                if (!new File(gversion_file_path).exists()) {
                    if (!new File(gversion_file_path).mkdirs()) {
                        throw new RuntimeException("Failed to make path: " + gversion_file_path)
                    } else {
                        println("Path did not exist. Created " + gversion_file_path)
                    }
                }

                def tz = TimeZone.getTimeZone(extension.timeZone)

                def version_of_git = parseGitVersion(executeGetOutput('git version', "UNKNOWN"))
//                println("  git version "+version_of_git[0]+"."+version_of_git[1]+"."+version_of_git[2])

                def dirty_value = executeGetSuccess('git diff --quiet --ignore-submodules=dirty')
                def git_revision = executeGetOutput('git rev-list --no-show-signature --count HEAD', "-1")
                def git_sha = executeGetOutput('git rev-parse HEAD', "UNKNOWN")
                def git_branch = executeGetOutput('git rev-parse --abbrev-ref HEAD', "UNKNOWN")
                def git_date
                def date_format

                if (version_of_git[0] == 1) {
                    date_format = "yyyy-MM-dd HH:mm:ss Z"
                    git_date = executeGetOutput('git show --no-show-signature -s --format=%ci HEAD', "UNKNOWN")
                } else {
                    date_format = "yyyy-MM-dd'T'HH:mm:ssXXX"
                    git_date = executeGetOutput('git show --no-show-signature -s --format=%cI HEAD', "UNKNOWN")
                }
                if (!git_date.equals("UNKNOWN")) {
                    try {
                        // Let's convert this from local to the desired time zone and adjust the date format
                        DateFormat formatter = new SimpleDateFormat(date_format)
                        Date java_data = formatter.parse(git_date)
                        formatter = new SimpleDateFormat(extension.dateFormat)
                        formatter.setTimeZone(tz)
                        git_date = formatter.format(java_data)
                    } catch (RuntimeException e) {
                        if (extension.debug) {
                            e.printStackTrace(System.err)
                        }

                        git_date = "UNKNOWN"
                    }
                }


                def indent = extension.indent
                def unix_time = System.currentTimeMillis()
                def formatter = new SimpleDateFormat(extension.dateFormat)
                formatter.setTimeZone(tz)
                String date_string = formatter.format(new Date(unix_time))

                Language language = Language.convert(extension.language)

                if (language == Language.JAVA) {
                    new File(gversion_file_path, extension.className + ".java").withWriter { writer ->
                        if (extension.classPackage.size() > 0) {
                            writer << "package $extension.classPackage;\n\n"
                        }

                        if (extension.annotate) {
                            writer << "import javax.annotation.Generated;\n\n"
                        }

                        writer << "/**\n"
                        writer << " * Automatically generated file containing build version information.\n"
                        writer << " */\n"
                        if (extension.annotate) {
                            writer << "@Generated(\"com.peterabeles.GVersion\")\n"
                        }
                        writer << "public final class $extension.className {\n"
                        writer << "${indent}public static final String MAVEN_GROUP = \"$project.group\";\n"
                        writer << "${indent}public static final String MAVEN_NAME = \"$project.name\";\n"
                        writer << "${indent}public static final String VERSION = \"$project.version\";\n"
                        writer << "${indent}public static final int GIT_REVISION = $git_revision;\n"
                        writer << "${indent}public static final String GIT_SHA = \"$git_sha\";\n"
                        writer << "${indent}public static final String GIT_DATE = \"$git_date\";\n"
                        writer << "${indent}public static final String GIT_BRANCH = \"$git_branch\";\n"
                        writer << "${indent}public static final String BUILD_DATE = \"$date_string\";\n"
                        writer << "${indent}public static final long BUILD_UNIX_TIME = " + unix_time + "L;\n"
                        writer << "${indent}public static final int DIRTY = " + dirty_value + ";\n"
                        writer << "\n"
                        writer << "${indent}private $extension.className(){}\n" // hide implicit public constructor
                        writer << "}\n"
                        writer.flush()
                    }
                } else if (language == Language.KOTLIN) {
                    new File(gversion_file_path, extension.className + ".kt").withWriter { writer ->
                        if (extension.classPackage.size() > 0) {
                            writer << "package $extension.classPackage\n"
                            writer << "\n"
                        }

                        def typeString = extension.explicitType ? ": String " : ""
                        def typeInt = extension.explicitType ? ": Int " : ""
                        def typeLong = extension.explicitType ? ": Long " : ""

                        writer << "/**\n"
                        writer << " * Automatically generated file containing build version information.\n"
                        writer << " */\n"
                        writer << "const val MAVEN_GROUP $typeString= \"$project.group\"\n"
                        writer << "const val MAVEN_NAME $typeString= \"$project.name\"\n"
                        writer << "const val VERSION $typeString= \"$project.version\"\n"
                        writer << "const val GIT_REVISION $typeInt= $git_revision\n"
                        writer << "const val GIT_SHA $typeString= \"$git_sha\"\n"
                        writer << "const val GIT_DATE $typeString= \"$git_date\"\n"
                        writer << "const val GIT_BRANCH $typeString= \"$git_branch\"\n"
                        writer << "const val BUILD_DATE $typeString= \"$date_string\"\n"
                        writer << "const val BUILD_UNIX_TIME $typeLong= " + unix_time + "L\n"
                        writer << "const val DIRTY $typeInt= $dirty_value\n"
                        writer.flush()
                    }
                } else if (language == Language.YAML) {
                    new File(gversion_file_path, extension.className + ".yaml").withWriter { writer ->
                        writer << "---\n"
                        writer << "MAVEN_GROUP: \"$project.group\"\n"
                        writer << "MAVEN_NAME: \"$project.name\"\n"
                        writer << "VERSION: \"$project.version\"\n"
                        writer << "GIT_REVISION: $git_revision\n"
                        writer << "GIT_SHA: \"$git_sha\"\n"
                        writer << "GIT_DATE: \"$git_date\"\n"
                        writer << "GIT_BRANCH: \"$git_branch\"\n"
                        writer << "BUILD_DATE: \"$date_string\"\n"
                        writer << "BUILD_UNIX_TIME: $unix_time\n"
                        writer << "DIRTY: $dirty_value\n"
                        writer.flush()
                    }
                } else if (language == Language.PROPERTIES) {
                    new File(gversion_file_path, extension.className).withWriter { writer ->
                        writer << "#Created by build system. Do not modify\n"
                        writer << "#\"$date_string\"\n"
                        writer << "version=\"$project.version\"\n"
                        writer << "revision=$git_revision\n"
                        writer << "name=\"$project.name\"\n"
                        writer << "timestamp=$unix_time\n"
                        writer << "group=\"$project.group\"\n"
                        writer << "sha=\"$git_sha\"\n"
                        writer << "git_date=\"$git_date\"\n"
                        writer << "git_branch=\"$git_branch\"\n"
                        writer << "build_date=\"$date_string\"\n"
                        writer << "dirty=$dirty_value\n"
                        writer.flush()
                    }
                } else {
                    throw new RuntimeException("BUG! Unknown language " + language)
                }
            }
        }

//        project.tasks.create('testReport',TestReport.class) {
//            doLast {
//                destinationDir = project.file("$project.buildDir/reports/allTests")
//                reportOn project.subprojects*test
//            }
//        }
    }
}
