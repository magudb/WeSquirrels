package controllers

import java.util.UUID

import play.api.Play
import play.api.mvc.{Action, Controller}
import utilities.OAuth

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by mud on 15-10-2015.
 */
class Authentication  extends Controller {
  lazy val oauth  = new OAuth(Play.current)
  lazy val scope = Play.current.configuration.getString("google.client.scope").get

  def google = Action { implicit request =>
      val state = UUID.randomUUID().toString  // random confirmation string
      val redirectUrl = oauth.getAuthorizationUrl(scope, state)
      Redirect(redirectUrl);
    }

  def logout = Action {
    Redirect("/").withNewSession
  }



  def googleCallback(code: Option[String] = None, state: Option[String] = None) = Action.async { implicit request =>
    (for {
      codeString <- code
      stateString <- state
    } yield {
        oauth.getUser(codeString).map { user =>
          Redirect("/").withSession("userId" -> user.userId, "name" -> user.name, "picture" -> user.picture)
        }.recover {
          case ex: IllegalStateException => Unauthorized(ex.getMessage)
        }
      }).getOrElse(Future.successful(BadRequest("No parameters supplied ")))
  }



}
