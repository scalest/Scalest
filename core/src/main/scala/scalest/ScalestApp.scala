package scalest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import com.typesafe.config.Config
import distage._

//This should also configure distage config parsing
abstract class ScalestApp(appName: String = "scalest-app", modules: List[Module] = List.empty)
  extends HttpApp {
  def routes: Route

  implicit lazy val system: ActorSystem = ActorSystem(appName)
  implicit lazy val config: Config = system.settings.config

  val akkaModule: ModuleDef = new ModuleDef {
    make[ActorSystem].from(system)
  }

  val injector: Injector = Injector()
  val appModule: Module = modules.reduce(_ ++ _) ++ akkaModule
  val appPlan: OrderedPlan = injector.plan(appModule)

  val locator: Locator = injector.produceUnsafe(appPlan)

  def startServer(): Unit = {
    startServer(config.getString("http.host"), config.getInt("http.port"), system)
  }

}