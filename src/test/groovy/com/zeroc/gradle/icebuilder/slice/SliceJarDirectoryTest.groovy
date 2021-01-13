// **********************************************************************
//
// Copyright (c) 2014-present ZeroC, Inc. All rights reserved.
//
// **********************************************************************

package com.zeroc.gradle.icebuilder.slice

import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.junit.Assume.assumeTrue

import java.nio.file.Files
import java.nio.file.Paths

class SliceJarDirectoryTest extends TestCase {

    def iceHome = null

    @Before
    public void createIceHome() {
        def isWindows = System.getProperty('os.name').toLowerCase().contains('windows')

        iceHome = File.createTempDir()
        createIceHomePath(["bin"])

        def copyFileBytes = { src, dst ->
           def srcFile = new File(src)
           def dstFile = new File(dst)
           dstFile << srcFile.bytes
           return dstFile
        }
        def dir = "lib"
        def os = System.properties['os.name']
        if (os.contains("Linux")) {
            dir = "lib64"
        }
        createIceHomePath([dir])

        def dst =  [iceHome.toString(), "bin", new File(project.slice.slice2java).getName()].join(File.separator)
        //copyFileBytes(project.slice.slice2java, dst).setExecutable(true)
        Files.copy(Paths.get(project.slice.slice2java), Paths.get(dst))
        def dh = new File([project.slice.iceHome, dir].join(File.separator))
        dh.eachFile {
            def target =  [iceHome.toString(), dir, it.getName()].join(File.separator)
            def source =  [project.slice.iceHome, dir, it.getName()].join(File.separator)
            Files.copy(Paths.get(source), Paths.get(target))
        }
        // For Ice 3.6 we also copy slice2java dependencies.
        // This is unnecessary in Ice 3.7 as slice2java is statically linked
        if(isWindows && project.slice.compareIceVersion("3.7") == -1) {
           ['slice36.dll', 'iceutil36.dll'].each {
                def src = [new File(project.slice.slice2java).getParent(), it].join(File.separator)
                copyFileBytes(src, [iceHome.toString(), "bin", it].join(File.separator)).setExecutable(true)
           }
        }
    }

    @After
    public void cleanupIceHome() {
        // if(iceHome) {
        //     iceHome.deleteDir()
        //     iceHome.deleteOnExit()
        // }
    }

    def createIceHomePath(path) {
        def newPath = new File([iceHome.toString(), path.join(File.separator)].join(File.separator))
        newPath.mkdirs()
        newPath.toString()
        return newPath
    }

    @Test
    public void testSliceCommon() {
        def tmpSliceDir = createIceHomePath(["share", "slice"])
        project.slice.iceHome = iceHome.toString()
        assertNotNull(project.slice.sliceDir)
        assertEquals(project.slice.sliceDir.toString(), tmpSliceDir.toString())
    }

    @Test
    public void testIce37SliceDir() {
        assumeTrue(project.slice.compareIceVersion("3.7") != -1)
        def tmpSliceDir = createIceHomePath(["share", "ice", "slice"])
        project.slice.iceHome = iceHome.toString()
        assertNotNull(project.slice.sliceDir)
        assertEquals(project.slice.sliceDir.toString(), tmpSliceDir.toString())
    }

    @Test
    public void testIce36SliceDir() {
        def tmpSliceDir = createIceHomePath(["share", "Ice-${project.slice.iceVersion}", "slice"])
        project.slice.iceHome = iceHome.toString()
        assertNotNull(project.slice.sliceDir)
        assertEquals(project.slice.sliceDir.toString(), tmpSliceDir.toString())
        //assertTrue(project.slice.sliceDir == tmpSliceDir)
    }

    @Test
    public void testOptSourceSliceDir() {
        def tmpSliceDir = createIceHomePath(["slice"])
        project.slice.iceHome = iceHome.toString()
        assertNotNull(project.slice.sliceDir)
        assertEquals(project.slice.sliceDir.toString(), tmpSliceDir.toString())
    }

}
