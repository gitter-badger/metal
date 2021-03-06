package metal.mutable

import scala.reflect.{classTag, ClassTag}
import spire.algebra.Order
import spire.math.QuickSort
import spire.syntax.cfor._
import metal.{Dummy, Methods}

final class Buffer[@specialized V](var array: Array[V], var length: Int)(implicit val V: Methods[V], val ct: ClassTag[V]) extends metal.Buffer[V] with metal.mutable.Collection {

  def toImmutable = new metal.immutable.Buffer[V](array.clone, length) // TODO: trim the array

  def sort()(implicit order: Order[V]): Unit = {
    QuickSort.qsort(array, 0, length.toInt - 1)(order, ct)
  }

  def clear(): Unit = {
    array = ct.newArray(0)
    length = 0
  }

  def reset(): Unit = {
    cforRange(0 until length) { i =>
      array(i) = null.asInstanceOf[V]
    }
    length = 0
  }

  def result() = {
    val res = new metal.immutable.Buffer[V](array, length)
    array = ct.newArray(0)
    length = 0
    res
  }

  def +=(elem: V): this.type = {
    ensureLength(length + 1)
    array(length.toInt) = elem
    length += 1
    this
  }

  def update(idx: Long, v: V): Unit = {
    array(idx.toInt) = v
  }

  /** Grow if necessary the underlying array to accomodate at least n elements. */
  def ensureLength(n: Long): Dummy[V] = {
    val arrayLength: Long = array.length
    if (n > arrayLength) {
      var newLength: Long = arrayLength.toLong * 2
      while (n > newLength) newLength = newLength * 2
      if (newLength > Int.MaxValue) newLength = Int.MaxValue
      val newArray = V.newArray(newLength.toInt)
      Array.copy(array, 0, newArray, 0, length.toInt)
      array = newArray
    }
    null
  }

  def remove(idx: Long): V = {
    val last = length.toInt - 1
    if (idx < 0) throw new IndexOutOfBoundsException(idx.toString)
    else if (idx < last) {
      val v = array(idx.toInt)
      Array.copy(array, idx.toInt + 1, array, idx.toInt, last - idx.toInt)
      array(last) = null.asInstanceOf[V]
      length = last
      v
    } else if (idx == last) {
      val v = array(idx.toInt)
      array(last) = null.asInstanceOf[V]
      length = last
      v
    } else throw new IndexOutOfBoundsException(idx.toString)
  }

}

object Buffer {

  def empty[@specialized V:Methods:ClassTag]: Buffer[V] = new Buffer[V](classTag[V].newArray(16), 0)

  def apply[@specialized V:Methods:ClassTag](items: V*): Buffer[V] = {
    val array = classTag[V].newArray(items.size)
    val it = items.iterator
    var i = 0
    while (it.hasNext) {
      array(i) = it.next
      i += 1
    }
    new Buffer[V](array, array.length)
  }

  def fromIterable[@specialized V:Methods:ClassTag](iterable: Iterable[V]): Buffer[V] = {
    val array = classTag[V].newArray(iterable.size)
    val it = iterable.iterator
    var i = 0
    while (it.hasNext) {
      array(i) = it.next
      i += 1
    }
    new Buffer[V](array, array.length)
  }

}
