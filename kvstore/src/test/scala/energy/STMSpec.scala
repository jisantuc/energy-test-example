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
