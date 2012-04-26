package akka.logger
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import java.lang.reflect.Field
import java.net.InetSocketAddress
import akka.dispatch.{ MessageInvocation, ActorCompletableFuture }
import akka.actor._

class LogActorRef(private[this] val actorFactory: () ⇒ Actor,
                  val _homeAddress: Option[InetSocketAddress]) extends LocalActorRef(actorFactory, _homeAddress) {

  private implicit def unnderlyingActor = this.actor
  private implicit val logActorRef = this
  @volatile
  private var actorVC = new ActorVectorClock

  override def start(): this.type = {
    val ret = super.start()
    Logger.started
    ret
  }

  override def stop() {
    Logger.stopped
  }

  override def postMessageToMailbox(message: Any, channel: UntypedChannel): Unit = {
    var senderVC: HashMap[ActorRef,Int] = null
    if (channel.isInstanceOf[LogActorRef]) {
      channel.asInstanceOf[LogActorRef].incVC()
      senderVC = channel.asInstanceOf[LogActorRef].getVectorClock
    }
    super.postMessageToMailbox(LogMessage(message,senderVC), channel)
    Logger.sent(channel, message)(this,senderVC)
  }

  override def postMessageToMailboxAndCreateFutureResultWithTimeout(
    message: Any,
    timeout: Long,
    channel: UntypedChannel): ActorCompletableFuture = {

    if (isClientManaged_?) {
      val chSender = channel match {
        case ref: ActorRef ⇒ Some(ref)
        case _             ⇒ None
      }
      val chFuture = channel match {
        case f: ActorCompletableFuture ⇒ Some(f)
        case _                         ⇒ None
      }
      val future = Actor.remote.send[Any](message, chSender, chFuture, homeAddress.get, timeout, false, this, None, ActorType.ScalaActor, None)
      if (future.isDefined) LogActorCompletableFuture(future.get)
      else throw new IllegalActorStateException("Expected a future from remote call to actor " + toString)
    } else {
      val future = channel match {
        case f: LogActorCompletableFuture ⇒ f
        case _                         ⇒ new LogActorCompletableFuture(timeout)(dispatcher, channel)
      }
      var senderVC : HashMap[ActorRef,Int] = null
      if (channel.isInstanceOf[LogActorRef]) {
        channel.asInstanceOf[LogActorRef].incVC()
        senderVC = channel.asInstanceOf[LogActorRef].getVectorClock
        } 
      dispatcher dispatchMessage new MessageInvocation(this, LogMessage(message,senderVC), future)
      Logger.sent(channel,message)(channel,senderVC)
      future
  }
  }

  override def invoke(messageHandle: MessageInvocation): Unit = {
      var realHandle = messageHandle
      if (messageHandle.message.isInstanceOf[LogMessage]) 
        realHandle = MessageInvocation(messageHandle.receiver,messageHandle.message.asInstanceOf[LogMessage].message,messageHandle.channel)
      var msgvc = messageHandle.message.asInstanceOf[LogMessage].vc
      var actorObject = this.actor
      var fields = actorObject.getClass().getDeclaredFields()
      var nullFields = new ArrayBuffer[Field]()
      var changedNullFields = false
      for (field <- fields){
        field.setAccessible(true)
        if (field.get(actorObject) == null) {
          nullFields.+=:(field)
          //println("null",field.getName)
        }
        //else println(field,field.get(actorObject))
      }
      updateVC(msgvc)
      incVC()
      val updatedVC = getVectorClock
      Logger.received(realHandle.channel,realHandle.message)(this,updatedVC)
      super.invoke(realHandle)
      var notNullFields = new ArrayBuffer[String]()
      for (field <- nullFields ){
        field.setAccessible(true)
        if (field.get(actorObject) != null) {
          //println(" not null",field.getName)
          changedNullFields = true
          notNullFields.+=(field.getName())
        }
        //else println(field,field.get(actorObject))
      }
        if (notNullFields.size>0){
          println("not size"+notNullFields.size)
          Logger.changedNullFields(this,updatedVC,notNullFields)
        }
  }

  def getVectorClock = actorVC.getVector()

  def updateVC(newVC: HashMap[ActorRef,Int]) = {
    if (newVC != null)
	  actorVC.update(newVC)
  }

  def incVC() {
    actorVC.increment

  }
}