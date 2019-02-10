package scalest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import distage._

//This should also configure distage config parsing
abstract class ScalestApp(appName: String = "scalest-app")
  extends HttpApp
  with App {
  def routes: Route

  val injector: Injector = Injector()

  lazy val system = ActorSystem(appName)

  val akkaModule: ModuleDef = new ModuleDef {
    make[ActorSystem].from(system)
  }

  override def main(args: Array[String]): Unit = {
    super.main(args)
    startServer("0.0.0.0", 9000, system)
  }
}