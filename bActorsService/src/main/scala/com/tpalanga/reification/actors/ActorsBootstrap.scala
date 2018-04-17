package com.tpalanga.reification.actors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

object ActorsBootstrap extends App with LazyLogging {
  implicit val system = ActorSystem("actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val actorService = system.actorOf(ActorsService.props)

  Http().bindAndHandle(new ActorsRoute(actorService).route, "0.0.0.0", 8082).map { httpServerBinding =>
    logger.info(s"Running at http://localhost:8082/")
  }
}
