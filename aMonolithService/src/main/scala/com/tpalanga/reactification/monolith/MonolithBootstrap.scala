package com.tpalanga.reactification.monolith

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

object MonolithBootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val monolithService = new MonolithService

  Http().bindAndHandle(new MonolithRoute(monolithService).route, "0.0.0.0", 8080).map { httpServerBinding =>
    logger.info(s"Running at http://localhost:8080/")
  }
}