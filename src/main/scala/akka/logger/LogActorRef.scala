package akka.logger
import scala.collection.mutable.HashMap

import java.net.InetSocketAddress
import akka.dispatch.{ MessageInvocation, ActorCompletableFuture }
import logger.{ CallerSource, ActorSource, Source, Logger }
import akka.actor._

class LogActorRef(private[this] val actorFactory: () ⇒ Actor,
                  val _homeAddress: Option[InetSocketAddress]) extends LocalActorRef(actorFactory, _homeAddress) {

  private implicit def unnderlyingActor = this.actor
  private implicit var vc = new HashMap[UntypedChannel, Int]()
  private implicit def ref = this
  private var isForward = false

  override def start(): this.type = {
    val ret = super.start()
    Logger.started
    ret
  }

  override def stop() {
    Logger.stopped
  }

  override def forward(message: Any)(implicit channel: ForwardableChannel) = {
    isForward = true
    super.forward(message)(channel)
    Logger.forwarded(channel, message)
    isForward = false
  }

  def !(message: Any)(implicit channel: UntypedChannel, actor: LogActorRef): Unit = {
    if (actor != null) {
      val vc = actor.vc
      actor.incVC();
      super.!(MessageWvc(message, vc))(channel)
    } else {
      super.!(message, vc)(channel)
    }
  }

  def ?(message: Any)(implicit channel: UntypedChannel, timeout: Actor.Timeout, actor: LogActorRef): ActorCompletableFuture = {
    if (actor != null) {
      super.?(MessageWvc(message, actor.vc))(channel, timeout)
    } else super.?(message)(channel, timeout)
  }

  override def postMessageToMailbox(message: Any, channel: UntypedChannel): Unit = {
    super.postMessageToMailbox(message, channel)
    if (!isForward) Logger.sent(channel, message)
  }

  override def postMessageToMailboxAndCreateFutureResultWithTimeout(
    message: Any,
    timeout: Long,
    channel: UntypedChannel): ActorCompletableFuture = {
    val ret = super.postMessageToMailboxAndCreateFutureResultWithTimeout(message, timeout, channel)
    if (!isForward) Logger.sent(channel, message)
    ret
  }

  override def invoke(messageHandle: MessageInvocation): Unit = {
    try {
      var msgvc = messageHandle.message.asInstanceOf[MessageWvc]
      var newVc = msgvc.vc

      var actorObject = this.actor
      actorObject.getClass().getFields()
      updateVC(newVc)
      val newHandle = new MessageInvocation(messageHandle.receiver, msgvc.message, messageHandle.channel)
      super.invoke(newHandle)
    } finally {
      Logger.received(messageHandle.message)
    }
  }

  def getVC = vc

  private def updateVC(newVc: HashMap[UntypedChannel, Int]) = {

  }

  def incVC() {

  }

  private def callStack = try { sys.error("exception") } catch { case ex ⇒ ex.getStackTrace drop 2 }

  private def senderOrCaller(channel: UntypedChannel): Source = {
    val ret = channel match {
      case ref: ActorRef ⇒ new ActorSource(ref.actor.getClass.getSimpleName)

      case _ ⇒
        // dig through the call stack to find the class
        val se = callStack(4) //skipping senderOrCaller, postMesssageTo*
        new CallerSource(stripPackageName(se.getClassName), se.getMethodName)
    }
    ret
  }

  private def stripPackageName(className: String): String = {
    className.substring(className.lastIndexOf(".") + 1)
  }
}