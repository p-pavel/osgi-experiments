package com.perikov.osgi.spike
import impl.Test
import org.osgi.framework.{BundleActivator, BundleContext}
import org.osgi.annotation.versioning.{ProviderType, ConsumerType, Version}
import org.osgi.service.component.annotations.*

import cats.effect.*
import cats.implicits.*
import cats.effect.implicits.*
import org.osgi.service.log.*
import com.comcast.ip4s.*
import impl.WebServer

@Component(
  property = Array("port:Short=8080", "useCache:Boolean=false"),
  immediate = true,
  configurationPolicy = ConfigurationPolicy.OPTIONAL,
)
@ProviderType
// @Config(configurationPid = "com.perikov.osgi.spike.Activator")
class Activator @Activate
 (ctx: BundleContext,
 @Reference(service = classOf[LoggerFactory]) log: Logger, props: java.util.Map[String,?]
 ) :

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
  import scala.util.chaining.*
  private val portResource: Resource[IO, Port] = 
    port
    .leftMap(IllegalArgumentException.apply(_))
    .pipe(Resource.pure)
    .rethrow
  logger.info(s"Port: $port")
  private val dealloc       = 
    portResource
    .flatMap(WebServer.server(_))
    .allocated
    .unsafeRunSync()
    ._2
  logger.info("Started")

  @Deactivate
  def stop() =
    logger.warn("Stopping")
    dealloc.unsafeRunSync()
    logger.info("Stopped")

end Activator
