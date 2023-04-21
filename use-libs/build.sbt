val useLibs = project
  .in(file("."))
  .enablePlugins(SbtOsgi, JavaAppPackaging)
  .settings(
    osgiSettings,
    resolvers += Resolver.mavenLocal,
    name := "test-dependency",
    autoScalaLibrary := false,
    libraryDependencies := Seq(
      "com.perikov" %% "scala3-library" % "3.2.2-SNAPSHOT",
      ),
    OsgiKeys.exportPackage := Seq("com.perikov.spikes.p2"),
    // OsgiKeys.importPackage += "scala.deriving;scala;version=\"[3.2.2,4)\"",
  )
