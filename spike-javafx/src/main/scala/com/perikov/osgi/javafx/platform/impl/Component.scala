package com.perikov.osgi.javafx.platform.impl

import org.osgi.service.component.annotations.*
import org.osgi.service.log.{LoggerFactory,Logger}

import com.perikov.osgi.javafx.platform.JavaFXRunner


@Component(
  scope = ServiceScope.SINGLETON,
  service = Array(classOf[JavaFXRunner])
)
private class JavaFXRunnerImpl @Activate (
   @Reference(service = classOf[LoggerFactory]) val log: Logger
   ) 
extends JavaFXRunner:
  import javafx.application.Platform
  val javafxVersion = System.getProperty("javafx.version")
  
  try 
    Platform.startup(() => ())
    Platform.setImplicitExit(false)
    log.info(s"Started JavaFX Platform $javafxVersion")
  catch 
    case e: IllegalStateException => 
      log.warn(s"JavaFX Platform $javafxVersion already started")
    case e: Throwable => 
      log.error("Failed to start JavaFX Platform", e)
      throw e

end JavaFXRunnerImpl


