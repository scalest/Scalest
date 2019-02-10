//package scalest.admin.slick
//
//import cats.implicits._
//import com.typesafe.config.ConfigFactory
//import io.circe.{Decoder, Json}
//import scalest.admin.crud.SearchDsl
//import scalest.admin.crud.SearchDsl.{And, Equal, FieldSearchDsl, Greater, GreaterEqual, Less, LessEqual, NotEqual, Or}
//import scalest.admin.slick.Test.Tests.TestsTable
//import slick.basic.DatabaseConfig
//import slick.jdbc.{H2Profile, JdbcProfile, JdbcType}
//
//import scala.concurrent.ExecutionContext.global
//import scala.concurrent.duration.Duration
//import scala.concurrent.{Await, ExecutionContext}
//import scala.language.experimental.macros
//
//trait SlickSearchDsl {
//  type Profile <: JdbcProfile
//  val profile: Profile
//  import profile.api._
//
//  case class FieldInfo[T <: Table[_], F](name: String, rep: T => Rep[F])(
//    implicit
//    val decoder: Decoder[F],
//    val jdbcType: JdbcType[F],
//  )
//
//  class SearchContext[T <: Table[_]](val table: T, val fields: Seq[FieldInfo[T, Any]]) {
//    def find(name: String): Either[String, FieldInfo[T, Any]] =
//      fields.find(_.name == name).toRight(s"Cannot search for field $name")
//  }
//  object SearchContext {
//    def apply[T <: Table[_]](table: T)(fields: FieldInfo[T, _]*) =
//      new SearchContext[T](table, fields.asInstanceOf[Seq[FieldInfo[T, Any]]])
//  }
//
//  def interpretFieldDsl[T](dsl: FieldSearchDsl)(implicit ctx: SearchContext[T]): Either[String, Rep[Boolean]] =
//    ctx.find(dsl.field).flatMap { i =>
//      import i._
//      val field = i.rep(ctx.table)
//      decoder(dsl.value.hcursor).left
//        .map(_.toString)
//        .map(_.bind)
//        .map { v =>
//          dsl match {
//            case _: Equal        => field === v
//            case _: NotEqual     => field =!= v
//            case _: Less         => field < v
//            case _: LessEqual    => field <= v
//            case _: Greater      => field > v
//            case _: GreaterEqual => field >= v
//          }
//        }
//    }
//
//  def interpret[T](dsl: SearchDsl)(implicit ctx: SearchContext[T]): Either[String, Rep[Boolean]] =
//    dsl match {
//      case And(left, right)               => (interpret(left), interpret(right)).mapN(_ && _)
//      case Or(left, right)                => (interpret(left), interpret(right)).mapN(_ || _)
//      case fieldSearchDsl: FieldSearchDsl => interpretFieldDsl(fieldSearchDsl)
//    }
//}
//
//object Test extends App {
//  object H2SlickModule extends SlickModule[H2Profile](H2Profile)
//
//  import H2SlickModule._
//  import H2SlickModule.profile.api._
//
//  case class Test(name: String, age: Int)
//
//  object Tests {
//    val tests = TableQuery[TestsTable]
//
//    class TestsTable(tag: Tag) extends Table[Test](tag, "test") {
//      val name = column[String]("name")
//      val age = column[Int]("age")
//      override def * = (name, age).mapTo[Test]
//    }
//  }
//
//  implicit lazy val ec: ExecutionContext = global
//
//  val config = ConfigFactory.parseString(
//    """
//      |slick {
//      |  profile = "slick.jdbc.H2Profile$"
//      |  db = {
//      |    url = "jdbc:h2:mem:test1"
//      |    driver = org.h2.Driver
//      |    keepAliveConnection = true
//      |    numThreads = 4
//      |  }
//      |}""".stripMargin,
//  )
//  implicit lazy val dc: DatabaseConfig[H2Profile] = DatabaseConfig.forConfig[H2Profile]("slick", config)
//
//  val query = Or(
//    Equal("name", Json.fromString("Oleg")),
//    GreaterEqual("age", Json.fromInt(18)),
//  )
//
//  val program = for {
//    _ <- dc.db.run(Tests.tests.schema.createIfNotExists)
//    _ <- dc.db.run(Tests.tests ++= List(Test("Gennadiy", 15), Test("Oleg", 16), Test("Oleg", 20), Test("Danil", 21)))
//    results: Seq[Test] <- dc.db.run(
//      Tests.tests.filter { table =>
//        // Macros
//        implicit val ctx: SearchContext[TestsTable] = SearchContext(table)(
//          FieldInfo[TestsTable, String]("name", _.name),
//          FieldInfo[TestsTable, Int]("age", _.age),
//        )
//        // Macros
//        interpret(query).fold(DBIO.failed())
//      }.result,
//    )
//  } yield println(results)
//
//  Await.result(program, Duration.Inf)
//}
