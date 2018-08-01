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

import uk.gov.hmrc.play.bootstrap.controller.BaseController
import play.api.mvc._
import javax.inject.{Inject, Named, Singleton}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

@Singleton()
class EmailControllers @Inject()(http:HttpClient)extends BaseController {

  def send(domain: String) = Action.async(parse.text) { implicit request =>
//    Future.successful(Ok("Matt's Amazing"))

  def result = http.POSTString(s"localhost:8300/$domain/email", request.body, Seq.empty[(String,String)])
    result.map{ x => Status(x.status)}
  }

}
