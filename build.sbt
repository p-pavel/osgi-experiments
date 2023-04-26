lazy val osgiLibraryBundle  = taskKey[File](
  "Create a library OSGi bundle (it does not have a classpath, only includes jars)"
)
lazy val karafDirectory     = settingKey[File]("Karaf directory")
lazy val deployToKaraf      = taskKey[File]("Deploy to karaf")
enablePlugins(SbtOsgi)
lazy val deployOsgiSettings = osgiSettings ++ Seq(
  deployToKaraf     := {
    val bundle = OsgiKeys.bundle.value
    val karaf  = karafDirectory.value
    IO.copyFile(bundle, karaf / bundle.getName)
    karaf / bundle.getName
  },
  publishMavenStyle := false
)

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
      deployOsgiSettings,
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

lazy val cats =
  project
    .in(file("libs/cats"))
    .dependsOn(scala3Lib)
    .enablePlugins(SbtOsgi)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("cats-")),
      OsgiKeys.exportPackage := Seq(
        "cats;cats.**;version=2.9.0"
      ),
      libraryDependencies    := Seq(
        "org.typelevel" %% "cats-core"   % "2.9.0" notTransitive (),
        "org.typelevel" %% "cats-kernel" % "2.9.0" notTransitive ()
      )
    )

lazy val catsEffect =
  project
    .in(file("libs/cats-effect"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib)
    .settings(
      deployOsgiSettings,
      libraryDependencies    := Seq(
        "org.typelevel" %% "cats-effect"        % "3.4.9",
        "org.typelevel" %% "cats-effect-kernel" % "3.4.9",
        "org.typelevel" %% "cats-effect-std"    % "3.4.9"
      ),
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("cats-effect-")),
      OsgiKeys.importPackage := Seq(
        "cats;cats.kernel;cats.syntax;cats.arrow;cats.data;version=\"[2.9,3)\"",
        "*"
      ), // TODO: Why?!!
      OsgiKeys.exportPackage := Seq("cats.effect;cats.effect.**;version=3.4.9")
    )

lazy val scodec    =
  project
    .in(file("libs/scodec"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("scodec-")),
      OsgiKeys.exportPackage := Seq(
        "scodec.bits;version=1.1.37"
      ),
      libraryDependencies    := Seq(
        "org.scodec" %% "scodec-bits" % "1.1.37"
      )
    )
lazy val literally =
  project
    .in(file("libs/literally"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("literally")),
      OsgiKeys.exportPackage := Seq(
        "org.typelevel.literally;version=1.1.0"
      ),
      libraryDependencies    := Seq(
        "org.typelevel" %% "literally" % "1.1.0"
      )
    )
lazy val ip4s      =
  project
    .in(file("libs/ip4s"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib, catsEffect, cats, literally)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("ip4s-")),
      OsgiKeys.exportPackage := Seq(
        "com.comcast.ip4s;version=3.3.0"
      ),
      libraryDependencies    := Seq(
        "com.comcast" %% "ip4s-core" % "3.3.0" notTransitive ()
      )
    )
lazy val fs2       =
  project
    .in(file("libs/fs2"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib, cats, catsEffect, scodec, ip4s)
    .settings(
      deployOsgiSettings,
      OsgiKeys.exportPackage := Seq(
        "fs2;fs2.**;version=3.6.1"
      ),
      OsgiKeys.importPackage := Seq(
        "jnr.unixsocket;resolution:=optional",
        "*"
      ),
      libraryDependencies    := Seq(
        "co.fs2" %% "fs2-core" % "3.6.1" notTransitive (),
        "co.fs2" %% "fs2-io"   % "3.6.1" notTransitive ()
      )
    )

lazy val catsParse =
  project
    .in(file("libs/cats-parse"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib, cats, catsEffect, scodec, ip4s)
    .settings(
      deployOsgiSettings,
      libraryDependencies    := Seq(
        "org.typelevel" %% "cats-parse" % "0.3.9" notTransitive ()
      ),
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("cats-parse")),
      OsgiKeys.exportPackage := Seq(
        "cats.parse;cats.parse.**;version=0.3.9"
      )
    )

lazy val keypool =
  project
    .in(file("libs/keypool"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib, cats, catsEffect)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("keypool")),
      OsgiKeys.exportPackage := Seq(
        "org.typelevel.keypool;version=0.4.8"
      ),
      libraryDependencies    := Seq(
        "org.typelevel" %% "keypool" % "0.4.8" notTransitive ()
      )
    )

lazy val log4cats =
  project
    .in(file("libs/log4cats"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib, cats, catsEffect)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("log4cats")),
      OsgiKeys.exportPackage := Seq(
        "org.typelevel.log4cats;org.typelevel.log4cats.slf4j;org.typelevel.log4cats.extras;org.typelevel.log4cats.syntax;version=2.6.0"
      ),
      libraryDependencies    := Seq(
        "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
      )
    )

lazy val vault = 
  project
    .in(file("libs/vault"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib, cats, catsEffect)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("vault")),
      OsgiKeys.exportPackage := Seq(
        "org.typelevel.vault;version=3.5.0"
      ),
      libraryDependencies    := Seq(
        "org.typelevel" %% "vault" % "3.5.0" notTransitive ()
      )
    )

lazy val caseInsensitive = 
  project
    .in(file("libs/case-insensitive"))
    .enablePlugins(SbtOsgi)
    .dependsOn(scala3Lib,cats)
    .settings(
      deployOsgiSettings,
      OsgiKeys.explodedJars  := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter(_.getName().contains("ci")),
      OsgiKeys.exportPackage := Seq(
        "org.typelevel.ci;version=1.3.0"
      ),
      libraryDependencies    := Seq(
        "org.typelevel" %% "case-insensitive" % "1.3.0" notTransitive ()
      )
    )

lazy val http4s =
  project
    .in(file("libs/http4s"))
    .dependsOn(
      scala3Lib,
      cats,
      catsEffect,
      fs2,
      scodec,
      ip4s,
      catsParse,
      keypool,
      log4cats,
      vault,
      caseInsensitive
    )
    .enablePlugins(SbtOsgi)
    .settings(
      deployOsgiSettings,
      OsgiKeys.exportPackage              := Seq(
        "org.http4s;org.http4s.**;version=0.23.18"
      ),
      OsgiKeys.explodedJars               := (Compile / dependencyClasspathAsJars).value
        .map(_.data)
        .filter { j =>
          val n = j.getName(); n.contains("http4s-") || n.contains("hpack")
        },
      libraryDependencies                 := Seq(
        "http4s-core",
        "http4s-dsl",
        "http4s-server",
        "http4s-client",
        "http4s-ember-core",
        "http4s-ember-server",
        "http4s-ember-client",

      ).map("org.http4s" %% _ % "0.23.18" notTransitive()),
      libraryDependencies += "org.http4s" %% "http4s-crypto" % "0.2.4" notTransitive(),
      libraryDependencies += "com.twitter" % "hpack" % "1.0.2" notTransitive()
    )

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
    .aggregate(
      cats,
      scala3Lib,
      catsEffect,
      scodec,
      fs2,
      ip4s,
      literally,
      http4s,
      catsParse,
      keypool,
      log4cats,
      vault,
      caseInsensitive
    )
