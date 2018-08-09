/*
 * Copyright 2018 HM Revenue & Customs
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

import java.net.ConnectException
import java.util.concurrent.TimeoutException

import akka.util.ByteString
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import play.api.mvc._
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import play.api.http.HttpEntity
import play.api.libs.json.JsValue
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._


@Singleton()
class EmailControllers @Inject()(
                                  http:HttpClient,
                                  val runModeConfiguration: Configuration,
                                  envi: Environment
                                )extends BaseController with ServicesConfig {

  protected def mode: Mode = envi.mode

  private lazy val rendererBaseUrl = baseUrl("email")


  def gatewayTimeoutResult( e:Exception):Result = {
    val msg = s"""{ "statusCode": $BAD_GATEWAY, "message": "${e.getMessage}" } """
    Result(ResponseHeader(BAD_GATEWAY), HttpEntity.Strict(ByteString(msg), None))
      .withHeaders("Content-Type" -> "application/json")
  }

  def send(domain: String): Action[JsValue] = Action.async(parse.json) { implicit request =>

    http.POST(s"$rendererBaseUrl/$domain/email", request.body)
        .map{ r =>
          Result( ResponseHeader(r.status), HttpEntity.Strict(ByteString(r.body), r.header("contentType")) ).withHeaders("Content-Type" -> "application/json")
        }
      .recover{
        case e: TimeoutException   => gatewayTimeoutResult(e)
        case e: ConnectException => gatewayTimeoutResult(e)
        case e: BadGatewayException =>  gatewayTimeoutResult(e)

        case e =>
          Result( ResponseHeader(400), HttpEntity.Strict(ByteString(e.getMessage), None) ).withHeaders("Content-Type" -> "application/json")
      }

  }

}
