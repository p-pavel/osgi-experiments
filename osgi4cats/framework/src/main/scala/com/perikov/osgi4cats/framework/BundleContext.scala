package com.perikov.osgi4cats.framework
import com.perikov.osgi4cats.*
import org.osgi.framework as osgi
trait BundleContext[F[_]] extends BundleReference[F], Wrapper[osgi.BundleContext]:
  def getProperty(s: String): F[Option[String]]

