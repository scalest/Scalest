package pet

import pet.PetModel.{Genders, Location, Pet, User}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

object Migration {
  def randomEnum[E <: Enumeration](enum: E): E#Value = enum.values.toList.apply(Random.nextInt(enum.values.size - 1))

  def randomList[T](size: Int, factory: () => T): Seq[T] = Seq.fill(size)(factory())

  def randomPet(): Pet = {
    Pet(
      name = Random.nextString(10),
      adopted = Random.nextBoolean(),
      location = Location(Random.nextInt(100) * Random.nextDouble(), Random.nextInt(100) * Random.nextDouble()),
      gender = randomEnum(Genders),
      bodySize = Random.nextInt(255).toByte,
      tags = Seq.fill(Random.nextInt(23))(Random.nextString(5))
      )
  }

  def randomUser(): User = User(username = Random.nextString(10), password = Random.nextString(10))

  def migrate(implicit dc: DatabaseConfig[H2Profile]): Unit = {
    import dc.profile.api._

    Await.ready(
      dc.db.run(
        DBIO.seq(
          Pets.query.schema.createIfNotExists,
          Pets.query ++= randomList(100, randomPet),
          Users.query.schema.createIfNotExists,
          Users.query ++= randomList(100, randomUser)
          )
        ), Duration.Inf
      )
  }
}
