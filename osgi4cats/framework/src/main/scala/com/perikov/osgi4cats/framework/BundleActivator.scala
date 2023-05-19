package com.perikov.osgi4cats.framework
import org.osgi.framework as osgi
import impl.*


/** implements OSGi [[org.osgi.framework.BundleActivator]]
 *  using [[cats.effect.Resource]] 
 *  Subclass this class but provide public constructor with no arguments
 */
abstract class BundleActivator extends osgi.BundleActivator:
  import cats.effect.*
  protected def start[F[_]: Async](ctx: BundleContext[F]): Resource[F, Unit] 
  import cats.effect.unsafe.implicits.global

  private var release: IO[Unit]= IO.unit
  final override def start(context: osgi.BundleContext): Unit = 
    release = start[IO](BundleContextImpl(context)).allocated.unsafeRunSync()._2
  final override def stop(context: osgi.BundleContext): Unit = 
    release.unsafeRunSync()
  
