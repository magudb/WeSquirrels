package controllers

import java.util.UUID

import play.api.Play
import play.api.mvc.{Action, Controller}
import utilities.OAuth2

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by mud on 15-10-2015.
 */
class Authentication  extends Controller {
  lazy val oauth2 = new OAuth2(Play.current)
  lazy val scope = Play.current.configuration.getString("google.client.scope").get

  def google = Action { implicit request =>
      val state = UUID.randomUUID().toString  // random confirmation string
      val redirectUrl = oauth2.getAuthorizationUrl(scope, state)
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
        val token = oauth2.getToken(codeString)
        oauth2.getUser(token).map { user =>
          Redirect("/").withSession("userId" -> user.userId, "name" -> user.name, "picture" -> user.picture)
        }.recover {
          case ex: IllegalStateException => Unauthorized(ex.getMessage)
        }
      }).getOrElse(Future.successful(BadRequest("No parameters supplied ")))
  }



}
