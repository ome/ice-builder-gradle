package com.zeroc.gradle.icebuilder.slice

import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class PythonTask extends DefaultTask {

    private static final def Log = Logging.getLogger(PythonTask)

    @InputFiles
    FileCollection inputFiles

    @OutputDirectory
    File outputDir

    @Input
    @Optional
    String prefix

    @Input
    @Optional
    FileCollection includeDirs

    // Change this to a configuration
    SliceExtension sliceExt = project.slice

    @TaskAction
    void action(IncrementalTaskInputs inputs) {
        if (!inputs.incremental) {
            inputFiles.each { file ->
                deleteOutputFile(file)
            }
        }

        List filesForProcessing = []
        inputs.outOfDate { change ->
            if (change.file.directory) return

            // Log which file will be included in slice2py
            Log.info("File for processing: $change.file")

            // Add input file for processing
            filesForProcessing.add("${change.file}")
        }

        if (!filesForProcessing.isEmpty()) {
            List cmd = [sliceExt.slice2py, "-I${sliceExt.sliceDir}"]

            if (includeDirs) {
                // Add any additional includes
                includeDirs.each { dir -> cmd.add("-I${dir}") }
            }

            // Add files for processing
            cmd.addAll(filesForProcessing)

            if (prefix) {
                // Set a prefix
                cmd.add("--prefix=${prefix}")
            }

            // Set the output directory
            cmd.add("--output-dir=${outputDir}")
            executeCommand(cmd)
        }

        inputs.removed { change ->
            if (change.file.directory) return

            deleteOutputFile(change.file)
        }
    }

    void inputFiles(Object... files) {
        setInputFiles(files)
    }

    void inputFiles(FileCollection collection) {
        setInputFiles(collection)
    }

    void setInputFiles(FileCollection collection) {
        if (inputFiles) {
            inputFiles = inputFiles + collection
        } else {
            inputFiles = collection
        }
    }

    void setInputFiles(Object... files) {
        setInputFiles(project.files(files))
    }

    void includeDirs(FileCollection collection) {
        setIncludeDirs(collection)
    }

    void includeDirs(Object... paths) {
        setIncludeDirs(paths)
    }

    void setIncludeDirs(FileCollection collection) {
        if (includeDirs) {
            includeDirs = includeDirs + collection
        } else {
            includeDirs = collection
        }
    }

    void setIncludeDirs(Object... dirs) {
        setIncludeDirs(project.files(dirs))
    }

    void outputDir(String dir) {
        setOutputDir(dir)
    }

    void outputDir(File dir) {
        setOutputDir(dir)
    }

    void setOutputDir(String dir) {
        setOutputDir(project.file(dir))
    }

    void setOutputDir(File dir) {
        outputDir = dir
    }

    void prefix(String text) {
        setPrefix(text)
    }

    void setPrefix(String text) {
        prefix = text
    }

    String getOutputFileName(File file) {
        def extension = FilenameUtils.getExtension(file.name)
        def filename = FilenameUtils.getBaseName(file.name)
        if (prefix) {
            filename = prefix + filename + "_ice"
        }
        return "${filename}.{$extension}"
    }

    void deleteOutputFile(File file) {
        // Convert the input filename to the output filename and
        // delete that file
        def targetFile = project.file("$outputDir/${getOutputFileName(file)}")
        if (targetFile.exists()) {
            targetFile.delete()
        }
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
