package com.perikov.osgi.javafx
import platform.*
package platform.impl:

  import org.osgi.service.component.annotations.*
  import org.osgi.service.log.{Logger,LoggerFactory}

  import javafx.stage.Stage
  import javafx.scene.Scene
  import javafx.scene.control

  @Component(
    scope = ServiceScope.SINGLETON,
    service = Array(classOf[TabHost])
  )
  class TabHostImpl @Activate (
    @Reference(service = classOf[LoggerFactory]) private val log: Logger,
    @Reference(service = classOf[JavaFXRunner]) private val runner: JavaFXRunner
    
  ) extends TabHost:

    private val (_stage, _tabPane) = runner call {
      val stage = Stage()
      stage.setTitle("Tab Host")
      val tabPane = control.TabPane()
      stage.setScene(Scene(tabPane, 640, 480))
      stage.show()
      (stage, tabPane)
    }

    log.info(s"Created TabHost")

    def addTab(tab: control.Tab): Unit = 
      runner.run {
        if ! _tabPane.getTabs().add(tab)
          then log.warn(s"Failed to add tab")
      }
    def removeTab(tab: control.Tab): Unit = 
      runner.run {
        if !_tabPane.getTabs().remove(tab)
          then log.warn(s"Failed to remove tab")
      } 

    @Deactivate
    def stop(): Unit = 
      runner.run { _stage.close() }
      log.info(s"Closed TabHost")
  end TabHostImpl