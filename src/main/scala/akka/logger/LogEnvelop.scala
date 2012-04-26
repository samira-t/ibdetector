package akka.logger

import akka.actor.UntypedChannel
import scala.collection.mutable.HashMap
import akka.actor.ActorRef

sealed trait LogEventType
case object send extends LogEventType
case object receive extends LogEventType

case class LogEvent(eventType:LogEventType, sender:UntypedChannel, message: Any, receiver:UntypedChannel, vc: HashMap[ActorRef,Int])
case class LogMessage(message: Any, vc: HashMap[ActorRef,Int])