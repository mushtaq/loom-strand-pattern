package top.lib

import java.util.concurrent.{CompletableFuture, ExecutorService}

trait StrandContext:
  def blocking[T, R](blockingOp: => T)(onResult: T => R): CompletableFuture[R]
  def onComplete[T, R](future: CompletableFuture[T])(onResult: T => R): CompletableFuture[R]
