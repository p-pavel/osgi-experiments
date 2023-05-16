package com.perikov.osgi.javafx.platform.impl

import javax.sql.* 
import org.osgi.service.component.annotations.*
import org.osgi.service.log.{LoggerFactory, Logger}
import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.implicits.*
import java.sql.PreparedStatement
import java.sql.Connection
import cats.effect.std.Random


trait JDBC[F[_]]:
  type Con
  type Stmt
  val connection: Resource[F, Con]
  extension (c: Con) 
    def prepareStatement(sql: String): Resource[F, Stmt]
    def setAutoCommit(b: Boolean): F[Unit]
    def commit(): F[Unit]
  extension (stmt: Stmt)
    def setString(i: Int, s: String): F[Unit]
    def setLong(i: Int, l: Long): F[Unit]
    def execute(): F[Boolean]

object JDBC:
  def onDataSource(ds: DataSource): JDBC[IO] = 
    new :
      type Con = Connection
      type Stmt = PreparedStatement
      val connection = Resource.fromAutoCloseable(IO(ds.getConnection()))

      extension (c: Con) 
        def prepareStatement(sql: String): Resource[IO, Stmt] = Resource.fromAutoCloseable(IO(c.prepareStatement(sql)))
        def setAutoCommit(b: Boolean): IO[Unit] = IO(c.setAutoCommit(b))
        def commit(): IO[Unit] = IO(c.commit())
      extension (stmt: Stmt)
        def setString(i: Int, s: String): IO[Unit] = IO(stmt.setString(i, s))
        def setLong(i: Int, l: Long): IO[Unit] = IO(stmt.setLong(i, l))
        def execute(): IO[Boolean] = IO(stmt.execute())
    end new

def dbTest(using J: JDBC[IO], log: Logger):IO[Unit] = 
  import J.*
  
  val s = for 
    con <- connection
    _ <- Resource.eval(con.setAutoCommit(false))
    stmt <- con.prepareStatement("INSERT INTO Person(Id, Name) VALUES(?,?)")
  yield (con,stmt)
  s.use { (con, stmt) =>
    def insertSomething(r: Random[IO]): IO[Unit] = 
      for 
        id <- r.nextLong
        nick = "Name " + id
        nick <- r.nextLong.map("Nick " + _)
        _ <- stmt.setLong(1, id)
        _ <- stmt.setString(2, nick)
        _ <- stmt.execute()
      yield ()

    val numIterations = 100_000
    for 
      r <- Random.scalaUtilRandom[IO]
      iteration = insertSomething(r).replicateA_(numIterations)
      .flatMap(_ => con.commit())
      .timed.flatMap((d,_) => IO(log.info(s"Time: ${d/numIterations}")))
      res <- iteration.foreverM
    yield res
    }

def testResource(ds: DataSource)(using l: Logger) = 
  dbTest(using JDBC.onDataSource(ds)).onError(t => IO(l.error(t.getMessage()))).background
  

@Component(immediate = true, enabled = false)
class JDBCTest @Activate (
  @Reference(service = classOf[LoggerFactory]) private val log: Logger,
  @Reference(service = classOf[DataSource], target = "(dataSourceName=h2db)") private val ds: DataSource,
) :
  log.info(s"Created JDBC Test with $ds")
  given Logger = log
  import unsafe.implicits.global
  private val (_, release) = testResource(ds).allocated.unsafeRunSync()
  @Deactivate
  private def stop(): Unit = 
    release.unsafeRunSync()
    log.info(s"Deactivated JDBC Test")

end JDBCTest
