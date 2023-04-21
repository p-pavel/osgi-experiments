package com.perikov.spikes.packages
import org.osgi.framework.{BundleActivator, BundleContext}
import org.osgi.annotation.bundle.*

class testLatency extends BundleActivator:
  val period_ns = 1_000_000L
  val period_size = 10000
  var thread: Thread = null
  
  override def start(context: BundleContext): Unit =
    println("Starting testLatency")
    thread = new Thread(() => testLatency())
    thread.start()

  override def stop(context: BundleContext): Unit =
    thread.stop()
    println("Stopping testLatency")

  def testLatency() =
    for i <- 1 to 10000 do
      var maxMiss = 0L
      var sumMiss = 0.0
      var start = System.nanoTime()
      var count = 0
      while count < period_size do
        val now = System.nanoTime()
        val delta = now - start
        if delta > period_ns then
          count += 1
          val miss = delta - period_ns
          maxMiss = miss max maxMiss
          sumMiss += miss
          if miss > 100_000 then println(s"Missed by $miss ns")
          start += period_ns
      println(
        s"""Period: $i, Max miss: $maxMiss ns, average: ${sumMiss / period_size} ns, total: ${sumMiss / 1_000_000} ms (${100 * sumMiss / (period_size * period_ns)}%)"""
      )


