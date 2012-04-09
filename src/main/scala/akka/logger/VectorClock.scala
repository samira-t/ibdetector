package akka.logger

import java.util.concurrent.ConcurrentHashMap
import akka.actor.ActorRef

class VectorClock(actor: ActorRef) {

  var vc = new ConcurrentHashMap[ActorRef, Int]()

  def increment(value: Int) {
    val newValue = vc.get(actor)
    //if (newValue != -2)
    vc.replace(actor, newValue + 1)
  }

  def updateAndIncerement(newVc: VectorClock) {

    for (actor <- newVc.vc.keySet().toArray()) {
      if (vc.containsKey(actor)) {

      } else {

      }
    }
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