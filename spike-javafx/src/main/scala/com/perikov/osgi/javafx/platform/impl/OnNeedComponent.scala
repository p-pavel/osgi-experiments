package com.perikov.osgi.javafx.platform.impl
import org.osgi.service.component.annotations.*
import org.osgi.service.log.{LoggerFactory, Logger}

trait OnNeedComponent

@Component(immediate = false, enabled = true) 
class OnNeedComponentImpl @Activate (
  @Reference(service = classOf[LoggerFactory]) private val log: Logger,
) extends OnNeedComponent:
  log.info(s"Created OnNeedComponentImpl")
  @Deactivate
  def stop(): Unit = 
    log.info(s"Closed OnNeedComponentImpl")

end OnNeedComponentImpl

import cats.effect.*


@Component(immediate = true, enabled = false)
class ComponentUser @Activate (
  @Reference(service = classOf[OnNeedComponent]) private val onNeed: OnNeedComponent,
  @Reference(service = classOf[LoggerFactory]) private val log: Logger,
) :
  import unsafe.implicits.global
  private def t[F[_]]: Resource[F,Int] = Resource.pure(10)
  private val (_, release) = t[IO].allocated.unsafeRunSync()
  log.info(s"Created ComponentUser")
  @Deactivate
  private def stop(): Unit = 
    log.info(s"Closed ComponentUser")
    release.unsafeRunSync()

end ComponentUser

