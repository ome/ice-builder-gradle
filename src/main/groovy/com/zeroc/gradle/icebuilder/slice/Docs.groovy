package com.zeroc.gradle.icebuilder.slice

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class Docs implements Named{

    private final String name

    private final Project project

    File outputDir

    FileCollection includeDirs

    FileCollection sourceFiles

    Docs(String name, Project project) {
        this.name = name
        this.project = project
    }

    @Override
    String getName() {
        return name
    }
}
