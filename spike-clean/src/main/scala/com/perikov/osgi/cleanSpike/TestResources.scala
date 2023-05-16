package com.perikov.osgi.cleanSpike
import cats.effect.*
import cats.effect.implicits.*
import scala.annotation.constructorOnly
import cats.effect.unsafe.IORuntime

import org.osgi.service
import service.component
import component.annotations.*
import component.*
import service.log.*
import com.perikov.osgi.cleanSpike.DeactivationReason

@Component
class TestResources @Activate(ctx: ComponentContext, @Reference(service= classOf[LoggerFactory]) logger: Logger):
  logger.info("Activating TestResources")
  @Deactivate
  private def deactivate(reason: Int): Unit =
    println(
      s"Deactivating TestResources (${DeactivationReason.byCode(reason)})"
    )