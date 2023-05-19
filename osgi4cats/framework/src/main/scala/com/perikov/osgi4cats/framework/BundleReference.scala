package com.perikov.osgi4cats.framework
import org.osgi.framework as osgi
import com.perikov.osgi4cats.*

trait BundleReference[F[_]] extends Wrapper[osgi.BundleReference]:
  def getBundle: Bundle[F] //TODO: effectful? lazy val?
