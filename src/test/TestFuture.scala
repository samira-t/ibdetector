///**
// * Copyright (C) 2011 Samira Tasharofi
// */
//package akka.logger
//import akka.actor.Actor
//import akka.actor.ActorRef
//
//class SampleActor(var brother: ActorRef = null) extends Actor {
//  var messageOrder = ListBuffer[Any]()
//  def receive = {
//    case msg @ ('m)  ⇒ messageOrder.+=(msg)
//    case msg @ 'req  ⇒ messageOrder.+=(msg); if (brother != null) { val f = brother ? 'req2; f.get }
//    case msg @ 'req2 ⇒ messageOrder.+=(msg); self.reply('reply)
//
//  }
//}
//
//
//
//object TestSmaple extends ScalaTest{
//  
//
//    val a = actorOf(new SampleActor()).start
//    val b = actorOf(new SampleActor(a)).start
//    b ! 'req
//    
//
//  }
//}