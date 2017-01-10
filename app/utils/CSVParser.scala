package utils

import shapeless._

import scala.collection.immutable.{:: => Cons}
import scala.util.{Failure, Success, Try}

/**
  * Created by malam on 1/5/17.
  */
class CSVException(s: String) extends RuntimeException

/** Trait for types that can be serialized to/deserialized from CSV */
trait CSVConverter[T] {
  def from(s: String): Try[T]
  def to(t: T): String
}

/** Instances of the CSVConverter trait */
object CSVConverter {
  def apply[T](implicit st: Lazy[CSVConverter[T]]): CSVConverter[T] = st.value

  def fail(s: String) = Failure(new CSVException(s))


  def countOccurrence(s: String): Int = {
    def count(s: String, acc: Int): Int = {
      s.indexOf("\"") match {
        case -1 => acc
        case x: Int => count(s.substring(x+1), acc + 1)
      }
    }
    count(s, 0)
  }

  // Primitives

  implicit def stringCSVConverter: CSVConverter[String] = new CSVConverter[String] {
    def from(s: String): Try[String] = Success(s)
    def to(s: String): String = s
  }

  implicit def intCsvConverter: CSVConverter[Int] = new CSVConverter[Int] {
    def from(s: String): Try[Int] = Try(s.trim.length match {
      case sz: Int if sz > 0 => s.toInt
      case _ => 0
    })
    def to(i: Int): String = i.toString
  }

  implicit def longCsvConverter: CSVConverter[Long] = new CSVConverter[Long] {
    def from(s: String): Try[Long] = Try(s.trim.length match {
      case sz: Int if sz > 0 => s.toLong
      case _ => 0
    })
    def to(i: Long): String = i.toString
  }

  implicit def doubleCsvConverter: CSVConverter[Double] = new CSVConverter[Double] {
    def from(s: String): Try[Double] = Try(s.trim.length match {
      case sz: Int if sz > 0 => s.toDouble
      case _ => 0
    })
    def to(i: Double): String = i.toString
  }

  implicit def boolCsvConverter: CSVConverter[Boolean] = new CSVConverter[Boolean] {
    def from(s: String): Try[Boolean] = Try(s match {
      case "1" => true
      case "0" => false
    })
    def to(i: Boolean): String = i.toString
  }

  def listCsvLinesConverter[A](l: List[String])(implicit ec: CSVConverter[A])
  : Try[List[A]] = l match {
    case Nil => Success(Nil)
    case Cons(s,ss) => for {
      x <- ec.from(s)
      xs <- listCsvLinesConverter(ss)(ec)
    } yield Cons(x, xs)
  }

  implicit def listCsvConverter[A](implicit ec: CSVConverter[A])
  : CSVConverter[List[A]] = new CSVConverter[List[A]] {
    def from(s: String): Try[List[A]] = listCsvLinesConverter(s.split("\n").toList)(ec)
    def to(l: List[A]): String = l.map(ec.to).mkString("\n")
  }


  // HList

  implicit def deriveHNil: CSVConverter[HNil] =
    new CSVConverter[HNil] {
      def from(s: String): Try[HNil] = s match {
        case "" => Success(HNil)
        case s => fail("Cannot convert '" ++ s ++ "' to HNil")
      }
      def to(n: HNil) = ""
    }

  implicit def deriveHCons[V, T <: HList]
  (implicit scv: Lazy[CSVConverter[V]], sct: Lazy[CSVConverter[T]])
  : CSVConverter[V :: T] =
    new CSVConverter[V :: T] {

      def from(s: String): Try[V :: T] = s.span(_ != ',') match {
        case (before,after) =>
          for {
            front <- scv.value.from(before)
            back <- sct.value.from(if (after.isEmpty) after else after.tail)
          } yield front :: back

        case _ => fail("Cannot convert '" ++ s ++ "' to HList")
      }

      def to(ft: V :: T): String = {
        scv.value.to(ft.head) ++ "," ++ sct.value.to(ft.tail)
      }
    }

  implicit def deriveHConsOption[V, T <: HList]
  (implicit scv: Lazy[CSVConverter[V]], sct: Lazy[CSVConverter[T]])
  : CSVConverter[Option[V] :: T] =
    new CSVConverter[Option[V] :: T] {

      def from(s: String): Try[Option[V] :: T] = s.span(_ != ',') match {
        case (before,after) =>
          (for {
            front <- scv.value.from(before)
            back <- sct.value.from(if (after.isEmpty) after else after.tail)
          } yield Some(front) :: back).orElse {
            sct.value.from(s).map(None :: _)
          }

        case _ => fail("Cannot convert '" ++ s ++ "' to HList")
      }

      def to(ft: Option[V] :: T): String = {
        ft.head.map(scv.value.to(_) ++ ",").getOrElse("") ++ sct.value.to(ft.tail)
      }
    }


  // Anything with a Generic

  implicit def deriveClass[A,R](implicit gen: Generic.Aux[A,R], conv: CSVConverter[R])
  : CSVConverter[A] = new CSVConverter[A] {

    def from(s: String): Try[A] = conv.from(s).map(gen.from)
    def to(a: A): String = conv.to(gen.to(a))
  }
}