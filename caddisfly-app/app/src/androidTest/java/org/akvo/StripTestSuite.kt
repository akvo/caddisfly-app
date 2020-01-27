package org.akvo

import org.akvo.caddisfly.instruction.StriptestInstructionsTest
import org.akvo.caddisfly.internal.StriptestTest
import org.akvo.caddisfly.test.StriptestSurveyTest
import org.junit.runners.Suite

// Uncomment @Runwith below to run this suite. This is commented because gradlew runs the test twice
//@RunWith(Suite::class)
@Suite.SuiteClasses(StriptestInstructionsTest::class, StriptestTest::class, StriptestSurveyTest::class)
class StripTestSuite