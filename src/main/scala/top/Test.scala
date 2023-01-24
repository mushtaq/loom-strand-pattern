package top

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors}
import top.user.Acc
import top.user.External
import top.lib.Strand

import java.io.Closeable

object Test:
  @main def main: Unit =
    val globalExecutor = Executors.newVirtualThreadPerTaskExecutor()

    val external = External(globalExecutor)

    val acc     = Acc.create(Strand(globalExecutor), external)
    val safeAcc = Acc.createSafe(Strand(globalExecutor), external)

    val accResult     = test(acc, globalExecutor)     // some Acc updates are lost
    val safeAccResult = test(safeAcc, globalExecutor) // all the Acc updates are preserved

    println(s"accResult = $accResult")
    println(s"safeAccResult = $safeAccResult")

    globalExecutor.shutdown()

  private def test(acc: Acc with Closeable, globalExecutor: ExecutorService) =
    def update() = CompletableFuture.supplyAsync(() => acc.set(1), globalExecutor)

    val updateFutures = (1 to 1000).map(* => update())
    updateFutures.foreach(_.get())
    val result = acc.get()
    acc.close()
    result
