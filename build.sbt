lazy val osgiLibraryBundle = taskKey[File](
  "Create a library OSGi bundle (it does not have a classpath, only includes jars)"
)
enablePlugins(SbtOsgi)

lazy val osgiLibSettings =
  osgiSettings ++
    Seq(
      Compile / osgiLibraryBundle := {
        import OsgiKeys.*
        import com.typesafe.sbt.osgi.Osgi
        Osgi.bundleTask(
          manifestHeaders.value,
          additionalHeaders.value,
          Seq.empty,
          (Compile / packageBin / artifactPath).value,
          (Compile / resourceDirectories).value,
          embeddedJars.value,
          explodedJars.value,
          failOnUndecidedPackage.value,
          (Compile / sourceDirectories).value,
          (Compile / packageBin / packageOptions).value,
          streams.value
        )
      },
      OsgiKeys.embeddedJars       := (Compile / dependencyClasspathAsJars).value.map(
        _.data
      )
    )

inThisBuild {
  Seq(
    scalaVersion     := "3.2.2",
    version          := "0.1.0-SNAPSHOT",
    organization     := "com.perikov",
    autoScalaLibrary := false,
    scalacOptions ++= Seq("-deprecation")
  )
}

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val scala3Lib =
  (project in file("libs/scala3Lib"))
    .enablePlugins(SbtOsgi)
    .settings(
      osgiLibSettings,
      libraryDependencies    := Seq(
        "org.scala-lang" %% "scala3-library" % "3.2.2"
      ),
      OsgiKeys.exportPackage := Seq(
        "scala;scala.**;-split-package:=merge-first; version=3.2.2"
      ),
      OsgiKeys.importPackage := Seq("*") // TODO: check self imports
    )

lazy val cats =
  project
    .in(file("libs/cats"))
    .dependsOn(scala3Lib)
    .enablePlugins(SbtOsgi)
    .settings(
      osgiLibSettings,
      autoScalaLibrary      := false,
      OsgiKeys.embeddedJars := (Compile / externalDependencyClasspath).value
        .map(
          _.data
        )
        .filterNot(_.getName.contains("library")), // TODO: hack
      OsgiKeys.exportPackage := Seq(
        "cats;cats.**;version=2.9.0"
      ),
      OsgiKeys.importPackage := Seq(
        "scala",
        "scala.annotation",
        "scala.collection",
        "scala.collection.immutable",
        "scala.collection.mutable",
        "scala.concurrent",
        "scala.concurrent.duration",
        "scala.deriving",
        "scala.math",
        "scala.reflect",
        "scala.runtime",
        "scala.runtime.function",
        "scala.runtime.java8",
        "scala.sys",
        "scala.util",
        "scala.util.control",
        "scala.util.hashing"
      ).map(_ + ";version=\"[3.2,4)\"") ++ Seq("*"),
      libraryDependencies    := Seq(
        "org.typelevel" %% "cats-core" % "2.9.0"
      )
    )

lazy val catsEffect =
  project
    .in(file("libs/cats-effect"))
    .enablePlugins(SbtOsgi)
    .dependsOn(cats, scala3Lib)
    .settings(
      osgiLibSettings,
      OsgiKeys.embeddedJars := (Compile / externalDependencyClasspath).value
        .map(
          _.data
        )
        .filter(_.getName.contains("cats-effect")), // TODO: hack
      OsgiKeys.exportPackage := Seq(
        "cats.effect;cats.effect.**;version=3.4.9"
      ),
      OsgiKeys.importPackage := Seq(
        "scala;scala.**;version=\"[3.2,4)\"",
        "cats;cats.kernel;cats.syntax;cats.data;cats.arrow;version=\"[2.9,3)\"",
        ),
      libraryDependencies := Seq(
        "org.typelevel" %% "cats-effect" % "3.4.9"
      )
    )

lazy val root =
  project.in(file(".")).aggregate(cats, scala3Lib, catsEffect)
