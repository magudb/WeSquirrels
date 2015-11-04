package controllers

import com.rabbitmq.client.ConnectionFactory
import play.api.Play
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

class Services extends Controller {
  val factory = new ConnectionFactory()
  val host = Play.current.configuration.getString("rabbitmq.host").get
  factory.setHost(host)
  val connection = factory.newConnection();
  val sendingChannel = connection.createChannel();
  val exchange = "fanout"

  def linkAdd(url: Option[String], title: Option[String], summary: Option[String]) = Action { request =>



    for {
      urlString <- url
      titleString <- title
      summaryString <- summary
      userId <- request.session.get("userId")
    } yield {
      val jsonObject = Json.toJson(
           Map(
                "url" -> Json.toJson(urlString),
                "title" -> Json.toJson(titleString),
                "summary" -> Json.toJson(summaryString)
              )

        )
      val jsonString: String = Json.stringify(jsonObject)

      sendingChannel.basicPublish(exchange, "links.add", null, jsonString.getBytes())

    }
    Ok("Send for creation")
  }

  def linkInfo(url_option: Option[String]) = Action {
    Ok("Send for creation")
  }
}
