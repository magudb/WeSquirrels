package controllers


import play.Logger
import play.api.mvc.{Action, Controller}

class Services extends Controller {
 // var opts : Properties = new Properties
 // var connection = Conn.connect(opts)

  def linkAdd() = Action { request =>
      request.body.asText match {
        case Some(str) => {
          Logger.info(str)
          Ok("Send for creation")
        }
        case None => Status(400)
      }
  }

def linkInfo(url_option: Option[String]) = Action {
    Ok("Send for creation")
  }
}
