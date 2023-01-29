package top.lib

import top.lib.RichExecutor.async

import java.io.Closeable
import java.util.concurrent.{CompletableFuture, ExecutorService, Executors, ThreadFactory}

class Strand(globalExecutor: ExecutorService) extends StrandContext with Closeable:
  // Single threaded executor but used a virtual thread
  // Hence, theoretically any number of Strands can be created in user space
  private val strandExecutor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory())

  // This is used to wrap the user code so that it all executes on a single virtual thread
  def execute[T](mutatingOp: => T): T =
    strandExecutor.async(mutatingOp).get()

  def onComplete[T, R](future: CompletableFuture[T])(onResult: T => R): CompletableFuture[R] =
    blocking(future.get()): x =>
      onResult(x)

  def blocking[T, R](blockingOp: => T)(onResult: T => R): CompletableFuture[R] =
    globalExecutor.async:
      val result = blockingOp // block in the globalExecutor
      val transformResult =
        strandExecutor.async:
          onResult(result) // callback on the result in the strandExecutor
      transformResult.get()

  def close(): Unit = strandExecutor.shutdown()
