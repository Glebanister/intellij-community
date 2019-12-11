/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.resolve;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/resolve/referenceWithLib")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ReferenceResolveWithLibTestGenerated extends AbstractReferenceResolveWithLibTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInReferenceWithLib() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("idea/testData/resolve/referenceWithLib"), Pattern.compile("^(.+)\\.kt$"), null, false);
    }

    @TestMetadata("delegatedPropertyWithTypeParameters.kt")
    public void testDelegatedPropertyWithTypeParameters() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/delegatedPropertyWithTypeParameters.kt");
    }

    @TestMetadata("fakeOverride.kt")
    public void testFakeOverride() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/fakeOverride.kt");
    }

    @TestMetadata("fakeOverride2.kt")
    public void testFakeOverride2() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/fakeOverride2.kt");
    }

    @TestMetadata("infinityAndNanInJavaAnnotation.kt")
    public void testInfinityAndNanInJavaAnnotation() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/infinityAndNanInJavaAnnotation.kt");
    }

    @TestMetadata("innerClassFromLib.kt")
    public void testInnerClassFromLib() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/innerClassFromLib.kt");
    }

    @TestMetadata("iteratorWithTypeParameter.kt")
    public void testIteratorWithTypeParameter() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/iteratorWithTypeParameter.kt");
    }

    @TestMetadata("multiDeclarationWithTypeParameters.kt")
    public void testMultiDeclarationWithTypeParameters() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/multiDeclarationWithTypeParameters.kt");
    }

    @TestMetadata("nestedClassFromLib.kt")
    public void testNestedClassFromLib() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/nestedClassFromLib.kt");
    }

    @TestMetadata("overloadFun.kt")
    public void testOverloadFun() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/overloadFun.kt");
    }

    @TestMetadata("overridingFunctionWithSamAdapter.kt")
    public void testOverridingFunctionWithSamAdapter() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/overridingFunctionWithSamAdapter.kt");
    }

    @TestMetadata("packageOfLibDeclaration.kt")
    public void testPackageOfLibDeclaration() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/packageOfLibDeclaration.kt");
    }

    @TestMetadata("referenceToRootJavaClassFromLib.kt")
    public void testReferenceToRootJavaClassFromLib() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/referenceToRootJavaClassFromLib.kt");
    }

    @TestMetadata("sameNameInLib.kt")
    public void testSameNameInLib() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/sameNameInLib.kt");
    }

    @TestMetadata("setWithTypeParameters.kt")
    public void testSetWithTypeParameters() throws Exception {
        runTest("idea/testData/resolve/referenceWithLib/setWithTypeParameters.kt");
    }
}
