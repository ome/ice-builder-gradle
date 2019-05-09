package com.zeroc.gradle.icebuilder.slice

import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import java.nio.file.Path

class JavaTask extends DefaultTask {

    private static final def Log = Logging.getLogger(JavaTask)

    @Input
    @Optional
    final Property<Boolean> tie = project.objects.property(Boolean)

    @Input
    @Optional
    final Property<Boolean> impl = project.objects.property(Boolean)

    @InputFiles
    @Optional
    final ConfigurableFileCollection includeDirs = project.files()

    @InputFiles
    final ConfigurableFileCollection sourceFiles = project.files()

    @OutputDirectory
    final DirectoryProperty outputDir = project.objects.directoryProperty()

    // Change this to a configuration
    SliceExtension sliceExt = project.slice

    @TaskAction
    void action(IncrementalTaskInputs inputs) {
        if (!inputs.incremental) {
            project.delete(outputDir.asFile.get().listFiles())
        }

        List<String> filesForProcessing = []
        inputs.outOfDate { change ->
            if (change.file.directory) return

            // Log which file will be included in slice2py
            Log.info("File for processing: $change.file")

            // Add input file for processing
            filesForProcessing.add(String.valueOf(change.file))
        }

        if (!filesForProcessing.isEmpty()) {
            List<String> cmd = ["slice2java", "-I" + sliceExt.sliceDir]

            if (!includeDirs.isEmpty()) {
                // Add any additional includes
                includeDirs.files.each { File dir -> cmd.add("-I" + dir) }
            }

            // Add files for processing
            cmd.addAll(filesForProcessing)

            if (tie.getOrElse(false)) {
                cmd.add("--tie")
            }

            if (impl.getOrElse(false)) {
                cmd.add("--impl")
            }

            // Set the output directory
            cmd.add("--output-dir=" + outputDir.asFile.get())
            executeCommand(cmd)
        }

        inputs.removed { change ->
            if (change.file.directory) return

            deleteOutputFile(change.file)
        }
    }

    void deleteOutputFile(File file) {
        Path resolvedPath = outputDir.get().asFile.toPath().resolve(file.toPath())
        File resolvedFile = resolvedPath.toFile()

        // Convert the input filename to the output filename and
        // delete that file
        File targetFile = new File(resolvedFile.path, FilenameUtils.getBaseName(resolvedFile.name) + ".java")
        if (targetFile.exists()) {
            targetFile.delete()
        }
    }

    private void executeCommand(List cmd) {
        StringBuffer sout = new StringBuffer()
        Process p = cmd.execute()
        p.waitForProcessOutput(sout, System.err)
        if (p.exitValue() != 0) {
            throw new GradleException("${cmd[0]} failed with exit code: ${p.exitValue()}")
        }
    }

}