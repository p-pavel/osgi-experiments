package com.perikov.osgi.spike.impl

import cats.*
import cats.effect.*
import cats.implicits.*
import cats.effect.implicits.*
import org.http4s
import http4s.HttpRoutes
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