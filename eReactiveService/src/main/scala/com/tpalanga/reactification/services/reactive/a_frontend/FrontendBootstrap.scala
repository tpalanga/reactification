package com.tpalanga.reactification.services.reactive.a_frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.tpalanga.reification.common.KafkaProducer
import com.typesafe.scalalogging.LazyLogging

object FrontendBootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("actor-system-frontend")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val service = new FrontendService

  val msg = "Hello World"

  val kafkaProducer = new KafkaProducer()

  Http().bindAndHandle(new FrontendRoute(service, kafkaProducer).route, "0.0.0.0", 8085).map { httpServerBinding =>
    logger.info(s"Running at http://localhost:8085/")
  }
}