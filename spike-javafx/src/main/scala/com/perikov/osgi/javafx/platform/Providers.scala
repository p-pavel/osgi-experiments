package com.perikov.osgi.javafx.platform

import javafx.scene.control

trait StageProvider:
  def stage(runner: JavaFXRunner): javafx.stage.Stage

trait TabProvider:
  lazy val tab: control.Tab

trait TabHost:
  def addTab(tab: control.Tab): Unit
  def removeTab(tab: control.Tab): Unit
