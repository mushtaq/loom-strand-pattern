package top

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors}
import top.user.Acc
import top.user.ExternalService
import top.lib.Strand

import java.io.Closeable

object Test:
  @main def main: Unit =
    val globalExecutor = Executors.newVirtualThreadPerTaskExecutor()

    val externalService = ExternalService(globalExecutor)

    val acc     = Acc.create(Strand(globalExecutor), externalService)
    val safeAcc = Acc.createSafe(Strand(globalExecutor), externalService)

    val accResult     = test(acc, globalExecutor)     // some Acc updates are lost
    val safeAccResult = test(safeAcc, globalExecutor) // all the Acc updates are preserved

    println(s"accResult = $accResult")
    println(s"safeAccResult = $safeAccResult")

    globalExecutor.shutdown()

  private def test(acc: Acc with Closeable, globalExecutor: ExecutorService) =
    // Asynchronously increments the balance by 1
    def update() = CompletableFuture.supplyAsync(() => acc.set(1), globalExecutor)

    // Large number of concurrent updates
    val updateFutures = (1 to 1000).map(* => update())

    // Wait for all updates to finish
    updateFutures.foreach(_.get())

    // Read the current balance
    val result = acc.get()

    acc.close()
    result
