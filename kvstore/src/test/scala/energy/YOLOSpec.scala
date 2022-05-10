package io.github.jisantuc.energybench

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.flatMap._
import cats.syntax.traverse._
import org.scalacheck.Prop
import org.scalacheck.Properties

class YOLOSpec extends Properties("yolo") {
  property("sum ints") = Prop.forAll(Generators.genPairs) {
    (pairs: List[(String, Int)]) =>
      val kvStore = KvStore.yolo[IO, String, Int](None)
      (kvStore.setMany(pairs) >> pairs.flatTraverse { case (k, _) =>
        kvStore.getKey(k).map(_.toList)
      }).attempt
        .map {
          case Left(_)     => true
          case Right(ints) =>
            // because the same string can be generated in the keys, we don't
            // know anything for certain about the sum other than that it should be
            // greater than 0 (based on the int generator)
            // but also, we don't really care about the sum, just that we do some work
            ints.sum > 0
        }
        .unsafeRunSync()

  }

}
