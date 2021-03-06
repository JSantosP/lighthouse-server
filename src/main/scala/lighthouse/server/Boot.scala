package lighthouse.server

import scala.util.Try
import akka.actor.ActorSystem
import akka.io.IO
import com.typesafe.config.{Config,ConfigFactory}
import spray.can.Http
import util.Properties

object Boot extends {
  override val config = ConfigFactory.load("server.conf")
} with App
  with LightHouse
  with Configuration {

  implicit val system = ActorSystem("lighthouse")

  lazy val service = system.actorOf(Service(resources))

  IO(Http) ! Http.Bind(service, interface = bindingIp, port = bindingPort)

}

trait LightHouse {
  _: Configuration =>

  type Resource = String
  type Path = String

  lazy val resources: Map[Resource, Path] = resourceList.map(_ -> "/").toMap

}

trait Configuration {

  import scala.collection.JavaConversions._

  val config: Config

  val bindingIp = config.getString("app.server.bind.ip")
  val bindingPort = Try(Option(System.getProperty("http.port")))
    .map(_.get).recover {
      case _: Throwable => config.getString("app.server.bind.port")
    }.get.toInt
  val resourceList = config.getStringList("app.server.resources").toList

}

