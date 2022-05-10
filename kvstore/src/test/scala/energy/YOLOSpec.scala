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
          case Left(_) => true
          case Right(ints) =>
            ints.sum == pairs.foldLeft(0)((acc, pair) => acc + pair._2)
        }
        .unsafeRunSync()

  }

}
