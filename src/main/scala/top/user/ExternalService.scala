package top.user

import top.lib.RichExecutor.async

import java.util.concurrent.{CompletableFuture, ExecutorService}

class ExternalService(globalExecutor: ExecutorService) {
  // Demo IO call to an external service that takes a few millis to complete
  def ioCall(): CompletableFuture[Int] =
    globalExecutor.async:
      Thread.sleep(10)
      99

}
