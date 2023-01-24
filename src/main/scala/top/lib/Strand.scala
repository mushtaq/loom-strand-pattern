package top.lib

import java.io.Closeable
import java.util.concurrent.{CompletableFuture, ExecutorService, Executors}

class Strand(globalExecutor: ExecutorService) extends StrandContext with Closeable:
  private val strandExecutor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory())

  def execute[T](mutatingOp: => T): T =
    executeAsync(mutatingOp).get()

  def executeAsync[T](mutatingOp: => T): CompletableFuture[T] =
    CompletableFuture.supplyAsync(() => mutatingOp, strandExecutor)

  def blocking[T, R](blockingOp: => T)(onResult: T => R): CompletableFuture[R] =
    val executeOnGlobal = CompletableFuture.supplyAsync(() => blockingOp, globalExecutor)
    onComplete(executeOnGlobal)(onResult)

  def onComplete[T, R](future: CompletableFuture[T])(onResult: T => R): CompletableFuture[R] =
    future.thenCompose(x => executeAsync(onResult(x)))

  def close(): Unit = strandExecutor.shutdown()
