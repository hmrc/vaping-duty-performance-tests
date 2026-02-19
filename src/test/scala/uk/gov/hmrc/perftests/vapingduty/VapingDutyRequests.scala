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

package uk.gov.hmrc.perftests.vapingduty

import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration
import uk.gov.hmrc.perftests.vapingduty.models.AuthUser

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object VapingDutyRequests extends ServicesConfiguration {

  // ---------- Base service URLs ----------
  val vapingDutyBaseUrl: String        = baseUrlFor("vaping-duty-frontend").stripSuffix("/")
  val authLoginStubBaseUrl: String     = baseUrlFor("auth-login-stub").stripSuffix("/")
  val emailVerificationBaseUrl: String = baseUrlFor("email-verification").stripSuffix("/")

  // ---------- Routes ----------
  private val vapingDutyRoute         = "/vaping-duty"
  private val enrolmentRoute          = "/enrolment"
  private val contactPreferencesRoute = "/contact-preferences"

  // ---------- Base paths ----------
  private val vapingDutyPath         = s"$vapingDutyBaseUrl$vapingDutyRoute"
  private val enrolmentPath          = s"$vapingDutyPath$enrolmentRoute"
  private val contactPreferencesPath = s"$vapingDutyPath$contactPreferencesRoute"

  // ---------- Test data ----------
  val emailAddressToVerify: String = randomTestEmail()
  val credIdToUse: String          = randomCredId()

  // ---------- CSRF ----------
  val CsrfPattern: String =
    """<input type="hidden" name="csrfToken" value="([^"]+)""""

  // ---------- Auth login stub URLs ----------
  private val ggAuthSignInUrl: String =
    s"$authLoginStubBaseUrl/auth-login-stub/gg-sign-in"

  private val authSessionUrl: String =
    s"$authLoginStubBaseUrl/auth-login-stub/session"

  // ---------- Enrolment URLs ----------
  private val doYouHaveApprovalIdUrl: String =
    s"$enrolmentPath/do-you-have-an-approval-id"

  private val administratorRequiredUrl: String =
    s"$enrolmentPath/organisation-administrator-required"

  private val youNeedAnApprovalIDUrl: String =
    s"$enrolmentPath/you-need-an-approval-id"

  private val alreadyEnrolledUrl: String =
    s"$enrolmentPath/already-enrolled"

  // ---------- Contact preference URLs ----------
  val howShouldWeContactYouUrl: String =
    s"$contactPreferencesPath/how-should-we-contact-you"

  private val checkYourPostalAddressUrl: String =
    s"$contactPreferencesPath/check-your-postal-address"

  private val changeYourPostalAddressUrl: String =
    s"$contactPreferencesPath/change-your-postal-address"

  private val enterEmailAddressUrl: String =
    s"$contactPreferencesPath/enter-email-address"

  private val contactPreferenceUpdatedUrl: String =
    s"$contactPreferencesPath/contact-preference-updated"

  private val confirmEmailAddressUrl: String =
    s"$contactPreferencesPath/confirm-email-address"

  private val tooManyAttemptsUrl: String =
    s"$contactPreferencesPath/too-many-attempts"

  def saveCsrfToken(): CheckBuilder[RegexCheckType, String] = regex(_ => CsrfPattern).saveAs("csrfToken")

  def randomTestEmail(): String = {
    val formatter = DateTimeFormatter.ofPattern("ddMMmmss")
    val timestamp = LocalDateTime.now().format(formatter)
    s"autotest$timestamp@example.com"
  }

  def randomCredId(): String =
    System.currentTimeMillis().toString.takeRight(16)

  val getAuthLoginPage: HttpRequestBuilder =
    http("Navigate to auth login stub page")
      .get(ggAuthSignInUrl)
      .check(status.is(200))
      .check(saveCsrfToken())

  def postAuthLoginPage(user: AuthUser, redirectUrl: String = doYouHaveApprovalIdUrl): HttpRequestBuilder =
    http("Login with user credentials")
      .post(ggAuthSignInUrl)
      .formParam("enrolment[0].credId", credIdToUse)
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("credentialStrength", "strong")
      .formParam("confidenceLevel", "50")
      .formParam("authorityId", "")
      .formParam("groupIdentifier", "")
      .formParam("email", "user@test.com")
      .formParam("credentialRole", "User")
      .formParam("redirectionUrl", redirectUrl)
      .formParam("affinityGroup", user.affinityGroup)
      .formParam("enrolment[0].state", user.enrolmentState)
      .formParam("enrolment[0].name", user.enrolmentKey)
      .formParam("enrolment[0].taxIdentifier[0].name", user.taxIdentifierName)
      .formParam("enrolment[0].taxIdentifier[0].value", user.taxIdentifierValue)
      .check(status.is(303))

  val getAuthSession: HttpRequestBuilder =
    http("get Auth Session")
      .get(authSessionUrl)
      .check(
        status.is(200),
        regex("""data-session-id="sessionId"[\s\S]*?<code[^>]*>(session-[^<]+)</code>""")
          .saveAs("sessionId"),
        regex("""data-session-id="authToken"[\s\S]*?<code[^>]*>[\s\S]*?(Bearer [A-Za-z0-9+/=]+)""")
          .saveAs("bearerToken")
      )

  val signOutSurvey: HttpRequestBuilder =
    http("Sign Out Survey")
      .get(s"$vapingDutyPath/account/sign-out-survey")
      .check(status.is(303))

  def getPasscodes(email: String): HttpRequestBuilder =
    http("get passcodes")
      .get(s"$emailVerificationBaseUrl/test-only/passcodes")
      .header("content-type", "application/x-www-form-urlencoded")
      .header("x-session-id", s => s("sessionId").as[String])
      .header("authorization", s => s("bearerToken").as[String])
      .check(
        status.is(200),
        jsonPath(s"$$.passcodes[?(@.email=='$email')].passcode")
          .ofType[String]
          .findAll
          .transform(_.lastOption.getOrElse(throw new RuntimeException(s"No passcode found for $email")))
          .saveAs("emailPasscode")
      )

  val navigateToVapingDutyPage: HttpRequestBuilder =
    http("Navigate to vaping duty Page")
      .get(s"$vapingDutyBaseUrl/$vapingDutyRoute")
      .check(status.is(200))

  val getEnrolmentDoYouHaveAnApprovalIdPage: HttpRequestBuilder =
    http("Get Enrolment Approval Page")
      .get(doYouHaveApprovalIdUrl)
      .check(status.is(200))
      .check(saveCsrfToken())

  def postEnrolmentDoYouHaveAnApprovalIdPage(enrolmentApprovalQuestion: Boolean): HttpRequestBuilder =
    http("Post Enrolment Approval Page")
      .post(doYouHaveApprovalIdUrl)
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", enrolmentApprovalQuestion)
      .check(status.is(303))

  val getOrganisationAdministratorRequiredPage: HttpRequestBuilder =
    http("Get Enrolment Organisation Sign In Page")
      .get(administratorRequiredUrl)
      .check(status.is(200))

  val getYouNeedAnApprovalIDPage: HttpRequestBuilder =
    http("Get VPDID Approval Required Page")
      .get(youNeedAnApprovalIDUrl)
      .check(status.is(200))

  val getAlreadyEnrolledPage: HttpRequestBuilder =
    http("Get Already Enrolled Page")
      .get(alreadyEnrolledUrl)
      .check(status.is(200))

  val getHowShouldWeContactYouPage: HttpRequestBuilder =
    http("Get How Should We Contact You Page")
      .get(howShouldWeContactYouUrl)
      .check(status.is(200))
      .check(css("input[name='csrfToken']", "value").saveAs("contactPrefCsrf"))

  def postHowShouldWeContactYouPage(contactPreferenceRadioButton: String): HttpRequestBuilder =
    http("Post How Should We Contact You Page")
      .post(howShouldWeContactYouUrl)
      .formParam("csrfToken", "#{contactPrefCsrf}")
      .formParam("value", contactPreferenceRadioButton)
      .check(status.is(303))

  val getCheckYourPostalAddressPage: HttpRequestBuilder =
    http("Get Check Your Postal Address Page")
      .get(checkYourPostalAddressUrl)
      .check(status.is(200))

  val getChangeYourPostalAddressPage: HttpRequestBuilder =
    http("Get Change Your Postal Address Page")
      .get(changeYourPostalAddressUrl)
      .check(status.is(200))

  val getContactPreferenceUpdatedPage: HttpRequestBuilder =
    http("Get Contact Preference Updated Page")
      .get(contactPreferenceUpdatedUrl)
      .check(status.is(200))

  val getWhatEmailAddressToBeContactedPage: HttpRequestBuilder =
    http("Get What Email Address To Be Contacted Page")
      .get(enterEmailAddressUrl)
      .check(status.is(200))

  def postWhatEmailAddressToBeContactedPage(emailAddress: String): HttpRequestBuilder =
    http("Post What Email Address To Be Contacted Page")
      .post(enterEmailAddressUrl)
      .formParam("csrfToken", "#{contactPrefCsrf}")
      .formParam("value", emailAddress)
      .check(status.is(303), header("Location").saveAs("emailVerificationRedirectUrl"))

  val getEmailConfirmationCodePage: HttpRequestBuilder =
    http("Get Email Confirmation Code Page")
      .get(session => session("emailVerificationRedirectUrl").as[String])
      .check(status.is(200))

  def postEmailConfirmationCodePage(): HttpRequestBuilder =
    http("Post Email Confirmation Code Page")
      .post(session => session("emailVerificationRedirectUrl").as[String])
      .formParam("csrfToken", "#{contactPrefCsrf}")
      .formParam("passcode", "#{emailPasscode}")
      .check(status.is(303))

  val getConfirmEmailAddressPage: HttpRequestBuilder =
    http("Get Confirm Email Address Page")
      .get(confirmEmailAddressUrl)
      .check(status.is(200))

  def postConfirmEmailAddressPage(): HttpRequestBuilder =
    http("Post Confirm Email Address Page")
      .post(confirmEmailAddressUrl)
      .formParam("csrfToken", "#{contactPrefCsrf}")
      .check(status.is(303))

  val getTooManyAttemptsPage: HttpRequestBuilder =
    http("Get Too Many Attempts Page")
      .get(tooManyAttemptsUrl)
      .check(status.is(200))
}
