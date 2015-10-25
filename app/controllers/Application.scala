package controllers


import models._
import play.api.mvc._

class Application extends Controller {
  def createUser(implicit request: RequestHeader): Option[User] = {
    for {
      userId <- request.session.get("userId")
      name <- request.session.get("name")
      picture <- request.session.get("picture")
    } yield {
        User(userId, name, picture)
    }
  }

  def index = Action {  request =>

    createUser(request).map { user =>
      Ok(views.html.home(user))
    }.getOrElse {
      Ok(views.html.index("hello"))
    }

  }

}
