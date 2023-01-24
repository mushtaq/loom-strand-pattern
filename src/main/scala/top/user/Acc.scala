package top.user

import top.lib.{Strand, StrandContext}

import java.io.Closeable

// Acc class is implemented assuming all methods will always be scheduled on a single thread
// Let's call this thread 'the Strand'
class Acc(context: StrandContext, external: External):
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

    context.blocking(Thread.sleep(100)) { x =>
      // Asynchronous operation using the result of a blocking call
      // User can freely mutate the shared vars here
      // All these mutations will be take place on 'the Strand'
    }

    context.onComplete(external.serviceCall()) { x =>
      // Asynchronous operation using the result of a async call
      // User can freely mutate the shared state here
      // All these mutations will be take place on 'the Strand'
    }

  def computeInterest(): Double =
    balance * interestRate

  def getBalanceWithInterest(): Double =
    totalTx += 1
    balance + computeInterest()

object Acc:
  def create(strand: Strand, external: External) = new Acc(strand, external) with Closeable:
    def close(): Unit = strand.close()

  // This factory uses mechanical transformation of the user class Acc
  // Hence, this can be automated using a macro annotation on the Acc class
  def createSafe(strand: Strand, external: External) = new Acc(strand, external) with Closeable:
    override def get(): Int                       = strand.execute(super.get())
    override def set(x: Int): Unit                = strand.execute(super.set(x))
    override def computeInterest(): Double        = strand.execute(super.computeInterest())
    override def getBalanceWithInterest(): Double = strand.execute(super.getBalanceWithInterest())

    def close(): Unit = strand.close()
