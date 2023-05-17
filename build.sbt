
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
      OsgiKeys.exportPackage      := Seq(
        "com.perikov.osgi.javafx.platform;version=0.1.1-SNAPSHOT"
      ),
      OsgiKeys.importPackage      := Seq(
        "scala;scala.**;version=\"[3.2,4.0)\"",
        "cats.effect;cats.effect.**;version=\"[3.4,4.0)\"",
        "*"
      ),
      Compile / packageBin / packageOptions += Package.ManifestAttributes(
        "Meta-Persistence" -> "OSGI-INF/persistence.xml"
      ),
      libraryDependencies ++= Seq(
        "org.osgi"             % "org.osgi.service.component"             % "1.4.0",
        "org.osgi"             % "org.osgi.annotation.versioning"         % "1.1.2",
        "org.osgi"             % "org.osgi.service.component.annotations" % "1.4.0",
        "org.osgi"             % "org.osgi.core"                          % "6.0.0",
        "org.osgi"             % "org.osgi.service.log"                   % "1.4.0",
        "org.osgi"             % "org.osgi.service.jpa"                   % "1.1.0",
        "org.apache.aries.jpa" % "org.apache.aries.jpa.api"               % "2.7.3",
        "org.openjfx"          % "javafx-controls"                        % "20.0.1",
        "org.typelevel"       %% "cats-effect"                            % "3.4.6",
        "javax.persistence"    % "javax.persistence-api"                  % "2.2"
      )
    )

lazy val javafxSpikeClient = myProj("javafxSpikeClient", "spike-javafx-client")
  .dependsOn(javafxSpike)
  .settings(
    libraryDependencies += "org.openjfx" % "javafx-controls" % "20.0.1"
  )

lazy val persistenceSpike = myProj("persistenceSpike", "spike-persistence")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"    %% "cats-effect"           % "3.5.0",
      "javax.persistence" % "javax.persistence-api" % "2.2"
    )
  )

lazy val cleanSpike = myProj("cleanSpike", "spike-clean")
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.0"
    )
  )

lazy val javafxApp = myProj("javafx-app1", "javafx-app1")
  .settings(
    fork := true,
    javaOptions ++=Seq("-XX:MaxGCPauseMillis=2", "-XX:+UseZGC", "-Xmx128m"),
    libraryDependencies ++= Seq(
      "org.openjfx" % "javafx-fxml" % "20.0.1",
      "org.openjfx" % "javafx-controls" % "20.0.1",
      "io.github.mkpaz" % "atlantafx-base" % "1.2.0",

      "org.typelevel" %% "cats-effect" % "3.5.0",
      "co.fs2" %% "fs2-core" % "3.7.0",

      "org.scalameta" %% "munit" % "0.7.29" % Test,

    )
  )

lazy val root =
  project
    .in(file("."))
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings
    )
    .aggregate(javafxSpike, javafxSpikeClient)

def myProj(name: String, base: String) =
  sbt
    .Project(name, file(base))
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
      OsgiKeys.bundleSymbolicName := s"com.perikov.osgi.$name",
      OsgiKeys.importPackage := Seq(
        "scala;scala.**;version=\"[3.2,4.0)\"",
        "*"
      ),
      libraryDependencies ++= Seq(
        "org.osgi" % "org.osgi.service.component"             % "1.4.0",
        "org.osgi" % "org.osgi.service.component.annotations" % "1.4.0",
        "org.osgi" % "org.osgi.core"                          % "6.0.0",
        "org.osgi" % "org.osgi.service.log"                   % "1.4.0"
      )
    )
