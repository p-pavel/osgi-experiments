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

lazy val spike =
  project
    .enablePlugins(SbtOsgi)
    .settings(
      deployOsgiSettings,
      OsgiKeys.bundleSymbolicName     := "com.perikov.osgi.spike",
      OsgiKeys.bundleVersion          := version.value,
      OsgiKeys.failOnUndecidedPackage := true,
      OsgiKeys.additionalHeaders      := Map(
        "Bundle-Description" -> "Just some tests to check bundle generation",
        "Bundle-Name"        -> "Perikov :: OSGi :: Spikes :: spike1"
      ),
      OsgiKeys.importPackage          := Seq(
        "org.http4s**;version=\"[0.23,1.0)\"",
        "scala**;version=\"[3.2,4.0)\"",
        "cats;cats.data;cats.syntax;version=\"[2.9,3.0)\"",
        "cats.effect**;version=\"[3.4,4)\"",
        "com.comcast**;version=\"[3.3,4)\"",
        "fs2.**;version=\"[3.6,4.0)\"",
        "*"
      ),
      OsgiKeys.exportPackage          := Seq("com.perikov.osgi.http4s;version=1.0.0"),
      libraryDependencies ++=
        Seq(
          "org.osgi"    % "org.osgi.core"       % "6.0.0",
          "org.osgi"    % "osgi.cmpn"           % "7.0.0",
          "org.http4s" %% "http4s-ember-server" % "0.23.18",
          "org.http4s" %% "http4s-dsl"          % "0.23.18"
          // "com.lihaoyi" %% "scalatags" % "0.12.0",

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
