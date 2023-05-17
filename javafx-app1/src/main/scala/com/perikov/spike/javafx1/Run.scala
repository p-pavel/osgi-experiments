package com.perikov.spike.javafx1

import atlantafx.base.theme.*
import javafx.fxml.FXMLLoader
import javafx.scene.{ Parent}

import scala.util.chaining.*

trait FXRun:
  def apply(f: => Unit): Unit

def constructApp(parent: Parent)(using run: FXRun) =
  import javafx.stage.Stage
  import javafx.scene.Scene
  run {
    val stage = Stage()
    stage.setTitle("Hello World!")
    stage.setWidth(800)
    stage.setHeight(600)

    val scene = Scene(parent)
    scene.setUserAgentStylesheet(PrimerDark().getUserAgentStylesheet())
    stage.setScene(scene)
    stage.show()
  }

@main
def run =
  import javafx.application.Platform
  Platform.startup(() => ())
  given FXRun        = f => Platform.runLater(() => f)
  val resourceURL = getClass().getResource("/main.fxml")
  val parent: Parent = FXMLLoader.load(resourceURL)


  constructApp(parent)
