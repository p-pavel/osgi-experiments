package com.perikov.spikes.p2

object Q:
  val t = Seq.empty
  case class Person(name:String)
  given CanEqual[Person, Person] = CanEqual.derived