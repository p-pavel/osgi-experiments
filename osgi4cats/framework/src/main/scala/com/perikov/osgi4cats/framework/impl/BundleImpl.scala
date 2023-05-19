package com.perikov.osgi4cats.framework.impl

import com.perikov.osgi4cats
import osgi4cats.framework.*

import org.osgi.framework as osgi
import cats.effect.IO

final case class BundleImpl(bundle: osgi.Bundle) extends Bundle[IO]:
  override def getState = IO.delay(bundle.getState).map(BundleState(_))
