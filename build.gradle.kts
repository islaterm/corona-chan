/**
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @since 1.0.1-a.1
 * @version 1.0
 */
@file:Suppress("SpellCheckingInspection")

import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  kotlin("jvm") version "1.3.72"
}

group = "com.github.islaterm"
version = "1.0.1-ALPHA.1"

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(group = "com.github.kotlin-telegram-bot.kotlin-telegram-bot", name = "dispatcher", version = "4.4.0")
  implementation(group = "org.yaml", name = "snakeyaml", version = "1.26")
  implementation(group = "org.jsoup", name = "jsoup", version = "1.13.1")
  implementation(group = "tech.tablesaw", name = "tablesaw-core", version = "0.38.1")
  implementation(group = "tech.tablesaw", name = "tablesaw-jsplot", version = "0.38.1")
  testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.7.0-M1")
  testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.7.0-M1")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  register<Jar>("uberJar") {
    archiveAppendix.set("uber")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
      configurations.runtimeClasspath.get().filter {
        it.name.endsWith("jar")
      }.map {
        zipTree(it)
      }
    })
  }
  named<Test>("test") {
    testLogging {
      events(FAILED, PASSED, SKIPPED, STANDARD_OUT, STANDARD_ERROR)
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
    }
    useJUnitPlatform()
    addTestListener(object : TestListener {
      /**
       * Called before an atomic test is started.
       * @param testDescriptor The test which is about to be executed.
       */
      override fun beforeTest(testDescriptor: TestDescriptor?) {}

      /**
       * Called after a test suite is finished.
       * @param suite The suite whose tests have finished being executed.
       * @param result The aggregate result for the suite.
       */
      override fun afterSuite(suite: TestDescriptor?, result: TestResult?) {}

      /**
       * Called before a test suite is started.
       * @param suite The suite whose tests are about to be executed.
       */
      override fun beforeSuite(suite: TestDescriptor?) {}

      /**
       * Called after an atomic test is finished.
       * @param testDescriptor The test which has finished executing.
       * @param result The test result.
       */
      override fun afterTest(testDescriptor: TestDescriptor?, result: TestResult?) {
        if (testDescriptor?.parent == null) { // root suite
          logger.quiet("----")
          if (result != null) {
            logger.quiet("Test result: ${result.resultType}")
            logger.quiet(
              "Test summary: ${result.testCount} tests, " +
                  "${result.successfulTestCount} succeeded, " +
                  "${result.failedTestCount} failed, " +
                  "${result.skippedTestCount} skipped"
            )
          }
        }
      }
    })
  }
}