package akka.logger

import akka.actor.UntypedChannel
import scala.collection.mutable.HashMap

case class MessageWvc(message: Any, vc: HashMap[UntypedChannel, Int])