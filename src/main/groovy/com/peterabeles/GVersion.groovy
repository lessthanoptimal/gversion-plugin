package com.peterabeles

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownProjectException

import java.text.DateFormat
import java.text.SimpleDateFormat

class GVersionExtension {
//    List javadoc_links = []
//    String javadoc_bottom_path = "misc/bottom.txt"
    String srcDir
    String classPackage = ""
    String className = "GVersion"
    String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    String timeZone = "UTC"
}

class GVersion implements Plugin<Project> {

    static String gversion_file_path( Project project , GVersionExtension extension ) {
        if(extension.srcDir == null )
            throw new RuntimeException("Must set gversion_file_path")

        String project_path = project.rootProject.file(".").path
//        System.out.println(project_path)

        File gversion_file_path = new File(extension.srcDir,
                extension.classPackage.replace(".",File.separator))
        return new File(project_path,gversion_file_path.path).getPath()
    }

    static String executeGetOutput( String command , String DEFAULT ) {
        def proc = command.execute()
        try {
            proc.consumeProcessErrorStream(new StringBuffer())
            proc.waitFor()
            if( proc.exitValue() != 0 )
                return DEFAULT;
            return proc.text.trim()
        } catch (IOException ignore) {
            return DEFAULT;
        } finally {
            proc.destroy()
        }
    }

    void apply(Project project) {
        // Add the 'greeting' extension object
        def extension = project.extensions.create('gversion', GVersionExtension)

//        project.tasks.create('alljavadoc',JavaDoc){
////        task alljavadoc(type: Javadoc) {
//            if( extension.length == 0 ) {
//                throw new RuntimeException("javadoc_links has not been specified")
//            }
//            if( extension.size() == 0 ) {
//                throw new RuntimeException("project_name needs to be specified")
//            }
//
//            // only include source code in src directory to avoid including 3rd party code which some projects do as a hack
//            source = javadocProjects.collect { project(it).fileTree('src').include('**/*.java') }
////    source = javadocProjects.collect { project(it).sourceSets.main.allJava }
//            classpath = files(javadocProjects.collect { project(it).sourceSets.main.compileClasspath })
//
//            destinationDir = file("docs/api")
//
//            // Hack for Java 8u121 and beyond. Comment out if running an earlier version of Java
//            options.addBooleanOption("-allow-script-in-comments", true)
//
//            // Add a list of uses of a class to javadoc
//            options.use = true
//
//            configure(options) {
//                failOnError = false
//                docTitle = extension.project_name+" JavaDoc ($project.version)"
//                links = extension.javadoc_links
//            }
//
//            // Work around a Gradle design flaw. It won't copy over files in doc-files
//            doLast {
//                copy {
//                    from javadocProjects.collect { project(it).fileTree('src').include('**/doc-files/*') }
//                    into destinationDir
//                }
//            }
//
//        }
//
//        task alljavadocWeb() {
//            doFirst {
//                alljavadoc.options.bottom = file(extension.javadoc_bottom_path).text
//                alljavadoc.destinationDir = file("docs/api-web")
//            }
//        }
//        alljavadocWeb.finalizedBy(alljavadoc)


        project.ext.checkProjectExistsAddToList = { whichProject , list ->
            try {
                project.project(whichProject)
                list.add(whichProject)
            } catch( UnknownProjectException ignore ) {}
        }

        // Force the release build to fail if it depends on a SNAPSHOT
        project.tasks.create('checkDependsOnSNAPSHOT'){
            doLast {
                if (project.version.endsWith("SNAPSHOT"))
                    return

                project.configurations.compile.each {
                    if (it.toString().contains("SNAPSHOT"))
                        throw new Exception("Release build contains snapshot dependencies: " + it)
                }
            }
        }

        project.task('checkForVersionFile') {
            doLast {
                def f = new File(gversion_file_path(project,extension),extension.className+".java")
                if( !f.exists() ) {
                    throw new RuntimeException("GVersion.java does not exist. Call 'createVersionFile'")
                }
            }
        }

        // Creates a resource file containing build information
        project.task('createVersionFile'){
            doLast {
                def gversion_file_path = gversion_file_path(project,extension)
                println("createVersionFile called. Path "+gversion_file_path)

                def tz = TimeZone.getTimeZone( extension.timeZone )

                def git_revision = executeGetOutput('git rev-list --count HEAD',"-1")
                def git_sha = executeGetOutput('git rev-parse HEAD',"UNKNOWN")
                def git_date = executeGetOutput('git show -s --format=%cI HEAD',"UNKNOWN")

                if( !git_date.equals("UNKNOWN")) {
                    // Let's convert this from local to the desired time zone and adjust the date format
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                    Date java_data = formatter.parse(git_date)
                    formatter = new SimpleDateFormat(extension.dateFormat)
                    formatter.setTimeZone(tz)
                    git_date = formatter.format(java_data)
                }

                def unix_time = System.currentTimeMillis()
                def formatter = new SimpleDateFormat(extension.dateFormat)
                formatter.setTimeZone(tz)
                String date_string = formatter.format(new Date(unix_time))

                def f = new File(gversion_file_path,extension.className+".java")
                def writer = new FileWriter(f);
                if( extension.classPackage.size() > 0 ) {
                    writer << "package $extension.classPackage;\n"
                    writer << "\n\n"
                }
                writer << "/**\n"
                writer << " * Automatically generated file containing build version information.\n"
                writer << " */\n"
                writer << "public class "+extension.className+" {\n"
                writer << "\tpublic static final String MAVEN_GROUP = \"$project.group\";\n"
                writer << "\tpublic static final String MAVEN_NAME = \"$project.name\";\n"
                writer << "\tpublic static final String VERSION = \"$project.version\";\n"
                writer << "\tpublic static final int GIT_REVISION = $git_revision;\n"
                writer << "\tpublic static final String GIT_SHA = \"$git_sha\";\n"
                writer << "\tpublic static final String GIT_DATE = \"$git_date\";\n"
                writer << "\tpublic static final String BUILD_DATE = \"$date_string\";\n"
                writer << "\tpublic static final long BUILD_UNIX_TIME = "+unix_time+"L;\n"
                writer << "}"
                writer.flush()
                writer.close()
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