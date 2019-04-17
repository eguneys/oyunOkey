package oyun.common

import akka.actor._
import akka.event._

final class Bus(system: ActorSystem) extends Extension with EventBus {

  type Event = Bus.Event
  type Classifier = Symbol
  type Subscriber = Tellable

  def publish(payload: Any, channel: Classifier) {
    publish(Bus.Event(payload, channel))
  }

  /**
    * Attempts to register the subscriber to the specified channel
    * @return true if successfull and false if not (because it was already subscribed to that Classifier, or otherwise)
    */
  // def subscribe(subscriber: Subscriber, to: Classifier): Boolean = {
  //   // log(s"subscribe to $subscriber")
  //   bus.subscribe(subscriber, to)
  // }

  // def subscribe(subscriber: Subscriber, to: Classifier*): Boolean = {
  //   to foreach { subscribe(subscriber, _) }
  //   true
  // }

  def subscribe(subscriber: Tellable, to: Classifier): Boolean = {
    bus.subscribe(subscriber, to)
  }

  def subscribe(subscriber: Tellable, to: Classifier*): Boolean = {
    to foreach { subscribe(subscriber, _) }
    true
  }

  def subscribe(ref: ActorRef, to: Classifier): Boolean = subscribe(Tellable(ref), to)

  def subscribe(ref: ActorRef, to: Classifier*): Boolean = subscribe(Tellable(ref), to: _*)

  def subscribeFun(to: Classifier*)(f: PartialFunction[Any, Unit]): Tellable = {
    val t = oyun.common.Tellable(f)
    subscribe(t, to: _*)
    t
  }

  def subscribeFuns(subscriptions: (Classifier, PartialFunction[Any, Unit])*): Unit =
    subscriptions foreach {
      case (classifier, subscriber) => subscribeFun(classifier)(subscriber)
    }

  def unsubscribe(subscriber: Tellable, from: Classifier): Boolean = bus.unsubscribe(subscriber, from)
  def unsubscribe(ref: ActorRef, from: Classifier): Boolean = unsubscribe(Tellable(ref), from)

  def unsubscribe(subscriber: Tellable): Unit = bus unsubscribe subscriber

  /**
    * Attempts to deregister the subscriber from the specified Classifier
    * @return true if successfull and false if not (because it wasn't subscribed to that Classifier, or otherwise)
    */
  // def unsubscribe(subscriber: Subscriber, from: Classifier): Boolean = {
  //   // log(s"[UN]subscribe $from $subscriber")
  //   bus.unsubscribe(subscriber, from)
  // }

  /**
    * Attempts to deregister the subscriber from all Classifiers it may be subscribed to
    */
  // def unsubscribe(subscriber: Subscriber) {
  //   // log(s"[UN]subscribe ALL $subscriber")
  //   bus unsubscribe subscriber
  // }

  /**
    * Publishes the specified Event to this bus
    */
  def publish(event: Event) {
    // log(event.toString)
    bus publish event
  }

  private def log(msg: => String) {
    // loginfo(msg)
  }

  private val bus = new EventBus with LookupClassification {
    type Event = Bus.Event
    type Classifier = Symbol
    type Subscriber = Tellable

    override protected val mapSize = 2048

    protected def compareSubscribers(a: Tellable, b: Tellable) = a.uniqueId compareTo b.uniqueId

    def classify(event: Event): Symbol = event.channel

    def publish(event: Event, subscriber: Tellable) =
      subscriber != event.payload

    def publish(event: Event, subscriber: ActorRef) = {
      subscriber ! event.payload
    }
  }
}

object Bus extends ExtensionId[Bus] with ExtensionIdProvider {
  case class Event(payload: Any, channel: Symbol)

  override def lookup = Bus

  override def createExtension(system: ExtendedActorSystem) = new Bus(system)
}
