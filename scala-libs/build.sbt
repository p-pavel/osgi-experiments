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
          "scala;-split-package:=merge-first",
          "scala.annotation;-split-package:=merge-first",
          "scala.deriving",
          "scala.io",
          "scala.jdk",
          "scala.jdk.javaapi",
          // "scala.collection",
          "scala.concurrent",
          "scala.concurrent.impl",
          "scala.collection.concurrent.impl",
          "scala.concurrent.duration",
          "scala.collection.concurrent",
          "scala.collection.convert",
          "scala.collection.convert.impl",
          "scala.collection.generic",
          "scala.collection.immutable",
          "scala.collection.mutable",
          "scala.math",
          "scala.reflect;-split-package:=merge-first",
          "scala.runtime;-split-package:=merge-first",
          "scala.runtime.function",
          "scala.runtime.java8",
          "scala.sys",
          "scala.util;-split-package:=merge-first",
          "scala.util.matching",
          "scala.util.hashing",
          "scala.util.control;-split-package:=merge-first",
        ).map( _ + ";version=3.2.2"),
    )
