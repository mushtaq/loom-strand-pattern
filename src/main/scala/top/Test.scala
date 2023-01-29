package top

import top.lib.RichExecutor.async

import java.util.concurrent.{CompletableFuture, ExecutorService, Executors}
import top.user.Account
import top.user.ExternalService
import top.lib.Strand

import java.io.Closeable

object Test:
  @main def main: Unit =
    val globalExecutor = Executors.newVirtualThreadPerTaskExecutor()

    val externalService = ExternalService(globalExecutor)

    val account     = Account.create(Strand(globalExecutor), externalService)
    val safeAccount = Account.createSafe(Strand(globalExecutor), externalService)

    val accResult     = test(account, globalExecutor)     // some Acc updates are lost
    val safeAccResult = test(safeAccount, globalExecutor) // all the Acc updates are preserved

    println(s"accResult = $accResult")
    println(s"safeAccResult = $safeAccResult")

    globalExecutor.shutdown()

  private def test(acc: Account with Closeable, globalExecutor: ExecutorService) =
    // Asynchronously increments the balance by 1
    def update() = globalExecutor.async(acc.set(1))

    // Large number of concurrent updates
    val updateFutures: Seq[CompletableFuture[Unit]] = (1 to 1000).map(* => update())

    // Wait for all updates to finish
    updateFutures.foreach(_.get())

    // Read the current balance
    val result = acc.get()

    acc.close()
    result
