package com.perikov.osgi

/** Some wrappers to make http4s work with OSGi
  */
package http4s:
  import cats.effect.{GenTemporal, Resource}
  import org.http4s.HttpRoutes
  import org.osgi.annotation.versioning.*

  @ConsumerType
  trait RouteHandler:
    def handler[F[_]](using
        F: GenTemporal[F, Throwable]
    ): Resource[F, HttpRoutes[F]]
end http4s
