package com.perikov.osgi.http4s

import cats.effect.{GenTemporal, Resource}
import org.http4s.HttpRoutes

trait RouteHandler:
  def handler[F[_]](using F: GenTemporal[F, Throwable]): Resource[F, HttpRoutes[F]]