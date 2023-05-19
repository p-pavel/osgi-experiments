package com.perikov.osgi4cats.framework
import org.osgi.framework.Bundle as B

enum BundleState(val raw: Int) :
  case UNINSTALLED extends BundleState(B.UNINSTALLED)
  case INSTALLED   extends BundleState(B.INSTALLED)
  case RESOLVED    extends BundleState(B.RESOLVED)
  case STARTING    extends BundleState(B.STARTING)
  case STOPPING    extends BundleState(B.STOPPING)
  case ACTIVE      extends BundleState(B.ACTIVE)

object BundleState:
  given Conversion[BundleState, BundleStateMask] = s => BundleStateMask(s.raw)

  private lazy val byValue = values.map(v => v.raw -> v).toMap
  private [framework] def apply(raw: Int): BundleState = 
    byValue.get(raw).getOrElse(
      throw new IllegalArgumentException(s"Unknown bundle state: $raw")
    )



//TODO: is this class needed?
private case class BundleStateMask(val raw: Int):
  def |(other: BundleStateMask): BundleStateMask = 
    BundleStateMask( raw | other.raw)

  def states: Set[BundleState] = 
    BundleState.values.filter(v => (v.raw & raw) != 0).toSet

  override def toString(): String =
    val used = states
    def unknowns = used.foldLeft(raw)((acc, v) => acc & ~v.raw)
    assert(unknowns == 0, f"Unknown bundle state mask: $raw%08X")
    val stringMask = used.map(_.toString).mkString(" | ")
    s"BundleStateMask($stringMask)"
