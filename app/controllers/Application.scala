package controllers

import com.rabbitmq.client.AMQP;
import play.api.mvc._
import Models._

class Application extends Controller {

  def index = Action {
    val person = new Link("userid", "http://google.com");
    Ok(views.html.index(person.url))
  }

}
