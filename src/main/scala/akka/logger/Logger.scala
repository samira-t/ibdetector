package akka.logger
import scala.collection.mutable.HashMap
import akka.actor.{ ActorRef, Actor, UntypedChannel }
import akka.event.{ EventHandler }
import scala.collection.mutable.ArrayBuffer



object Logger {

  // possible to have a list of listeners in the future
  //val defaultListener = Actor.actorOf[DefaultListener].start()
  
  val traceAnalyzer = Actor.actorOf[TraceAnalyzer].start()


  def stopped(implicit actor: ActorRef) = traceAnalyzer ! Stopped(actor)

  def started(implicit actor: ActorRef) = traceAnalyzer ! Started(actor)

  def sent(sender: UntypedChannel, message: Any)(implicit actor: UntypedChannel, vc: HashMap[ActorRef,Int]) {
    traceAnalyzer ! Sent(sender, message, actor, vc)
  }

  def received(sender:UntypedChannel, message: Any)(implicit actor: LogActorRef, vc:HashMap[ActorRef,Int]){
    traceAnalyzer ! Received(sender,message, actor, actor.getVectorClock)
  }
  
  def changedNullFields(receiver:LogActorRef, vc:HashMap[ActorRef,Int], changedFields:ArrayBuffer[String]){
    println("changed null", receiver, vc, changedFields.mkString(";"))
  }



  def shutdown() {
    traceAnalyzer.stop()
  }

//  class DefaultListener extends Actor {
//
//    //private val actorGraph = new Graph
//    private var vc = new HashMap[UntypedChannel, Int]()
//
////    def receive = {
////
////      case Started(actor) ⇒
////        println("Started " + simpleName(actor))
////        vc.+=(actor->0)
////
////      // add the actor to the graph
////      //actorGraph + ActorVertex(simpleName(actor))
////
////      case Stopped(actor) =>
////        vc.-=(actor)
////        println("Stopped " + simpleName(actor))
////
////      case Sent(message, actor) =>
////        val clock = vc.get(sender)
////
////        
////      case e: Forwarded ⇒ //processOutgoing(e)
////
////      case Received(message, actor) ⇒
////        println(simpleName(actor) + " received message " + simpleName(message))
////        if (actor.isInstanceOf[LogActorRef]) println("vc = "+ actor.asInstanceOf[LogActorRef].getVC)
////
////    }

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
//    private def simpleName(instance: Any): String = {
//      instance.asInstanceOf[AnyRef].getClass.getSimpleName
//    }
//  }
}