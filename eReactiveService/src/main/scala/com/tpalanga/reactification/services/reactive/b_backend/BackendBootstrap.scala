package com.tpalanga.reactification.services.reactive.b_backend

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.tpalanga.reification.common.KafkaConsumer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

object BackendBootstrap extends App with LazyLogging {

  implicit val system = ActorSystem("actor-system-backend")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def messageHandler: String => Future[Unit] =
    msg => Future {
      logger.info(s"Processing: $msg")
      // - create pdf document
      // - send email with download link
      //Thread.sleep(2000)
      // - post a message to a "Done" queue
      logger.info(s"Processing done: $msg")
    }

  val kafkaConsumer = new KafkaConsumer(messageHandler)
}