package com.perikov.osgi.spike
import impl.Test
import org.osgi.framework.{BundleActivator, BundleContext}
import org.osgi.service.component.annotations.*

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.effect.implicits.*
import org.http4s
import http4s.HttpRoutes
import org.osgi.service.log.*
import com.comcast.ip4s.*



object WebServer extends IOApp.Simple:
  import http4s.ember.server.EmberServerBuilder
  import http4s.server.staticcontent
  lazy val staticResources: HttpRoutes[IO] =
    staticcontent
      .resourceServiceBuilder[IO]("/static")
      .withClassLoader(Option(getClass.getClassLoader()))
      .withCacheStrategy(staticcontent.MemoryCache())
      .withPreferGzipped(true)
      .toRoutes
  import http4s.dsl.io.*
  import http4s.syntax.literals.uri
  import org.http4s.headers.Location
  val redirectToIndex: HttpRoutes[IO]      =
    HttpRoutes.of[IO] { case GET -> Root =>
      MovedPermanently(Location(uri"/index.html"))
    }
  def server(port: Port = port"8080") = 
    EmberServerBuilder
    .default[IO]
    .withHost(host"0.0.0.0")
    .withPort(port)
    .withHttpApp((staticResources <+> redirectToIndex).orNotFound)
    .build
  def run                                  = server().useForever
end WebServer

@Component(
  property = Array("port:Short=8080", "useCache:Boolean=false"),
  immediate = true,
  configurationPolicy = ConfigurationPolicy.OPTIONAL,
)
// @Config(configurationPid = "com.perikov.osgi.spike.Activator")
class Activator @Activate
 (
 @Reference(service = classOf[LoggerFactory]) log: Logger, props: java.util.Map[String,?]
 ) :
  def handler = ???

  import cats.effect.unsafe.implicits.global
  private val logger = log
  logger.info(s"Starting with $props")

  val  port                  =
    for
      portVal   <- Option(props.get("port")).toRight("No 'port' property")
      portOption = portVal match
                     case i: Short => Port.fromInt(i)
                     case s: String  => Port.fromString(s)
                     case other => None
      res       <- portOption.toRight(s"Invalid port value: $portVal")
    yield res
  val t: Resource[IO, Port] =
    Resource.pure(port.leftMap(IllegalArgumentException.apply(_))).rethrow
  logger.info(s"Port: $port")
  private val dealloc       = t.flatMap(WebServer.server(_)).allocated.unsafeRunSync()._2
  logger.info("Started")

  @Deactivate
  def stop() =
    logger.warn("Stopping")
    dealloc.unsafeRunSync()
    logger.info("Stopped")

end Activator
