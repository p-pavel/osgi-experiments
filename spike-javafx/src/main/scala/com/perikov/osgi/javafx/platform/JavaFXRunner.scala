package com.perikov.osgi.javafx.platform

import javafx.application.Platform

trait JavaFXRunner:
  final def run(r: Runnable): Unit = Platform.runLater(r)
  final def run(f: => Unit): Unit = run(() => f)

  /** This is a blocking call. It blocks until result is available from JavaFX
    * thread
    */
  final def call[A](f: => A): A =
    object runner extends Runnable:
      private var result: A = compiletime.uninitialized
      private var done      = false
      def run(): Unit       =
        synchronized {
          result = f
          done = true
          notifyAll()
        }
      def get               =
        synchronized {
          while !done do wait()
          result
        }
    end runner
    import util.chaining.*
    runner.tap(run).get
  end call
end JavaFXRunner
