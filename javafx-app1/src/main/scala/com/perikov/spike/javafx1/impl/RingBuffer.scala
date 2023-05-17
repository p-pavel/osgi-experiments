package com.perikov.spike.javafx1.impl
import scala.reflect.ClassTag
import java.util.AbstractList

/** very simpllisting ring buffer. 
 * 
 * **ABSOULUTELY THREAD UNSAFE**
 */
class RingBuffer[@specialized(Double, Float, Int, Long) T:ClassTag](capacity: Int) extends AbstractList[T]:
  require(capacity > 0, "Capacity must be positive")
  private object indexes:
    private var _start = 0
    private var _size = 0
    def start = _start
    def size = _size
    def size_= (n: Int): Unit = 
      require(n >= 0 && n <= capacity, s"Size must be between 0 and $capacity")
      _size = n
    def start_= (n: Int): Unit =
      require(n >= 0 && n < capacity, s"Start must be between 0 < $capacity")
      _start = n
  end indexes
  
  export indexes.size
  import indexes.*

  private val _buf = new Array[T](capacity)
  inline def apply(i: Int) = get(i)
  override def get(i: Int): T = 
    if i < 0 || i >= size then throw new IndexOutOfBoundsException(s"Index $i out of bounds for size $size")
    _buf((start + i) % capacity)
  override def add(t: T): Boolean = 
    _buf((start + size) % capacity) = t
    if size == capacity 
      then start = (start + 1) % capacity
      else indexes.size += 1
    true
  override def clear(): Unit = indexes.size = 0
end RingBuffer