/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.emailproxy.controllers

import org.apache.pekko.util.ByteString
import play.api.http.HttpEntity
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.*

import java.net.{ ConnectException, URI }
import java.util.concurrent.TimeoutException
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.{ BadGatewayException, HttpResponse }
import uk.gov.hmrc.play.audit.http.connector.{ AuditConnector, AuditResult }
import uk.gov.hmrc.play.audit.model.{ DataEvent, EventTypes }

@Singleton()
class EmailControllers @Inject() (
  http: HttpClientV2,
  cc: ControllerComponents,
  servicesConfig: ServicesConfig,
  auditConnector: AuditConnector
)(implicit
  ec: ExecutionContext
) extends BackendController(cc) {

  private lazy val rendererBaseUrl = servicesConfig.baseUrl("email")

  def gatewayTimeoutResult(e: Exception): Result = {
    val msg = s"""{ "statusCode": $BAD_GATEWAY, "message": "${e.getMessage}" } """
    Result(ResponseHeader(BAD_GATEWAY), HttpEntity.Strict(ByteString(msg), None))
      .withHeaders("HttpResponse.entity.contentType" -> "application/json")
  }

  def send(domain: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    http
      .post(new URI(s"$rendererBaseUrl/$domain/email").toURL)
      .withBody(request.body)
      .execute[HttpResponse]
      .map { r =>
        createAuditEvent()
        Result(ResponseHeader(r.status), HttpEntity.Strict(ByteString(r.body), r.header("contentType")))
          .withHeaders("HttpResponse.entity.contentType" -> "application/json")
      }
      .recover {
        case e: TimeoutException    => gatewayTimeoutResult(e)
        case e: ConnectException    => gatewayTimeoutResult(e)
        case e: BadGatewayException => gatewayTimeoutResult(e)

        case e =>
          Result(ResponseHeader(BAD_REQUEST), HttpEntity.Strict(ByteString(e.getMessage), None))
            .withHeaders("HttpResponse.entity.contentType" -> "application/json")
      }
  }

  private[controllers] def createAuditEvent()(using request: Request[JsValue]): Future[AuditResult] =
    auditConnector.sendEvent(
      DataEvent(
        auditSource = "email-proxy",
        auditType = EventTypes.Succeeded,
        tags = Map("path" -> request.path, "headers" -> request.headers.toString),
        detail = Map("request" -> request.body.toString)
      )
    )
}
