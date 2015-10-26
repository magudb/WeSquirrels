package utilities

import models.User
import play.api.Application
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc.Results

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OAuth(application: Application) {
  lazy val authId = application.configuration.getString("google.client.id").get
  lazy val authSecret = application.configuration.getString("google.client.secret").get
  lazy val baseUrl = application.configuration.getString("google.redirect.url").get
  lazy val callback = application.configuration.getString("google.callback.url").get

  def getAuthorizationUrl(scope: String, state: String): String = {
    baseUrl.format(authId, callback, scope, state)
  }

  def getUser(code: String): Future[User] = {
    val futureResponse: Future[WSResponse] = for {
      access_token <- WS.url("https://www.googleapis.com/oauth2/v3/token")(application).
        withQueryString(
          "client_id" -> authId,
          "client_secret" -> authSecret,
          "code" -> code,
          "redirect_uri" -> callback,
          "grant_type" -> "authorization_code").
        withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON).
        post(Results.EmptyContent())
      user <- {
        val accessToken = (access_token.json \ "access_token").as[String]
        WS.url("https://www.googleapis.com/oauth2/v3/userinfo")(application).
          withHeaders("Authorization" -> s"Bearer $accessToken").
          get()
      }
    } yield user


    futureResponse.map[User] { response =>
              val jsonResult = response.json
                println(response.json.toString())
              val id =  (jsonResult \ "sub").asOpt[String].get
              val name = (jsonResult \ "name").asOpt[String].get
              val picture = (jsonResult \ "picture").asOpt[String].get
              User(id, name, picture)
            }
  }
}