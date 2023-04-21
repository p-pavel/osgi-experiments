import sbt.*
import Defaults.*
lazy val scalaLibs =
  project
    .in(file("."))
    .enablePlugins(SbtOsgi)
    .settings(
      name := "scala3-library",
      scalaVersion := "3.2.2",
      version := "3.2.2-SNAPSHOT",
      osgiSettings,
      libraryDependencies ++= Seq(
        "org.osgi" % "org.osgi.core" % "6.0.0",
        "org.osgi" % "org.osgi.annotation.bundle" % "2.0.0"
      ),
      // OsgiKeys.embeddedJars := (Compile / dependencyClasspathAsJars).value
      //   .map(_.data)
      //   .filter(_.getName.contains("scala3")),
      OsgiKeys.explodedJars := Seq(),
      OsgiKeys.requireBundle := Seq(
        // Can't get from depenencies -- settings cannot depend on tasks
        "org.scala-lang.scala-library;bundle-version=\"[2.3.10,3)\"",
      ),
      OsgiKeys.privatePackage := Seq("com.perikov.spikes.packages"),
      OsgiKeys.bundleActivator := Some(
        "com.perikov.spikes.packages.testLatency"
      ),
      OsgiKeys.exportPackage := 
        Seq(
          "scala.*;-split-package:=merge-first",
          // "scala.quoted.*;-split-package:=merge-first",
          //  "scala.*",
          // "scala.runtime.*",
          // "scala.annotation.*",
          // "scala.collection.*",
          // "scala.concurrent.*",
          // "scala.util.*",
          // "scala.jdk.*",
          // "scala.sys.*",
          // "scala.quoted",
          // "scala.quoted.*",
          // "scala.deriving",
        ).map( _ + ";version=3.2.2"),
      // OsgiKeys.importPackage := Seq("scala.quoted;version=3.2.2")
    )
