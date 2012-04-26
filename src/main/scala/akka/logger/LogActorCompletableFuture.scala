package akka.logger
import akka.dispatch.ActorCompletableFuture
import akka.actor.UntypedChannel
import akka.logger._
import akka.dispatch.CompletableFuture
import java.util.concurrent.TimeUnit.{ NANOSECONDS ⇒ NANOS, MILLISECONDS ⇒ MILLIS }
import java.util.concurrent.TimeUnit
import akka.dispatch.MessageDispatcher


class LogActorCompletableFuture(timeout: Long, timeunit: TimeUnit)(implicit dispatcher: MessageDispatcher, parent:UntypedChannel) 
extends ActorCompletableFuture(timeout,timeunit)(dispatcher){
  def this(timeout: Long)(implicit dispatcher: MessageDispatcher, parent:UntypedChannel) = this(timeout, MILLIS)
  override def !(message: Any)(implicit channel: UntypedChannel) = {
    if (channel.isInstanceOf[LogActorRef]){
      channel.asInstanceOf[LogActorRef].incVC()
      var senderVc = channel.asInstanceOf[LogActorRef].getVectorClock
      Logger.sent(channel,message)(parent,senderVc)
      if (parent.isInstanceOf[LogActorRef]){
      parent.asInstanceOf[LogActorRef].updateVC(senderVc)
      parent.asInstanceOf[LogActorRef].incVC()
      Logger.received(channel,message)(parent.asInstanceOf[LogActorRef],parent.asInstanceOf[LogActorRef].getVectorClock)
      }
    }
    super.!(message)(channel)
  }
  
  
  
}

object LogActorCompletableFuture {
  def apply(f: CompletableFuture[Any])(implicit parent:UntypedChannel): LogActorCompletableFuture =
    new LogActorCompletableFuture(f.timeoutInNanos, NANOS)(f.dispatcher, parent) {
      completeWith(f)
      override def !(message: Any)(implicit channel: UntypedChannel) = {
    if (channel.isInstanceOf[LogActorRef]){
      channel.asInstanceOf[LogActorRef].incVC()
      var senderVc = channel.asInstanceOf[LogActorRef].getVectorClock
      Logger.sent(channel,message)(parent,senderVc)
      if (parent.isInstanceOf[LogActorRef]){
      parent.asInstanceOf[LogActorRef].updateVC(senderVc)
      parent.asInstanceOf[LogActorRef].incVC()
      Logger.received(channel,message)(parent.asInstanceOf[LogActorRef],parent.asInstanceOf[LogActorRef].getVectorClock)
      }
    }
        f completeWithResult message
      }
      override def sendException(ex: Throwable) = {
        f completeWithException ex
        f.value == Some(Left(ex))
      }
    }
}
