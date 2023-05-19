package com.perikov.osgi4cats.framework.impl
import com.perikov.osgi4cats.framework.*
import org.osgi.framework as osgi
import cats.effect.IO

import cats.effect.Sync
case class BundleContextImpl(ctx: osgi.BundleContext) extends BundleContext[IO]:
  override def getBundle: Bundle[IO]  = BundleImpl(ctx.getBundle())
  override def getProperty(s: String) =
    IO.delay(Option(ctx.getProperty(s)))
