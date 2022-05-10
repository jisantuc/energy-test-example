package io.github.jisantuc.energybench

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.traverse._
import io.github.timwspence.cats.stm.STM
import org.scalacheck.Prop
import org.scalacheck.Properties
import org.scalacheck.Test

class STMSpec extends Properties("stm") {
  property("sum ints") = Prop.forAll(Generators.genPairs) {
    (pairs: List[(String, Int)]) =>
      (
        for {
          stm <- STM.runtime[IO]
          kvStore <- KvStore.forStm[IO, String, Int](stm, None)
          ints <- stm.commit(
            kvStore.setMany(pairs) >> pairs.flatTraverse { case (k, _) =>
              kvStore.getKey(k).map(_.toList)
            }
          )
        } yield {
          ints
        }
      ).attempt
        .map {
          case Left(_) => true
          case Right(ints) =>
            ints.sum == pairs.foldLeft(0)((acc, pair) => acc + pair._2)
        }
        .unsafeRunSync()

  }
}
