package org.jetbrains.uast.test.java

import org.junit.Test

class SimpleJavaRenderLogTest : AbstractJavaRenderLogTest() {
    @Test fun testDataClass() = doTest("DataClass/DataClass.java")

    @Test fun testEnumSwitch() = doTest("Simple/EnumSwitch.java")

    @Test fun testJava() = doTest("Simple/Simple.java")

    @Test fun testReturnX() = doTest("Simple/ReturnX.java")
}