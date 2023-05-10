package com.perikov.osgi.javafx.client

import org.osgi.service.component.annotations.*
import org.osgi.service.component.ComponentContext
import org.osgi.service.log.{LoggerFactory, Logger}

import org.osgi.framework.BundleContext
import java.util.Hashtable

import com.perikov.osgi.javafx.platform.*

import javafx.scene.layout.StackPane
import javafx.scene.control.*

import javafx.collections.ObservableList
import javafx.collections.FXCollections

import scala.jdk.CollectionConverters.*
import scala.util.chaining.*
import javafx.scene.layout.VBox
import javafx.scene.layout.BorderPane

@Component(service = Array(classOf[TabProvider]))
class JavaFXClient @Activate (
  @Reference val tabHost: TabHost,
  ctx: ComponentContext,
  @Reference(service = classOf[LoggerFactory]) val log: Logger
) extends TabProvider:

  lazy val tab = 
    val items = FXCollections.observableList((1 to 10000).map(s"=== Some item " + _).toList.asJava)
    val list = new ListView(items)
    Tab("Client", list)
    .tap(_.setClosable(false))

  private def onTabClosed(): Unit = 
    log.info("Tab closed")


end JavaFXClient

@Component
class ServiceRegistrator @Activate(
  ctx: BundleContext
) extends TabProvider:
  ctx.registerService(classOf[TabProvider], this, Hashtable())
  lazy val tab: Tab = 
    val vbox = VBox(Label("ServiceRegistrator"), Button("Click me"), Button("Another button"))
    val pane = BorderPane(vbox)
    vbox.setSpacing(5)
    Tab("ServiceRegistrator",pane )