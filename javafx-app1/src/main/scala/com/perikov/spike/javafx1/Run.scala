package com.perikov.spike.javafx1

import atlantafx.base.theme.*
import javafx.animation.AnimationTimer
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.fxml.FXMLLoader
import javafx.scene.{Node, Parent, chart}
import chart.LineChart
import chart.{XYChart, LineChart}
import XYChart.Series

import java.beans.BeanProperty
import scala.compiletime.uninitialized
import scala.util.chaining.*

trait FXRun:
  def apply(f: => Unit): Unit

def constructApp(parent: Parent)(using run: FXRun) =
  import javafx.stage.Stage
  import javafx.scene.Scene
  import javafx.scene.layout.BorderPane
  import javafx.scene.control.Label
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

import javafx.fxml.FXML

@FXML
class Controller:
  import javafx.scene.control.*
  println("Created controller")

  object timer extends AnimationTimer:
    val phaseOffset                        = SimpleDoubleProperty(0.0)
    val timeStart: Option[Long]            = None
    override def handle(nanos: Long): Unit =
      phaseOffset.set(nanos / 1e9)
  end timer

  private def fillData(
      numPoints: Int,
      start: Double,
      freq: Double,
      phase: Double
  ) =
    val data = (0 until numPoints).map(x =>
      val off = x.toDouble / numPoints
      val t   = start + off
      XYChart.Data(
        off * 2 * math.Pi,
        math.sin(t * freq * 2 * math.Pi + phase)
      )
    )
    XYChart.Series("Func", FXCollections.observableArrayList(data*))

  @FXML
  def initialize(): Unit =

    toggleFreeze
      .selectedProperty()
      .addListener((_, _, selected) =>
        if selected then timer.stop()
        else timer.start()
      )
    phaseLabel
      .textProperty()
      .bind(phase.valueProperty().asString("%1.2f"))
    frequencyLabel
      .textProperty()
      .bind(frequency.valueProperty().asString("%02.2f"))

    val params =
      for
        f <- frequency.valueProperty()
        s <- timer.phaseOffset
        p <- phase.valueProperty()
        w <- lineChart.widthProperty()
      yield (w.intValue(), s.doubleValue(), f.doubleValue(), p.doubleValue())

    lineChart
      .dataProperty()
      .bind(
        params.map(d => FXCollections.observableArrayList(fillData.tupled(d)))
      )
    val themes = FXCollections.observableArrayList(
      PrimerDark(),
      NordDark(),
      PrimerLight(),
      NordLight()
    )

    themeChoice
      .tap(_.setItems(themes))
      .tap(
        _.setConverter(
          new javafx.util.StringConverter[Theme]:
            override def toString(t: Theme): String        = t.getName()
            override def fromString(string: String): Theme = ???
        )
      )
      .tap(_.setValue(themes.get(0)))
      .tap(_.valueProperty().addListener((_, _, theme) => {
        lineChart
          .getScene()
          .setUserAgentStylesheet(theme.getUserAgentStylesheet())
      }))

    timer.start()
  @FXML @BeanProperty
  private var themeChoice: ChoiceBox[Theme]        = compiletime.uninitialized
  @FXML @BeanProperty
  private var toggleFreeze: ToggleButton           = compiletime.uninitialized
  @FXML @BeanProperty
  private var phase: Slider                        = compiletime.uninitialized
  @FXML @BeanProperty
  private var frequency: Slider                    = compiletime.uninitialized
  @FXML @BeanProperty
  private var frequencyLabel: Label                = compiletime.uninitialized
  @FXML @BeanProperty
  private var phaseLabel: Label                    = compiletime.uninitialized
  @FXML @BeanProperty
  private var lineChart: LineChart[Double, Double] = compiletime.uninitialized

end Controller

@main
def run =
  import javafx.application.Platform
  Platform.startup(() => ())
  given FXRun        = f => Platform.runLater(() => f)
  val parent: Parent = FXMLLoader.load(getClass().getResource("/main.fxml"))
  println(s"Loaded: $parent")

  constructApp(parent)
