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

lazy val framework   = osgi4Cats("framework")
lazy val service_log = osgi4Cats("service.log")

lazy val root =
  project
    .in(file("."))
    .aggregate(framework, service_log)

def osgi4Cats(name: String) = {
  val projectId = "osgi4cats_" + name.replace(".", "_")
  val subfolder = name.replace(".", "/")
  bundleProject(projectId, s"osgi4cats/$subfolder")
}

def bundleProject(name: String, base: String) =
  sbt
    .Project(name, file(base))
    .enablePlugins(SbtOsgi)
    .settings(
      osgiSettings,
      OsgiKeys.bundleSymbolicName := s"osgi4cats.$name",
      OsgiKeys.importPackage      := Seq(
        "scala;scala.**;version=\"[3.2,4.0)\"",
        "cats.effect;version=\"[3.5,4.0)\"",
        "*"
      ),
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-effect" % "3.5.0",

        "org.osgi" % "org.osgi.framework"                     % "1.10.0",
        "org.osgi" % "org.osgi.service.component"             % "1.4.0",
        "org.osgi" % "org.osgi.service.component.annotations" % "1.4.0",
        "org.osgi" % "org.osgi.core"                          % "6.0.0",
        "org.osgi" % "org.osgi.service.log"                   % "1.4.0"
      )
    )
