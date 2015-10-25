package utilities

import models.User
import play.api.Application
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.WS
import play.api.mvc.Results

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class OAuth2(application: Application) {
  lazy val authId = application.configuration.getString("google.client.id").get
  lazy val authSecret = application.configuration.getString("google.client.secret").get
  lazy val baseUrl = application.configuration.getString("google.redirect.url").get
  lazy val callback = application.configuration.getString("google.callback.url").get

  def getAuthorizationUrl(scope: String, state: String): String = {
    baseUrl.format(authId, callback, scope, state)
  }

  def getUser(accessToken: String) : Future[User] = {
    val profile = WS.url("https://www.googleapis.com/oauth2/v3/userinfo")(application).
      withHeaders("Authorization" -> s"Bearer $accessToken").
      get()

      profile.map[User] { response =>
              val jsonResult = response.json

              val id =  (jsonResult \ "sub").asOpt[String].get
              val name = (jsonResult \ "name").asOpt[String].get
              val picture = (jsonResult \ "picture").asOpt[String].get
              User(id, name, picture)
            }

  }
  def getToken(code: String): String = {
    val tokenResponse = WS.url("https://www.googleapis.com/oauth2/v3/token")(application).
      withQueryString("client_id" -> authId,
        "client_secret" -> authSecret,
        "code" -> code,
        "redirect_uri"-> callback,
        "grant_type" -> "authorization_code").
      withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON).
      post(Results.EmptyContent())



      val token = tokenResponse.flatMap { response =>
        (response.json \ "access_token").asOpt[String].fold(Future.failed[String](new IllegalStateException("Sod off!"))) { accessToken =>
          Future.successful(accessToken)
        }
      }
      Await.ready(token, Duration("200 millis"))
  }
}