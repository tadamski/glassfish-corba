package com.sun.tools.corba.ee.idl.toJavaPortable;
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

/**
 * Tests IDLJ by comparing the generated source files against the expected files.
 */
public class IdljGenerationTest {

    private static int testNum = 0;
    private static File rootDir;
    private static final boolean COMPILE_GENERATED = true;  // set false to check generated files without compiling

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeClass
    public static void clearRootDir() throws IOException {
        rootDir = Files.createTempDirectory("idlj").toFile();
    }

    @Test
    public void generateClassesWithoutOptions() throws Exception {
        GenerationControl generator = new GenerationControl("src/main/java/com/sun/tools/corba/ee/idl/ir.idl");

        generator.generate();

        checkGeneratedFiles(generator, "ir");
    }

    // Confirms that the generated files match those in the specified directory of master files
    private void checkGeneratedFiles(GenerationControl generator, String mastersSubDir) throws IOException {
        File masterDir = new File(getModuleRoot(), "src/test/masters/" + mastersSubDir);

        String[] generatedFilePaths = getFilePaths(generator.getDestDir());
        String[] expectedFilePaths = getFilePaths(masterDir);

        assertThat("In " + generator.getDestDir(), generatedFilePaths, arrayContaining(expectedFilePaths));
        compareGeneratedFiles(masterDir, generator.getDestDir(), expectedFilePaths);
    }

    private File getModuleRoot() {
        String classPathString = TestUtils.getClassPathString();
        return new File(classPathString.substring(0, classPathString.lastIndexOf("/target/")));
    }

    // Returns a sorted array of paths to files with the specified suffix under the specified directory, relative to that directory
    private String[] getFilePaths(File rootDir) {
        ArrayList<String> files = new ArrayList<>();
        appendFiles(files, rootDir, rootDir.getAbsolutePath().length() + 1, ".java");
        Collections.sort(files);
        return files.toArray(new String[files.size()]);
    }

    @SuppressWarnings("ConstantConditions")
    private void appendFiles(ArrayList<String> files, File currentDir, int rootDirLength, String suffix) {
        for (File file : currentDir.listFiles())
            if (file.isDirectory())
                appendFiles(files, file, rootDirLength, suffix);
            else if (file.getName().endsWith(suffix))
                files.add(getRelativePath(file, rootDirLength));
    }

    private String getRelativePath(File file, int rootDirLength) {
        return file.getAbsolutePath().substring(rootDirLength);
    }

    private void compareGeneratedFiles(File expectedDir, File actualDir, String... generatedFileNames) throws IOException {
        for (String filePath : generatedFileNames)
            compareFiles(filePath, expectedDir, actualDir);
    }

    private void compareFiles(String filePath, File masterDirectory, File generationDirectory) throws IOException {
        File expectedFile = new File(masterDirectory, filePath);
        File actualFile = new File(generationDirectory, filePath);

        compareFiles(expectedFile, actualFile);
    }

    private void compareFiles(File expectedFile, File actualFile) throws IOException {
        LineNumberReader expected = new LineNumberReader(new FileReader(expectedFile));
        LineNumberReader actual = new LineNumberReader(new FileReader(actualFile));

        String expectedLine = "";
        String actualLine = "";
        while (expectedLine != null && actualLine != null && expectedLine.equals(actualLine)) {
            expectedLine = expected.readLine();
            actualLine = actual.readLine();
        }

        if (expectedLine == null && actualLine == null) return;

        if (expectedLine == null)
            fail("Unexpected line in generated file at " + actual.getLineNumber() + ": " + actualLine);
        else if (actualLine == null)
            fail("Actual file ends unexpectedly at line " + expected.getLineNumber());
        else if (!expectedLine.trim().startsWith("* IGNORE"))
            fail("Generated file mismatch in " + actualFile + " at line " + actual.getLineNumber() +
                    "\nshould be <" + expectedLine + "> " +
                    "\nbut found <" + actualLine + ">");

    }


    private class GenerationControl {
        private ArrayList<String> argList = new ArrayList<>();
        private String[] idlFiles;
        private File destDir;
        private String warning;

        @SuppressWarnings("ResultOfMethodCallIgnored")
        GenerationControl(String... idlFiles) {
            this.idlFiles = idlFiles;

            destDir = new File(rootDir + "/" + (++testNum));
            destDir.mkdirs();
            addArgs("-td", destDir.getAbsolutePath());
        }

        private void addArgs(String... args) {
            argList.addAll(Arrays.asList(args));
        }

        File getDestDir() {
            return destDir;
        }

        String getWarning() {
            return warning;
        }

        private void generate() throws IOException {
            if (argList.contains("-iiop") && !COMPILE_GENERATED) addArgs("-Xnocompile");
            for (String name : idlFiles)
                addArgs(new File(getModuleRoot(), name).getAbsolutePath());
            Compile.compiler = new Compile();
            String[] argv = argList.toArray(new String[argList.size()]);
            Compile.compiler.start(argv);
        }

    }

    private static String[] toNameList(Class<?>[] classes) {
        String[] nameList = new String[classes.length];
        for (int i = 0; i < classes.length; i++)
            nameList[i] = classes[i].getName();
        return nameList;
    }
}
