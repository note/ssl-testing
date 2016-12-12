package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer

import scala.util.{Failure, Success}
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    implicit val as = ActorSystem("client-system")
    implicit val mat = ActorMaterializer()
    implicit val scheduler = as.dispatcher

    val http = Http()

    val response = http.singleRequest(HttpRequest(uri = "http://akka.io"))

    response.andThen {
      case Success(r) =>
        val strict = r.entity.toStrict(5000.millis)
        val body = strict.map(_.data.utf8String)
        body.foreach { b =>
          println(s"Success: $b")
          mat.shutdown()
          as.terminate()
        }
      case Failure(ex) =>
        println(s"failure: $ex")
        mat.shutdown()
        as.terminate()
    }
  }
}
