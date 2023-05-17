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
import javafx.fxml.FXML

trait Oscilloscope:
  def addMeasurement(m:Measurement): Unit

@FXML
class Controller :
  import javafx.scene.control.*

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
      phase: Double,
      noiseLevel: Double
  ) =
    val data = (0 until numPoints).map(x =>
      val off = x.toDouble / numPoints
      val t   = start + off
      XYChart.Data(
        off * 2 * math.Pi,
        math.sin(t * freq * 2 * math.Pi + phase) + (math.random()*2 - 1) * noiseLevel
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
        n <- noiseSlider.valueProperty()
      yield (w.intValue(), s.doubleValue(), f.doubleValue(), p.doubleValue(), n.doubleValue())

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
  @FXML 
  private var noiseSlider: Slider                = compiletime.uninitialized
  @FXML
  private var themeChoice: ChoiceBox[Theme]        = compiletime.uninitialized
  @FXML
  private var toggleFreeze: ToggleButton           = compiletime.uninitialized
  @FXML
  private var phase: Slider                        = compiletime.uninitialized
  @FXML
  private var frequency: Slider                    = compiletime.uninitialized
  @FXML
  private var frequencyLabel: Label                = compiletime.uninitialized
  @FXML
  private var phaseLabel: Label                    = compiletime.uninitialized
  @FXML
  private var lineChart: LineChart[Double, Double] = compiletime.uninitialized
end Controller