lazy val osgiLibraryBundle = taskKey[File](
  "Create a library OSGi bundle (it does not have a classpath, only includes jars)"
)
lazy val karafDirectory    = settingKey[File]("Karaf directory")
lazy val deployToKaraf     = taskKey[File]("Deploy to karaf")
enablePlugins(SbtOsgi)

inThisBuild {
  Seq(
    scalaVersion     := "3.2.2",
    version          := "0.1.0-SNAPSHOT",
    organization     := "com.perikov",
    autoScalaLibrary := false,
    scalacOptions ++= Seq("-deprecation"),
    karafDirectory   := baseDirectory.value / ".." / "deploy" / "deploy"
  )
}

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val scala3Lib =
  project
    .in(file("libs/scala3Lib"))
    .enablePlugins(SbtOsgi)
    .settings(
      autoScalaLibrary       := false,
      libraryDependencies    := Seq(
        "org.scala-lang" % "scala-library"    % "2.13.10",
        "org.scala-lang" % "scala3-library_3" % "3.2.2"
      ),
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data),
      OsgiKeys.exportPackage := Seq(
        "*;version=3.2.2"
      ),
      OsgiKeys.importPackage := Seq("*")
    )

lazy val cats      =
  project
    .in(file("libs/cats"))
    .dependsOn(scala3Lib)
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
      autoScalaLibrary       := false,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("cats-")),
      OsgiKeys.exportPackage := Seq(
        "cats;cats.**;version=2.9.0"
      ),
        
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
      osgiSettings,
      libraryDependencies    := Seq(
        "org.typelevel" %% "cats-effect" % "3.4.9"
      )
    )

// lazy val fs2 =
//   project
//     .in(file("libs/fs2"))
//     .enablePlugins(SbtOsgi)
//     .settings(
//       osgiLibSettings,
//       OsgiKeys.embeddedJars := (Compile / externalDependencyClasspath).value
//         .map(
//           _.data
//         )
//         .filter(_.getName.contains("fs2")), // TODO: hack
//       OsgiKeys.exportPackage := Seq(
//         "fs2;fs2.**;version=3.6.1"
//       ),
//       OsgiKeys.importPackage := Seq(
//         "scala;scala.**;version=\"[3.2,4)\"",
//         "cats;cats.kernel;cats.syntax;cats.data;cats.arrow;version=\"[2.9,3)\"",
//         "cats.effect;cats.effect.**;version=\"[3.4,4)\"",
//         "*"
//       ),
//       libraryDependencies    := Seq(
//         "co.fs2" %% "fs2-core" % "3.6.1",
//         "co.fs2" %% "fs2-io"   % "3.6.1"
//       )
//     )

// lazy val ip4s =
//   project
//     .in(file("libs/ip4s"))
//     .enablePlugins(SbtOsgi)
//     .settings(
//       osgiLibSettings,
//       OsgiKeys.embeddedJars := (Compile / externalDependencyClasspath).value
//         .map(
//           _.data
//         )
//         .filter(_.getName.contains("ip4s")), // TODO: hack
//       OsgiKeys.exportPackage := Seq(
//         "org.typelevel.ip4s;org.typelevel.ip4s.**;version=3.0.0"
//       ),
//       OsgiKeys.importPackage := Seq(
//         "scala;scala.**;version=\"[3.2,4)\"",
//         "cats;cats.kernel;cats.syntax;cats.data;cats.arrow;version=\"[2.9,3)\"",
//         "cats.effect;cats.effect.**;version=\"[3.4,4)\"",
//         "fs2;fs2.**;version=\"[3.6,4)\"",
//         "*"
//       ),
//       libraryDependencies    := Seq(
//         "org.typelevel" %% "ip4s-core"  % "3.0.0",
//         "org.typelevel" %% "ip4s-circe" % "3.0.0"
//       )
//     )

lazy val spike =
  project
    .dependsOn(scala3Lib)
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
      OsgiKeys.bundleSymbolicName     := "com.perikov.osgi.spike",
      OsgiKeys.bundleVersion          := version.value,
      OsgiKeys.failOnUndecidedPackage := true,
      OsgiKeys.additionalHeaders      := Map(
        "Bundle-Description" -> "Just some tests to check bundle generation",
        "Bundle-Name"        -> "Perikv :: OSGi :: Spikes :: spike1"
      ),
      // OsgiKeys.importPackage          := Seq("com.perikov.osgi.spike", "*"),
      OsgiKeys.exportPackage          := Seq("com.perikov.osgi.spike;version=1.0.0"),
      libraryDependencies ++=
        Seq(
          "org.osgi" % "org.osgi.core" % "6.0.0",
          "org.osgi" % "osgi.cmpn"     % "7.0.0"
        )
    )
lazy val root  =
  project
    .in(file("."))
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
      deployToKaraf := {
        val karafDir = karafDirectory.value
        val bundle   = OsgiKeys.bundle.value
        val dest     = karafDir / bundle.getName
        IO.copyFile(bundle, dest)
        dest
      }
    )
    .aggregate(cats, scala3Lib, catsEffect )
