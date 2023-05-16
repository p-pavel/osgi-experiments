package com.perikov.osgi.javafx.platform.impl

import javax.persistence.*
import scala.beans.BeanProperty
import scala.compiletime.uninitialized
import org.osgi.service.component.annotations.*
import org.apache.aries.jpa.template.JpaTemplate
import org.osgi.service.log.*
import javax.sql.DataSource
import org.apache.aries.jpa.template.TransactionType

enum Sex extends Enum[Sex]:
  case Male,Female

@Entity
class Person:
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = uninitialized
  var name: String = uninitialized
  var age: Int = uninitialized
  var sex: Sex = uninitialized
  var externalId: java.util.UUID = uninitialized
end Person

@Entity
class Something:
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = uninitialized
  var name: String = uninitialized
end Something


@Component(immediate = true, enabled = false)
class JPATests @Activate (
  @Reference(service = classOf[LoggerFactory]) private val log: Logger,
  @Reference private val jt: JpaTemplate,
) :
  log.info(s"Created JPATests")
  val props = java.util.Properties()
  Map(
  ) foreach { (k,v) => props.put(k,v) }
  // val factory =  Persistence.createEntityManagerFactory("spike-javafx", props)
  val numIterations = 10_000
  val start = System.nanoTime()
  var s1 = 0l
  jt.tx(TransactionType.Required, em =>
    val s = Something()
    s.name = "Something"
    em.persist(s)
    for i <- 1 to numIterations do
      val p = Person()
      p.id = 1_000_000 + i
      p.name = "Pavel " + i
      p.age = 48
      p.externalId = java.util.UUID.randomUUID()
      p.sex = if i % 2 == 0 then Sex.Male else Sex.Female
      em.persist(p)
    end for
    em.flush()
    s1 = System.nanoTime() 

  )
  val dur = (System.nanoTime() - start)/1e9
  log.info(s"Created $numIterations entities in ${dur}, ${(s1 - start)/1e9} s")
  

  @Deactivate
  private def stop(): Unit = 
    log.info(s"Closed JPATests")

end JPATests