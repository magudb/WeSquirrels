package controllers

import play.api._
import play.api.mvc._

/**
 * Created by mud on 15-10-2015.
 */
class Authentication  extends Controller {

  def index = Action {

    Ok(views.html.index("Your new application is ready."))

  }

}
