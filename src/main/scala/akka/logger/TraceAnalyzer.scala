package akka.logger

import akka.actor._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

  sealed trait ActorEvent
  case class Started(actor: ActorRef) extends ActorEvent
  case class Stopped(actor: ActorRef) extends ActorEvent
  case class Sent(source:UntypedChannel, message: Any, destination: UntypedChannel,vc:HashMap[ActorRef,Int]) extends ActorEvent
  case class Forwarded(source: UntypedChannel, message: Any, destination: ActorRef, vc:HashMap[ActorRef,Int]) extends ActorEvent
  case class Received(source:UntypedChannel,message: Any, actor: LogActorRef, vc: HashMap[ActorRef,Int]) extends ActorEvent
 

class TraceAnalyzer extends Actor{
  
  var trace = new ListBuffer[LogEvent]
  
    def receive = {

      case Started(actor) ⇒
        println("Started " + actor)
        //vc.+=(actor->0)

      // add the actor to the graph
      //actorGraph + ActorVertex(simpleName(actor))

      case Stopped(actor) =>
        //vc.-=(actor)
        println("Stopped " + actor)

      case Sent(source, message, destination, vc) =>
        println("sent", source, message, destination, vc)
        //val clock = vc.get(sender)

        
      case e: Forwarded ⇒ //processOutgoing(e)

      case Received(source,message, destination, vc) ⇒
      var currentSender = self.sender
        println("received", source, message, destination, vc)
        //println(simpleName(actor) + " received message " + simpleName(message))
        //if (destination.isInstanceOf[LogActorRef]) println("vc = "+ destination.asInstanceOf[LogActorRef].vc
        //if (currentSender.isInstanceOf[LogActorCompletableFuture])  currentSender = 
          //currentSender.asInstanceOf[LogActorCompletableFuture].parent

    }

}