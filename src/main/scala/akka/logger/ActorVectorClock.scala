package akka.logger

import java.util.concurrent.ConcurrentHashMap
import akka.actor.ActorRef
import scala.collection.mutable.HashMap

class ActorVectorClock(implicit logActorRef: LogActorRef) {

  @volatile
  var vc = new ConcurrentHashMap[ActorRef, Int]()
  vc.put(logActorRef,0)

  def increment = synchronized {
    val newValue = vc.get(logActorRef)
    //if (newValue != -2)
    vc.replace(logActorRef, newValue + 1)
    println(newValue+1)
  }

  def update(newVC: HashMap[ActorRef,Int]) =  synchronized{

    for (actor <- newVC.keySet) {
         val value = newVC.get(actor).get
     if (vc.containsKey(actor)) {
        if (vc.get(actor)< value) {
      
        vc.replace(actor,value)
        }

      } else {
        vc.put(actor, value)

      }
    }
  }
  
  def getVector():HashMap[ActorRef,Int] = synchronized {
    var copy = HashMap[ActorRef,Int]()
    var iterator = vc.entrySet().iterator()
    while (iterator.hasNext() ){
      var entry = iterator.next()
      copy.+=((entry.getKey(),entry.getValue()))
      
    }
    return copy
    
    
  }

  override def toString(): String = {
    var result = ""
    var itr = vc.keys()
    while (itr.hasMoreElements()) {
      val actor = itr.nextElement()
      result += vc.get(actor) + ","
    }
    result += "\n"
    return result
  }
}