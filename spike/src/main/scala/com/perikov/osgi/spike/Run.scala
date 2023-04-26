package com.perikov.osgi.spike
import impl.Test
import org.osgi.framework.{BundleActivator, BundleContext}
import org.osgi.service.component.annotations.*


@Component(immediate = true)
class Activator: 
  @Activate
  def start(ctx: BundleContext) = 
    println(Test.message)
    println(s"Using context $ctx")
  @Deactivate
  def stop() = println(s"Stopping")

@main
def run = println(Test.message)
