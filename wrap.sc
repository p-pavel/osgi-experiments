#! env -S scala-cli shebang -deprecation

/* Naive utilities to generate wrap:mvn urls */

//>using scala "3.2.2"
//>using lib "com.lihaoyi::scalatags:0.12.0"

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
  def packageImport      = "Import-Package=*"

  def wrapUrl: String =
    baseURL + "$" + Seq(
      bundleVersion,
      bundleName,
      bundleSymbolicName,
      packageExport
      // packageImport
    ).mkString("&")
end Artifact

//TODO: Generic ?
import scalatags.Text.all.*

case class KarafFeature(
    name: String,
    version: String,
    description: String,
    bundles: Seq[Artifact],
    features: Seq[KarafFeature] = Seq.empty
):
  def xmlTags: ConcreteHtmlTag[String] = tag("feature")(attr("name") := name)(
    attr("version")     := version,
    attr("description") := description,
    tag("feature")(attr("prerequisite") := true, "wrap"),
    features.map(_.xmlTags),
    bundles.map { b =>
      tag("bundle")(b.wrapUrl)
    }
  )
end KarafFeature

case class KarafFeatureRepository(
    name: String,
    features: KarafFeature*
):
  def xmlTags = tag("features")(
    xmlns        := "http://karaf.apache.org/xmlns/features/v1.4.0",
    attr("name") := name
  )(
    features.map(_.xmlTags)
  )

object bundles:
  /** typelevel is pervasive in the ecosystem, so we have a helper for it
    */
  def typelevel(version: String)(name: String, readableName: String) =
    Artifact("org.typelevel", name, version, readableName)

  def cats(v: String): Seq[Artifact] =
    Seq(
      "cats-core"   -> "Cats :: Core",
      "cats-kernel" -> "Cats :: Kernel"
    ) map typelevel(v)

  def catsEffect(v: String): Seq[Artifact] =
    Seq(
      "cats-effect"        -> "Cats :: Effect",
      "cats-effect-kernel" -> "Cats :: Effect :: Kernel",
      "cats-effect-std"    -> "Cats :: Effect :: Std"
    ) map typelevel(v)

  def http4s(v: String) =
    Seq(
      "http4s-core"         -> "Http4s :: Core",
      "http4s-dsl"          -> "Http4s :: DSL",
      "http4s-ember-core"   -> "Http4s :: Ember :: Core",
      "http4s-ember-server" -> "Http4s :: Ember :: Server",
      "http4s-ember-client" -> "Http4s :: Ember :: Client",
      "http4s-server"       -> "Http4s :: Server",
      "http4s-client"       -> "Http4s :: Client"
    ) map { Artifact("org.http4s", _, v, _) }

  /** Twitter's hpack is used by http4s */
  def hpack(v: String) = Artifact(
    "com.twitter",
    "hpack",
    v,
    "Twitter :: HPack"
  ).withoutScalaVersion

  def http4sCrypto(v: String) = Artifact(
    "org.http4s",
    "http4s-crypto",
    v,
    "Http4s :: Crypto"
  )

  def ip4s(v: String) =
    Artifact("com.comcast", "ip4s-core", v, "IP4S")

  def scodec(v: String) =
    Artifact("org.scodec", "scodec-bits", v, "Scodec")

  def fs2(v: String) = Seq(
    "fs2-core" -> "FS2 :: Core",
    "fs2-io"   -> "FS2 :: IO"
  ) map { Artifact("co.fs2", _, v, _) }

  def log4cats(v: String) =
    Seq(
      "log4cats-core"  -> "Log4Cats :: Core",
      "log4cats-slf4j" -> "Log4Cats :: SLF4J"
    ).map(typelevel(v))

  def vault(v: String) = typelevel(v)("vault", "Typelevel :: Vault")

  def catsParse(v: String) =
    typelevel(v)("cats-parse", "Cats :: Parse")

  def keypool(v: String) =
    typelevel(v)("keypool", "Typelevel :: KeyPool")

  def literally(v: String) =
    typelevel(v)("literally", "Typelevel :: Literally")

  def caseInsensitive(v: String) =
    typelevel(v)("case-insensitive", "Typelevel :: Case Insensitive")
end bundles

object features:
  import bundles as b
  def feature(
      name: String,
      version: String,
      description: String,
      bundles: String => Seq[Artifact],
      deps: KarafFeature*
  ) =
    KarafFeature(name, version, description, bundles(version), deps)

    
  def cats       = 
    feature("cats", "2.9.0", "Cats", b.cats)

  def catsEffect =
    feature("cats-effect", "3.4.10", "Cats Effect", b.catsEffect, cats)

  def fs2 =
    feature("fs2", "3.6.1", "FS2", v => b.fs2(v) ++ Seq(b.scodec("1.1.37")), catsEffect)

  def repo = KarafFeatureRepository("scala-libs", cats, catsEffect, fs2)

end features

import bundles.*
val libs: Seq[Artifact] =
  Seq(
    fs2("3.6.1"),
    cats("2.9.0"),
    catsEffect("3.4.10"),
    http4s("0.23.18"),
    log4cats("2.6.0"),
    Seq(
      ip4s("3.3.0"),
      scodec("1.1.37"),
      hpack("1.0.2"),
      http4sCrypto("0.2.4"),
      vault("3.5.0"),
      catsParse("0.3.9"),
      keypool("0.4.8"),
      literally("1.1.0"),
      caseInsensitive("1.3.0")
    )
  ).flatten

def karafInstallCommands = libs.map(_.wrapUrl).map(u => s"install '$u'").mkString(";\n")

println(features.repo.xmlTags.render)