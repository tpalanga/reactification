package com.tpalanga.reactification.a_monolith

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.reactification.a_monolith.route.MonolithRoute
import com.tpalanga.reactification.a_monolith.service.MonolithService
import com.typesafe.scalalogging.LazyLogging

object Bootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("account-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val monolithService = new MonolithService

  Http().bindAndHandle(new MonolithRoute(monolithService).route, "0.0.0.0", 8080).map { httpServerBinding =>
    logger.info(s"Running at http://localhost:8080/")
  }
}