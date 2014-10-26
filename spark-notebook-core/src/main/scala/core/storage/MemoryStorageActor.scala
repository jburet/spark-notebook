package core.storage

import akka.actor.{Props, Actor}

object MemoryStorageActor {
  def props(id: String) = Props(new MemoryStorageActor(id))
}


class MemoryStorageActor(val id: String) extends Actor {
  def receive = ???
}
