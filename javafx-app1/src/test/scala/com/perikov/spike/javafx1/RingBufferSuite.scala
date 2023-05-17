package com.perikov.spike.javafx1
import scala.reflect.ClassTag
import java.util.AbstractList

import impl.RingBuffer

class RingBufferSuite extends munit.FunSuite:
  test("Empty buffer should have size 0") {
    assertEquals(RingBuffer[Int](10).size, 0)
  }
  
  test("Size is always less then capacity") {
    val buf = RingBuffer[Int](10)
    for i <- 0 to 100 do 
      assertEquals(buf.size, 10 min i)
      buf.add(i)
  }

  test("Buffer should contain added elements") {
    val capacity = 10
    val elements = 100
    val buf = RingBuffer[Int](capacity)
    for i <- 0 until elements do 
      buf.add(i)
    
    for i <- 0 until capacity do
      assertEquals(buf.get(i), elements - capacity + i)
  }

  test("Supports addAll") {
    import scala.jdk.CollectionConverters.*
    val buf = RingBuffer[Int](10)
    val elems = (0 until 5).asJava
    buf.addAll(elems)
    assertEquals( buf.toArray().toSeq, elems.toArray.toSeq, "Buffer should contain all added elements")
  }

  test("Supports clear") {
    val buf = RingBuffer[Int](10)
    buf.add(10)
    buf.clear()
    assertEquals(buf.size, 0, "Buffer should be empty after clear")
  }

  test("Wrapping to observable list") {
    import javafx.collections.{FXCollections, ObservableList}
    val buf = RingBuffer[Int](10)
    val col = FXCollections.observableArrayList[ObservableList[Int]]()
    col.add(FXCollections.observableList(buf))
    var invalidated = false
    col.addListener(_ => invalidated = true)
    col.set(0, FXCollections.observableList(buf))
    assert(invalidated, "Observable list should be invalidated when buffer changes")
  }

end RingBufferSuite
