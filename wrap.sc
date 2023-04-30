#! env -S  scala-cli shebang -deprecation 


/* Naive utilities to generate wrap:mvn urls */

//>using scala "3.2.2"

/** maven artifact. Do not use sbt's ModuleID to keep thinks self-contained
  */
case class Artifact(
    organization: String,
    name: String,
    version: String,
    readableName: String,
    scalaVersion: Option[String] = Option("3")
):
  def withoutScalaVersion = copy(scalaVersion = None)

  def versionSuffix = scalaVersion.fold("")(v => s"_$v")
  def baseURL       = s"wrap:mvn:$organization/$name$versionSuffix/$version"
  def bundleVersion = s"Bundle-Version=$version"
  def bundleName    =
    import java.net.URLEncoder as enc
    s"Bundle-Name=${enc.encode(readableName, "UTF-8")}"

  def bundleSymbolicName = s"Bundle-SymbolicName=$organization.$name"
  def packageExport      = s"Export-Package=*;version=$version"

  /** generate a wrap:mvn url string for the given artifact */
  def wrapUrl: String =
    baseURL + "$" + Seq(
      bundleVersion,
      bundleName,
      bundleSymbolicName,
      packageExport
    ).mkString("&")
end Artifact

/** typelevel is pervasive in the ecosystem, so we have a helper for it
  */
def typelevel(name: String, version: String, readableName: String) =
  Artifact("org.typelevel", name, version, readableName)

def cats(v: String): Seq[Artifact] =
  Seq(
    "cats-core"   -> "Cats :: Core",
    "cats-kernel" -> "Cats :: Kernel"
  ) map { (name, readableName) =>
    typelevel(name, v, readableName)
  }

def catsEffect(v: String): Seq[Artifact] =
  Seq(
    "cats-effect"        -> "Cats :: Effect",
    "cats-effect-kernel" -> "Cats :: Effect :: Kernel",
    "cats-effect-std"    -> "Cats :: Effect :: Std"
  ) map { (name, readableName) => typelevel(name, v, readableName) }

def http4s(v: String) =
  Seq(
    "http4s-core"         -> "Http4s :: Core",
    "http4s-dsl"          -> "Http4s :: DSL",
    "http4s-ember-core"   -> "Http4s :: Ember :: Core",
    "http4s-ember-server" -> "Http4s :: Ember :: Server",
    "http4s-ember-client" -> "Http4s :: Ember :: Client",
    "http4s-server"       -> "Http4s :: Server",
    "http4s-client"       -> "Http4s :: Client"
  ) map { (name, readableName) =>
    Artifact("org.http4s", name, v, readableName)
  }

/** Twitter's hpack is used by http4s */
val hpack = Artifact(
  "com.twitter",
  "hpack",
  "1.0.2",
  "Twitter :: HPack"
).withoutScalaVersion

val http4sCrypto = Artifact(
  "org.http4s",
  "http4s-crypto",
  "0.2.4",
  "Http4s :: Crypto"
)

def fs2(v: String) = Seq(
  "fs2-core" -> "FS2 :: Core",
  "fs2-io"   -> "FS2 :: IO"
) map { (name, readableName) => Artifact("co.fs2", name, v, readableName) }

val libs = fs2("3.6.1") ++ cats("2.9.0") ++ catsEffect("3.4.10") ++ http4s(
  "0.23.18"
) ++ Seq(
  hpack,
  http4sCrypto
)

/** sequence of install commands for Karaf */
val cmd = libs.map(_.wrapUrl).map(u => s"install '$u'").mkString(";\n")

println(cmd)
