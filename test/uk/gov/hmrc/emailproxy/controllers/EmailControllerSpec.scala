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

import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ ControllerComponents, Headers }
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{ HttpClient, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ ExecutionContext, Future, TimeoutException }

class EmailControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {
  import play.api.test.Helpers._

  val validBody: String =
    """{
      | "to": ["andy.hicks@digital.hmrc.gov.uk"],
      | "templateId": "dc-1462-test-message",
      | "parameters": {
      |   "recipientName_line1": "Mr Andy Smith",
      |   "recipientName_line2": "in the capacity of",
      |   "recipientName_line3": "A. N. Other"
      | }
      |}""".stripMargin

  val fakeRequest: FakeRequest[String] =
    FakeRequest("POST", "/hmrc/email", Headers("Content-type" -> "application/json"), validBody)

  val injector: Injector = app.injector
  implicit lazy val ec: concurrent.ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  implicit lazy val sc: ServicesConfig = injector.instanceOf[ServicesConfig]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure(Map("appname" -> "TEST")).build()

  "Send email" should {
    "be valid" in {
      val mockHttpClient = mock[HttpClient]
      when(mockHttpClient.POST[JsValue, HttpResponse](anyString(), any(), any())(any(), any(), any(), any()))
        .thenReturn(
          Future.successful(HttpResponse(ACCEPTED, Json.parse("""{"result": "Hello"}"""), Map("" -> Seq("")))))
      val controller = new EmailControllers(mockHttpClient, cc, sc)

      implicit lazy val materializer: Materializer = app.materializer

      val result = call(controller.send("hmrc"), fakeRequest)

      status(result) mustEqual ACCEPTED
    }
  }

  "should be invalid" in {
    val mockHttpClient = mock[HttpClient]
    when(mockHttpClient.POST[JsValue, HttpResponse](anyString(), any(), any())(any(), any(), any(), any())).thenReturn(
      Future.successful(
        HttpResponse(BAD_REQUEST, Json.parse("""{"statusCode":  400, "message": "Something"}"""), Map("" -> Seq(""))))
    )
    val controller = new EmailControllers(mockHttpClient, cc, sc)

    implicit lazy val materializer: Materializer = app.materializer

    val result = call(controller.send("hmrc"), fakeRequest)

    status(result) mustEqual BAD_REQUEST
  }

  "email server no running" in {
    val mockHttpClient = mock[HttpClient]

    when(mockHttpClient.POST[JsValue, HttpResponse](anyString(), any(), any())(any(), any(), any(), any()))
      .thenAnswer(new Answer[Future[HttpResponse]] {
        override def answer(invocation: InvocationOnMock): Future[HttpResponse] = Future.failed(new TimeoutException())
      })
    val controller = new EmailControllers(mockHttpClient, cc, sc)

    implicit lazy val materializer: Materializer = app.materializer

    val result = call(controller.send("hmrc"), fakeRequest)

    status(result) mustEqual BAD_GATEWAY
  }

}
