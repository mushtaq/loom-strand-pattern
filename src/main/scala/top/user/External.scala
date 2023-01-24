package top.user

import java.util.concurrent.{CompletableFuture, ExecutorService}

class External(globalExecutor: ExecutorService) {
  def serviceCall(): CompletableFuture[Int] =
    CompletableFuture.supplyAsync(() =>
      Thread.sleep(10)
      99
    )
}
