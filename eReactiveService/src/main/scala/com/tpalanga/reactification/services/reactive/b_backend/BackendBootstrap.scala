package com.tpalanga.reactification.services.reactive.b_backend

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.tpalanga.reification.common.KafkaConsumer
import com.typesafe.scalalogging.LazyLogging

object BackendBootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("actor-system-backend")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def messageHandler: String => Unit =
    msg => {
      logger.info(s"Processing: $msg")
      // - create pdf document
      // - send email with download link
    }

  val kafkaConsumer = new KafkaConsumer(messageHandler)
}