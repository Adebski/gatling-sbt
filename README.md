# gatling-sbt   [![Build Status](https://travis-ci.org/gatling/gatling-sbt.svg?branch=master)](https://travis-ci.org/gatling/gatling-sbt)


This SBT plugin integrates Gatling with SBT, allowing to use Gatling as a testing framework.

## Setup 

Snapshots are available on Sonatype.

In `project/plugins.sbt`, add: 

    addSbtPlugin("io.gatling" % "gatling-sbt" % "2.1.0")
    
You'll also need those two dependencies:

```scala
"io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.4" % "test"
"io.gatling"            % "gatling-test-framework"    % "2.1.4" % "test"
```

And then, in your `.scala` build :

```scala

import io.gatling.sbt.GatlingPlugin

lazy val project = Project(...)
                     .enablePlugins(GatlingPlugin)
				     .settings(libraryDependencies ++= /* Gatling dependencies */)

```

or in your `.sbt` file, for SBT up to 0.13.5 :

```scala

val test = project.in(file("."))
  .enablePlugins(GatlingPlugin)
  .settings(libraryDependencies ++= /* Gatling dependencies */)

```

or for 0.13.6 and later : 

```scala

enablePlugins(GatlingPlugin)

libraryDependencies ++= /* Gatling dependencies */

```

## Usage 

As with any SBT testing framework, you'll be able to run Gatling simulations using SBT standard `test`, `testOnly`, `testQuick`, etc... tasks.

## 'Test' vs 'Integration Tests' configurations

This plugin offers two different custom SBT configurations, named `Gatling` and `GatlingIt`.
They are tied to different sources directories (see next section for more details) and therefore allow to separate your simulations according to your needs, should you desire it.

Ideally :

* Your simulations with low injection profiles, which may serve as functional tests, should live in 'src/test' (the default source directory for the `Gatling` configuration), and run along your unit tests, since they would complete quickly
* Longer, more complex simulations with high injection profiles, should live in 'src/it' (the default source directory for the `GatlingIt` configuration) and be run on a as-needed basis.

Also, since they're tied to separate SBT configurations, your SBT settings can then be customized per configuration.
You can expect a relatively short simulation to run easily with the default JVM settings, but simulations with much higher load can very well require an increase of the max heap memory allowed for example).

**Note :** When using the `GatlingIt` configuration, you must prefix the various tasks and settings you may want to use by `it`, e.g. `test` becomes `it:test`, etc...

## Default settings 

For the `Gatling` configuration :

* By default, Gatling simulations must be in `src/test/scala`, configurable using the `scalaSource in Gatling` setting.
* By default, Gatling reports are written to `target/gatling`, configurable using the `target in Gatling` setting.

For the `GatlingIt` configuration :

* By default, Gatling simulations must be in `src/it/scala`, configurable using the `scalaSource in GatlingIt` setting.
* By default, Gatling reports are written to `target/gatling-it`, configurable using the `target in GatlingIt` setting.

## Additional tasks

Gatling's SBT plugin also offers three additional tasks:

* `startRecorder`, which start the Recorder, configured to save recorded simulations to the location specified by `scalaSource in Gatling` (by default, `src/test/scala`).
* `lastReport`, which opens the last generated report in your web browser.
* `copyConfigFiles`, which copies Gatling's configuration files (gatling.conf & recorder.conf) from the bundle into your project resources if they're missing. Please not that you need to add Gatling's bundle to your dependencies for this task to work : `"io.gatling" % "gatling-bundle" % "2.1.4" % "test" artifacts (Artifact("gatling-bundle", "zip", "zip", "bundle"))`. After the configuration files have been copied, this dependency can be safely dropped.
* `copyLogbackXml`, which copies Gatling's default logback.xml. Works the same as `copyConfigFiles`, you'll need to add the bundle to your dependencies.
