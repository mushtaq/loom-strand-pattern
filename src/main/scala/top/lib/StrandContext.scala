package top.lib

import java.util.concurrent.{CompletableFuture, ExecutorService}

// Limited API of the Strand class to be used by the end user
trait StrandContext:
  def blocking[T, R](blockingOp: => T)(onResult: T => R): CompletableFuture[R]
  def onComplete[T, R](future: CompletableFuture[T])(onResult: T => R): CompletableFuture[R]
