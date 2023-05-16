package com.perikov.osgi.javafx.platform

import javafx.scene.control
import org.osgi.annotation.versioning.*

trait StageProvider:
  def stage(runner: JavaFXRunner): javafx.stage.Stage


@ConsumerType
trait TabProvider:
  lazy val tab: control.Tab

@ProviderType
trait TabHost:
  def addTab(tab: control.Tab): Unit
  def removeTab(tab: control.Tab): Unit
