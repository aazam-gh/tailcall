package tailcall.gateway.remote.operations

import tailcall.gateway.lambda.Lambda
import tailcall.gateway.remote.Remote
import zio.schema.Schema

trait OptionOps {
  implicit final class RemoteOptionOps[A](private val self: Remote[Option[A]]) {
    def isSome: Remote[Boolean] = Remote(self.toLambda >>> Lambda.option.isSome)

    def isNone: Remote[Boolean] = Remote(self.toLambda >>> Lambda.option.isNone)

    def fold[B](ifNone: Remote[B], ifSome: Remote[A] => Remote[B]): Remote[B] =
      Remote(Lambda.option.fold[Any, A, B](self.toLambda, ifNone.toLambda, Remote.fromRemoteFunction(ifSome)))

    def getOrElse[B >: A](default: Remote[B]): Remote[B] =
      Remote(Lambda.option.fold(self.toLambda, default.toLambda, Remote.fromRemoteFunction[B, B](i => i)))

    def getOrDie: Remote[A] = getOrElse(Remote.die("Failed to get value from None"))

    def flatMap[B](f: Remote[A] => Remote[Option[B]])(implicit ev: Schema[B]): Remote[Option[B]] =
      self.fold[Option[B]](Remote(Option.empty[B]), f(_))
  }
}
