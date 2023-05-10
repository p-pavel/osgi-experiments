// lazy val osgiLibraryBundle  = taskKey[File](
//   "Create a library OSGi bundle (it does not have a classpath, only includes jars)"
// )
// lazy val karafDirectory     = settingKey[File]("Karaf directory")
// lazy val deployToKaraf      = taskKey[File]("Deploy to karaf")
// enablePlugins(SbtOsgi)
// lazy val deployOsgiSettings = osgiSettings ++ Seq(
//   deployToKaraf     := {
//     val bundle = OsgiKeys.bundle.value
//     val karaf  = karafDirectory.value
//     IO.copyFile(bundle, karaf / bundle.getName)
//     karaf / bundle.getName
//   },
//   publishMavenStyle := false
// )

inThisBuild {
  Seq(
    scalaVersion := "3.2.2",
    version      := "0.1.0-SNAPSHOT",
    organization := "com.perikov",
    scalacOptions ++= Seq(
      "-deprecation",
      "-source",
      "future-migration",
      "-rewrite"
    )
    // karafDirectory   := baseDirectory.value / ".." / "deploy" / "deploy"
  )
}

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val spike =
  project
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
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
      OsgiKeys.exportPackage          := Seq("com.perikov.osgi.http4s"),
      libraryDependencies ++=
        Seq(
          "org.osgi"    % "org.osgi.core"                    % "6.0.0",
          "org.osgi"    % "osgi.cmpn"                        % "7.0.0",
          "org.osgi"    % "org.osgi.framework"               % "1.10.0",
          "org.osgi"    % "org.osgi.annotation.versioning"   % "1.1.2",
          "org.osgi"    % "org.osgi.service.http.whiteboard" % "1.1.1",
          "org.osgi"    % "org.osgi.service.http"            % "1.2.2",
          "org.http4s" %% "http4s-ember-server"              % "0.23.18",
          "org.http4s" %% "http4s-dsl"                       % "0.23.18"
          // "com.lihaoyi" %% "scalatags" % "0.12.0",

        )
    )

lazy val javafxSpike =
  project
    .in(file("spike-javafx"))
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
      OsgiKeys.bundleSymbolicName := "com.perikov.osgi.javafx.platform",
      OsgiKeys.exportPackage      := Seq("com.perikov.osgi.javafx.platform"),
      OsgiKeys.importPackage      := Seq(
        "scala;scala.**;version=\"[3.2,4.0)\"",
        "*"
      ),
      libraryDependencies ++= Seq(
        "org.osgi"    % "org.osgi.service.component"             % "1.4.0",
        "org.osgi"    % "org.osgi.service.component.annotations" % "1.4.0",
        "org.osgi"    % "org.osgi.core"                          % "6.0.0",
        "org.osgi"    % "org.osgi.service.log"                   % "1.4.0",
        "org.openjfx" % "javafx-controls"                        % "20.0.1"
      )
    )

lazy val javafxSpikeClient = 
  project
    .in(file("spike-javafx-client"))
    .dependsOn(javafxSpike)
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
      OsgiKeys.bundleSymbolicName := "com.perikov.osgi.javafx.client",
      OsgiKeys.importPackage      := Seq(
        "scala;scala.**;version=\"[3.2,4.0)\"",
        "*"
      ),
      libraryDependencies ++= Seq(
        "org.osgi"    % "org.osgi.service.component"             % "1.4.0",
        "org.osgi"    % "org.osgi.service.component.annotations" % "1.4.0",
        "org.osgi"    % "org.osgi.core"                          % "6.0.0",
        "org.osgi"    % "org.osgi.service.log"                   % "1.4.0",
        "org.openjfx" % "javafx-controls"                        % "20.0.1"
      )
    )
lazy val camelSpike =
  project
    .in(file("spike-camel"))
    .enablePlugins(SbtOsgi)
    .settings(
      autoScalaLibrary            := true,
      osgiSettings,
      OsgiKeys.bundleSymbolicName := "com.perikov.osgi.spike.camel",
      OsgiKeys.importPackage      := Seq(
        "scala;scala.**;version=\"[3.2,4.0)\"",
        "cats.effect**;version=\"[3.4,4)\"",
        "cats;cats.syntax;version=\"[2.9,3.0)\"",
        "*"
      ),
      libraryDependencies ++= Seq(
        "org.osgi"       % "org.osgi.service.component"             % "1.4.0",
        "org.osgi"       % "org.osgi.service.component.annotations" % "1.4.0",
        "org.osgi"       % "org.osgi.core"                          % "6.0.0",
        "org.osgi"       % "org.osgi.service.log"                   % "1.4.0",
        "org.typelevel" %% "log4cats-core"                          % "2.6.0",
        "org.typelevel" %% "cats-effect"                            % "3.4.6",
        "org.openjfx"    % "javafx-controls"                        % "20.0.1"
      )
    )
lazy val root       =
  project
    .in(file("."))
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings
    )
