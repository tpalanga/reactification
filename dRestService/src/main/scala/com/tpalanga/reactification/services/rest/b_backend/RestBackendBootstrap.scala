package com.tpalanga.reactification.services.rest.b_backend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.reactification.services.rest.a_frontend.FrontendRoute
import com.tpalanga.reactification.services.rest.a_frontend.RestFrontendBootstrap.{logger, service}
import com.tpalanga.reification.common.KafkaConsumer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

object RestBackendBootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("actor-system-backend")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val service = new BackendService

  Http().bindAndHandle(new BackendRoute(service).route, "0.0.0.0", 8184).map { httpServerBinding =>
    logger.info(s"Running at http://localhost:8184/")
  }

}