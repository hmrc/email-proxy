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

import java.util.concurrent.TimeoutException

import akka.stream.Materializer
import com.typesafe.config.Config
import play.api.test.FakeRequest
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Headers
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class EmailControllerSpec extends PlaySpec with GuiceOneAppPerSuite  {
  import play.api.test.Helpers._

  val validBody = """{ "to": ["andy.hicks@digital.hmrc.gov.uk"], "templateId": "dc-1462-test-message", "parameters": { "recipientName_line1": "Mr Andy Smith", "recipientName_line2": "in the capacity of", "recipientName_line3": "A. N. Other" } }"""

  val fakeRequest = FakeRequest("POST", "/hmrc/email", Headers("Content-type" -> "application/json"), validBody)


  "Send email" should {
    "should be valid" in {
      val validEmailSent = new DummyHttpClient {
        override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier): Future[HttpResponse] =
          Future.successful( HttpResponse(202, Some(Json.parse("""{"result": "Hello"}"""))))
      }
      val conf = Configuration( "Test.microservice.services.email.host"  -> "localhost", "Test.microservice.services.email.port"  -> "80")

      val env = Environment.simple()

      val controller =  new EmailControllers(validEmailSent, conf, env)

      implicit lazy val materializer: Materializer = app.materializer

      val result = call( controller.send("hmrc"), fakeRequest )

      status(result) mustEqual ACCEPTED
    }
  }

  "should be invalid" in {
    val validEmailSent = new DummyHttpClient {
      override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier): Future[HttpResponse] =
        Future.successful( HttpResponse(400, Some(Json.parse("""{"statusCode":  400, "message": "Something"}"""))))
    }
    val conf = Configuration( "Test.microservice.services.email.host"  -> "localhost", "Test.microservice.services.email.port"  -> "80")

    val env = Environment.simple()

    val controller =  new EmailControllers(validEmailSent, conf, env)

    implicit lazy val materializer: Materializer = app.materializer

    val result = call( controller.send("hmrc"), fakeRequest )

    status(result) mustEqual BAD_REQUEST
  }

  "email server no running" in {
    val validEmailSent = new DummyHttpClient {
      override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier): Future[HttpResponse] =
        throw new TimeoutException("Ahhhh")
    }
    val conf = Configuration( "Test.microservice.services.email.host"  -> "localhost", "Test.microservice.services.email.port"  -> "80")

    val env = Environment.simple()

    val controller =  new EmailControllers(validEmailSent, conf, env)

    implicit lazy val materializer: Materializer = app.materializer


    assertThrows[TimeoutException]  {

      val result = call(controller.send("hmrc"), fakeRequest)

      status(result) mustEqual BAD_REQUEST
    }
  }


}



//     BadRequest(Json.obj("statusCode" -> 400, "message" -> message))

trait DummyHttpClient extends HttpClient {
  override def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = ???

  override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override def doEmptyPost[A](url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override def doFormPost(url: String, body: Map[String, Seq[String]])(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override def doDelete(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = ???

  override def configuration: Option[Config] = ???

  override def doPut[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = ???

  override val hooks: Seq[HttpHook] = Seq.empty[HttpHook]

  override def doPatch[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = ???

}


