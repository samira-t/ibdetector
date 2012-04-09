package logger
import scala.collection.mutable.HashMap
import akka.actor.{ ActorRef, Actor, UntypedChannel }
import akka.event.{ EventHandler }
import akka.logger.LogActorRef

sealed abstract class Source
class ActorSource(val actorName: String) extends Source {
  override def toString = {
    actorName
  }
}
class CallerSource(val className: String, val methodName: String) extends Source {
  override def toString = {
    className + "::" + methodName
  }
}


object Logger {

  // possible to have a list of listeners in the future
  val defaultListener = Actor.actorOf[DefaultListener].start()

  sealed trait ActorEvent
  case class Started(actor: ActorRef) extends ActorEvent
  case class Stopped(actor: ActorRef) extends ActorEvent
  case class Sent(source: UntypedChannel, message: Any, destination: UntypedChannel) extends ActorEvent
  case class Forwarded(source: UntypedChannel, message: Any, destination: ActorRef) extends ActorEvent
  case class Received(message: Any, actor: ActorRef) extends ActorEvent

  def stopped(implicit actor: ActorRef) = defaultListener ! Stopped(actor)

  def started(implicit actor: ActorRef) = defaultListener ! Started(actor)

  def sent(sender: UntypedChannel, message: Any = "")(implicit actor: ActorRef, vc:HashMap[UntypedChannel,Int]) {
    defaultListener ! Sent(sender, message, actor)
  }

  def received(message: Any = "")(implicit actor: ActorRef, vc:HashMap[UntypedChannel,Int]){
    defaultListener ! Received(message, actor)
  }

  def forwarded(sender: UntypedChannel, message: Any = "")(implicit actor: ActorRef, vc:HashMap[UntypedChannel,Int]) {
    defaultListener ! Forwarded(sender, message, actor)
  }

  def printGraph() {
//    defaultListener ! Export
  }

  def shutdown() {
    defaultListener.stop()
  }

  class DefaultListener extends Actor {

    //private val actorGraph = new Graph
    private var vc = new HashMap[UntypedChannel, Int]()

    def receive = {

      case Started(actor) ⇒
        println("Started " + simpleName(actor))
        vc.+=(actor->0)

      // add the actor to the graph
      //actorGraph + ActorVertex(simpleName(actor))

      case Stopped(actor) =>
        vc.-=(actor)
        println("Stopped " + simpleName(actor))

      case Sent(sender, message, actor) =>
        val clock = vc.get(sender)

        
      case e: Forwarded ⇒ //processOutgoing(e)

      case Received(message, actor) ⇒
        println(simpleName(actor) + " received message " + simpleName(message))
        if (actor.isInstanceOf[LogActorRef]) println("vc = "+ actor.asInstanceOf[LogActorRef].getVC)

    }

/*    private def processOutgoing(event: ActorEvent) = {
      val (source, message, actor, isForward) = event match {
        case Sent(s, m, a) ⇒ (s, m, a, false)
        case Forwarded(s, m, a) ⇒ (s, m, a, true)
        case _ ⇒ throw new Exception("Unknown actor event in process outgoing")
      }
      println(source + (if (isForward) " forwarded " else " sent ") + "message " +
        simpleName(message) + " to " + simpleName(actor))

      var label = ""
      val destVertex = ActorVertex(simpleName(actor))
      val sourceVertex = source match {
        case c: CallerSource ⇒ label = c.methodName + "::"; ObjectVertex(c.className)
        case a: ActorSource ⇒ ActorVertex(a.actorName)
      }

      val edge = if (isForward)
        (sourceVertex --> destVertex)(label + simpleName(message))
      else
        (sourceVertex ~> destVertex)(label + simpleName(message))
      actorGraph + sourceVertex + destVertex + edge
    }
*/
    private def simpleName(instance: Any): String = {
      instance.asInstanceOf[AnyRef].getClass.getSimpleName
    }
  }
}