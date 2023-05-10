package com.perikov.osgi.javafx.platform.impl

import org.osgi.service.component.annotations.*
import org.osgi.service.log.{LoggerFactory, Logger}


import org.osgi.util.tracker.{ServiceTracker, ServiceTrackerCustomizer}
import org.osgi.framework.{BundleContext,ServiceReference}

import com.perikov.osgi.javafx.platform.{TabHost, TabProvider}

@Component(
  immediate = true,
  scope = ServiceScope.SINGLETON,
  service= Array()
  )
class TabWhiteboard @Activate (
  bundleCtx: BundleContext,
  @Reference private val tabHost: TabHost,
  @Reference(service  = classOf[LoggerFactory]) private val log: Logger
): 
  type Ref = ServiceReference[TabProvider]
  log.info("Starting TabWhiteboard")
  private val tracker = ServiceTracker[TabProvider, TabProvider](bundleCtx, classOf[TabProvider],null)
  tracker.open()

  def addingService(reference: Ref): TabProvider = 
    val tabProvider = bundleCtx.getService(reference)
    val tab = tabProvider.tab
    log.info(s"Adding tab ${tab.getText()}")
    tabHost.addTab(tab)
    tabProvider

  def modifiedService(reference: Ref, service: TabProvider): Unit = 
    log.info(s"Modified service ${service}")
    
  def removedService(reference: Ref, service: TabProvider): Unit = 
    val tab = service.tab
    log.info(s"Removing service ${tab.getText()}")
    tabHost.removeTab(tab)
    bundleCtx.ungetService(reference)

  @Deactivate
  def stop(): Unit = 
    log.info("Stopping TabWhiteboard")
    tracker.close()

  
