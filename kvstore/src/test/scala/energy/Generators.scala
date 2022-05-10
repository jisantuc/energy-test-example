package io.github.jisantuc.energybench

import org.scalacheck.Gen

object Generators {
  val genPair: Gen[(String, Int)] = for {
    s <- Gen.alphaStr
    d <- Gen.choose(0, 100)
  } yield (s, d)

  val genPairs: Gen[List[(String, Int)]] = for {
    nPairs <- Gen.choose(1, 1000)
    pairs <- Gen.listOfN(nPairs, genPair)
  } yield pairs
}
