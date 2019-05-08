package com.zeroc.gradle.icebuilder.slice

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class DocsTask extends DefaultTask {

    @Input
    @Optional
    boolean underscore

    @Input
    @Optional
    boolean debug

    @Input
    @Optional
    int index

    @Input
    @Optional
    int summary

    @OutputDirectory
    File outputDir

    @InputFile
    @Optional
    File header

    @InputFile
    @Optional
    File footer

    @InputFile
    @Optional
    File indexHeader

    @InputFile
    @Optional
    File indexFooter

    @InputFiles
    @Optional
    FileCollection includeDirs

    @InputFiles
    FileCollection sourceFiles

    // Change this to a configuration
    SliceExtension sliceExt = project.slice

    @TaskAction
    void apply() {
        List<String> cmd = ["slice2html", "-I${sliceExt.sliceDir}"]

        cmd.addAll(["--output-dir", String.valueOf(outputDir)])

        if (includeDirs) {
            // Add any additional includes
            includeDirs.each { dir -> cmd.add("-I${dir}") }
        }

        if (header) {
            cmd.addAll(["--hdr", String.valueOf(header)])
        }

        if (footer) {
            cmd.addAll(["--ftr", String.valueOf(footer)])
        }

        if (indexHeader) {
            cmd.addAll(["--indexhdr", String.valueOf(indexHeader)])
        }

        if (indexFooter) {
            cmd.addAll(["--indexhdr", String.valueOf(indexFooter)])
        }

        // Add the source files
        sourceFiles.files.each {
            cmd.add(String.valueOf(it))
        }

        if (debug) {
            cmd.add("-d")
        }

        executeCommand(cmd)
    }

    void executeCommand(List cmd) {
        def sout = new StringBuffer()
        def p = cmd.execute()
        p.waitForProcessOutput(sout, System.err)
        if (p.exitValue() != 0) {
            throw new GradleException("${cmd[0]} failed with exit code: ${p.exitValue()}")
        }
    }

}
