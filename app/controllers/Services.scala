package controllers

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.QueueingConsumer.Delivery
import com.rabbitmq.client.{ConnectionFactory, QueueingConsumer}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

class Services extends Controller {
  val factory = new ConnectionFactory()
  factory.setHost("localhost")
  val connection = factory.newConnection()
  val rpcChannel = connection.createChannel()
  val requestQueueName = "rpc_link_queue"
  val replyQueueName = rpcChannel.queueDeclare().getQueue()
  val consumer = new QueueingConsumer(rpcChannel)
  rpcChannel.basicConsume(replyQueueName, true, consumer)


  def toBytes(x: String) = x.getBytes("UTF-8")
  def fromBytes(x: Array[Byte]) = new String(x, "UTF-8")
  def whileLoop(cond : =>Boolean)(block : =>Unit) : Unit =
    if(cond) {
      block
      whileLoop(cond)(block)
    }

  def linkAdd(url: Option[String], title: Option[String], summary: Option[String]) = Action { request =>
    for {
      urlString <- url
      titleString <- title
      summaryString <- summary
      userId <- request.session.get("userId")
    } yield {

    }
    Ok("Send for creation")
  }

  def linkInfo(url_option: Option[String]) = Action.async {

    val corrId = java.util.UUID.randomUUID().toString()

    val props = new BasicProperties
    .Builder()
      .correlationId(corrId)
      .replyTo(replyQueueName)
      .build()

      val url:String = url_option map { _ } getOrElse ""

      if(url.isEmpty){
        Future.successful(BadRequest("No url supplied"))
      }
      rpcChannel.basicPublish("", requestQueueName, props, url.getBytes())
     val response:String  = getRPCCallback(corrId, consumer.nextDelivery())


    Future.successful(Ok(response))
  }
  def getRPCCallback(id: String, delivery:Delivery): String = delivery.getProperties().getCorrelationId().equals(id) match{
    case _ => ""
    case true => return fromBytes(delivery.getBody())
    case false => getRPCCallback(id, consumer.nextDelivery())
  }


}
