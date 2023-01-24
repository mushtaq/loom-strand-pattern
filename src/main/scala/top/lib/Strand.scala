package top.lib

import java.io.Closeable
import java.util.concurrent.{CompletableFuture, ExecutorService, Executors}

class Strand(globalExecutor: ExecutorService) extends StrandContext with Closeable:
  // Single threaded executor but used a virtual thread
  // Hence, theoretically any number of Strands can be created in user space
  private val strandExecutor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory())

  // This is used to wrap the user code so that it all executes on a single virtual thread
  def execute[T](mutatingOp: => T): T =
    executeAsync(mutatingOp).get()

  private def executeAsync[T](mutatingOp: => T): CompletableFuture[T] =
    CompletableFuture.supplyAsync(() => mutatingOp, strandExecutor)

  def blocking[T, R](blockingOp: => T)(onResult: T => R): CompletableFuture[R] =
    val executeOnGlobal = CompletableFuture.supplyAsync(() => blockingOp, globalExecutor)
    onComplete(executeOnGlobal)(onResult)

  def onComplete[T, R](future: CompletableFuture[T])(onResult: T => R): CompletableFuture[R] =
    future.thenCompose(x => executeAsync(onResult(x)))

  def close(): Unit = strandExecutor.shutdown()
