package com.perikov.osgi4cats.framework
import org.osgi.framework as osgi
import com.perikov.osgi4cats.Wrapper

trait Bundle[F[_]] extends Wrapper[osgi.Bundle]:
  def getState: F[BundleState]