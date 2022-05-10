package io.github.jisantuc.energybench

import cats.MonadError
import cats.effect.Concurrent
import cats.effect.Sync
import cats.syntax.functor._
import cats.syntax.traverse._
import io.github.timwspence.cats.stm.STM

import scala.collection.mutable.{Map => MutableMap}
import scala.util.Random

trait KvStore[F[_], K, V] {
  def setKey(key: K, value: V): F[Unit]
  def setMany(pairs: List[(K, V)]): F[Unit]
  def getKey(key: K): F[Option[V]]
}

object KvStore {
  def yolo[F[_]: Sync, K, V](failureThreshold: Option[Float] = Some(0.999f)) = {
    def maybeThrow[A](threshold: Option[Float])(txn: F[A]): F[A] = if (
      Random.nextFloat() >= threshold.getOrElse(Float.PositiveInfinity)
    ) {
      MonadError[F, Throwable].raiseError(new Exception("bad luck"))
    } else {
      txn
    }

    new KvStore[F, K, V] {
      private val underlying: MutableMap[K, V] = MutableMap.empty
      def setKey(key: K, value: V): F[Unit] = maybeThrow(failureThreshold) {
        Sync[F].delay {
          underlying += ((key, value))
        }
      }
      def setMany(pairs: List[(K, V)]): F[Unit] = maybeThrow(failureThreshold) {
        pairs.traverse { case (k, v) =>
          setKey(k, v)
        }.void
      }

      def getKey(key: K): F[Option[V]] = maybeThrow(failureThreshold) {
        Sync[F].delay {
          underlying.get(key)
        }
      }
    }
  }

  def forStm[F[_]: Concurrent, K, V](
      stm: STM[F],
      failureThreshold: Option[Float] = Some(0.999f)
  ): F[KvStore[stm.Txn, K, V]] = {

    import stm._

    def maybeThrow[A](threshold: Option[Float])(txn: Txn[A]): Txn[A] = if (
      Random.nextFloat() >= threshold.getOrElse(Float.PositiveInfinity)
    ) {
      stm.abort(new Exception("bad luck"))
    } else {
      txn
    }

    stm.commit(TVar.of(MutableMap.empty[K, V])) map { underlying =>
      new KvStore[stm.Txn, K, V] {
        import stm._

        def setKey(key: K, value: V): Txn[Unit] = maybeThrow(failureThreshold) {
          // Unsafe randomness because STM can't perform effects
          for {
            curr <- underlying.get
            _ = curr += ((key, value))
            _ <- underlying.set(curr)
          } yield ()
        }

        def setMany(pairs: List[(K, V)]): Txn[Unit] =
          maybeThrow(failureThreshold) {
            pairs.traverse { case (k, v) =>
              setKey(k, v)
            }.void
          }

        def getKey(key: K): Txn[Option[V]] =
          maybeThrow(failureThreshold) {
            underlying.get.map(_.get(key))
          }

      }
    }

  }
}
