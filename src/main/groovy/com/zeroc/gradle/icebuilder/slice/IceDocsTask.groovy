package com.zeroc.gradle.icebuilder.slice

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class IceDocsTask extends DefaultTask implements Serializable {

    private static final def Log = Logging.getLogger(IceDocsTask)

    @Input
    @Optional
    final Property<Boolean> underscore = project.objects.property(Boolean)

    @Input
    @Optional
    final Property<Boolean> debug = project.objects.property(Boolean)

    @Input
    @Optional
    final Property<Integer> index = project.objects.property(Integer)

    @Input
    @Optional
    final Property<Integer> summary = project.objects.property(Integer)

    @InputFile
    @Optional
    final RegularFileProperty header = project.objects.fileProperty()

    @InputFile
    @Optional
    final RegularFileProperty footer = project.objects.fileProperty()

    @InputFile
    @Optional
    final RegularFileProperty indexHeader = project.objects.fileProperty()

    @InputFile
    @Optional
    final RegularFileProperty indexFooter = project.objects.fileProperty()

    @OutputDirectory
    final DirectoryProperty outputDir = project.objects.directoryProperty()

    @InputFiles
    @Optional
    FileCollection includeDirs

    @InputFiles
    @Optional
    FileCollection sourceDirs

    @InputFile
    @Optional
    final RegularFileProperty src = project.objects.fileProperty()

    @TaskAction
    void apply() {
        List<String> cmd = ["slice2html"]

        cmd.addAll(["-I", project.slice.sliceDir])

        if (includeDirs) {
            // Add any additional includes
            includeDirs.each { dir -> cmd.addAll(["-I", "${dir}"]) }
        }

        cmd.addAll(["--output-dir", String.valueOf(outputDir.asFile.get())])

        if (header.isPresent()) {
            cmd.addAll(["--hdr", String.valueOf(header.asFile.get())])
        }

        if (footer.isPresent()) {
            cmd.addAll(["--ftr", String.valueOf(footer.asFile.get())])
        }

        if (indexHeader.isPresent()) {
            cmd.addAll(["--indexhdr", String.valueOf(indexHeader.asFile.get())])
        }

        if (indexFooter.isPresent()) {
            cmd.addAll(["--indexftr", String.valueOf(indexFooter.asFile.get())])
        }

        if (debug.getOrElse(false)) {
            cmd.add("-d")
        }

        // Add the source files
        if (sourceDirs) {
            sourceDirs.each { dir ->
                new File(dir.absolutePath).traverse(type: groovy.io.FileType.FILES) { it ->
                    if (it.name.endsWith('.ice')) {
                        cmd.add(String.valueOf(it))
                    }
                }
            }
        }

        executeCommand(cmd)
    }

    void executeCommand(List cmd) {
        def p = cmd.execute()
        p.waitForProcessOutput(new StringBuffer(), System.err)
        if (p.exitValue() != 0) {
            throw new GradleException("${cmd[0]} failed with exit code: ${p.exitValue()}")
        }
    }

}
