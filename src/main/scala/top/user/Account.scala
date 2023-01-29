package top.user

import top.lib.{Strand, StrandContext}

import java.io.Closeable

// All methods can be scheduled on a single thread ('the Strand')
// Direct mutating operation on the shared state is allowed
// Blocking calls and Future results must be handled via the 'StrandContext'
class Account(context: StrandContext, externalService: ExternalService):
  private var balance = 0
  private var totalTx = 0

  private val interestRate = 0.10

  def get(): Int =
    totalTx += 1
    balance

  def set(x: Int): Unit =
    // User can freely mutate the shared variables because all ops are scheduled on 'the Strand'
    totalTx += 1
    balance += x

    context.blocking(Thread.sleep(100)): x =>
      // Asynchronous operation using the result of a blocking call
      // User can freely mutate the shared vars here
      // All these mutations will be take place on 'the Strand'
      ()

    context.onComplete(externalService.ioCall()): x =>
      // Asynchronous operation using the result of a async call
      // User can freely mutate the shared state here
      // All these mutations will be take place on 'the Strand'
      ()

  def computeInterest(): Double =
    balance * interestRate

  def getBalanceWithInterest(): Double =
    totalTx += 1
    balance + computeInterest()

object Account:
  // Unsafe factory, just for the demo purpose
  // Updates will be lost, Do Not Use!
  def create(strand: Strand, externalService: ExternalService) =
    new Account(strand, externalService) with Closeable:
      def close(): Unit = strand.close()

  // Safe factory that does mechanical transformations before creating an instance
  // This can be automated using a macro annotation on the Acc class
  def createSafe(strand: Strand, externalService: ExternalService) =
    new Account(strand, externalService) with Closeable:
      override def get(): Int                       = strand.execute(super.get())
      override def set(x: Int): Unit                = strand.execute(super.set(x))
      override def computeInterest(): Double        = strand.execute(super.computeInterest())
      override def getBalanceWithInterest(): Double = strand.execute(super.getBalanceWithInterest())

      def close(): Unit = strand.close()
