package com.tpalanga.reactification.services.rest.a_frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

object RestFrontendBootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("actor-system-frontend")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val service = system.actorOf(FrontendService.props)

  Http().bindAndHandle(new FrontendRoute(service).route, "0.0.0.0", 8084).map { httpServerBinding =>
    logger.info(s"Running at http://localhost:8084/")
  }

}