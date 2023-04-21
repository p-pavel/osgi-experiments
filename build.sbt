ThisBuild / scalaVersion := "3.2.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.perikov"
ThisBuild / scalacOptions ++= Seq("-deprecation")
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val scalaLibs =
  project.in(file("scala-libs"))

lazy val root =
  project.in(file("."))
  .aggregate(scalaLibs)

