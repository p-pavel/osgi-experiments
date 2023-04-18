lazy val scalaLibs = 
  project
    .in(file("."))
    .enablePlugins(SbtOsgi)
    .settings(
      name := "scala-libs",
      osgiSettings,
      libraryDependencies := Seq(),
    )
