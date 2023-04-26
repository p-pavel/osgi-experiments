package com.perikov.osgi.spike
import impl.Test
import org.osgi.framework.{BundleActivator, BundleContext}
import org.osgi.service.component.annotations.*

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.effect.implicits.*

object WebServer extends IOApp.Simple:
  import org.http4s
  import http4s.ember.server.EmberServerBuilder
  import com.comcast.ip4s.*
  import http4s.server.staticcontent
  lazy val staticResources =
    staticcontent
      .resourceServiceBuilder[IO]("/static")
      .withClassLoader(Option(getClass.getClassLoader()))
      .withCacheStrategy(staticcontent.MemoryCache())
      .toRoutes
  val server               = EmberServerBuilder
    .default[IO]
    .withHost(host"localhost")
    .withPort(port"8080")
    .withHttpApp(staticResources.orNotFound)
    .build
  def run                  = server.useForever
end WebServer

@Component(immediate = true)
class Activator:
  import cats.effect.unsafe.implicits.global
  var dealloc                   = IO.unit
  @Activate
  def start(ctx: BundleContext) =
    dealloc = WebServer.server.allocated.unsafeRunSync()._2
  @Deactivate
  def stop()                    =
    dealloc.unsafeRunSync()
end Activator
