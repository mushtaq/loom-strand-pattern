package top.user

import java.util.concurrent.{CompletableFuture, ExecutorService}

class ExternalService(globalExecutor: ExecutorService) {
  // Demo IO call to an external service that takes a few millis to complete
  def ioCall(): CompletableFuture[Int] =
    CompletableFuture.supplyAsync(
      () =>
        Thread.sleep(10)
        99
      ,
      globalExecutor
    )
}
