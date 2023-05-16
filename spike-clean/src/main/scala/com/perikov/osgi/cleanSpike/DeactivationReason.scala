package com.perikov.osgi.cleanSpike

import org.osgi.service.component.ComponentConstants

enum DeactivationReason(val code: Int):
  case Unspecified
      extends DeactivationReason(
        ComponentConstants.DEACTIVATION_REASON_UNSPECIFIED
      )
  case Disabled
      extends DeactivationReason(
        ComponentConstants.DEACTIVATION_REASON_DISABLED
      )
  case ReferenceVanished
      extends DeactivationReason(
        ComponentConstants.DEACTIVATION_REASON_REFERENCE
      )
  case ConfigurationModified
      extends DeactivationReason(
        ComponentConstants.DEACTIVATION_REASON_CONFIGURATION_MODIFIED
      )

  case ConfigurationDeleted
      extends DeactivationReason(
        ComponentConstants.DEACTIVATION_REASON_CONFIGURATION_DELETED
      )
  case Disposed
      extends DeactivationReason(
        ComponentConstants.DEACTIVATION_REASON_DISPOSED
      )
  case BundleStopped
      extends DeactivationReason(
        ComponentConstants.DEACTIVATION_REASON_BUNDLE_STOPPED
      )

object DeactivationReason:
  lazy val byCode = DeactivationReason.values.map(r => r.code -> r).toMap