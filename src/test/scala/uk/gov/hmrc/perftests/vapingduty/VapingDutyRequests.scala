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
  val vapingDutyAccountBaseUrl: String = baseUrlFor("vaping-duty-account").stripSuffix("/")

  // ---------- Base paths ----------
  private val vapingDutyPath         = s"$vapingDutyBaseUrl/vaping-duty"
  private val enrolmentPath          = s"$vapingDutyPath/enrolment"
  private val contactPreferencesPath = s"$vapingDutyPath/contact-preferences"
  private val completeReturnPath     = s"$vapingDutyPath/complete-return"
  private val dutySuspendedPath      = s"$completeReturnPath/duty-suspended"
  private val adjustmentPath         = s"$completeReturnPath/adjustment"

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

  val ViewYourReturnsUrl: String =
    s"$vapingDutyPath/view-your-returns"

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

  // ---------- Complete return URLs ----------
  private val CompleteReturnStartPageUrl: String =
    s"$completeReturnPath/before-you-start"

  private val CompleteReturnTaskListUrl: String =
    s"$completeReturnPath/task-list"

  private val DeclareDutyUrl: String =
    s"$completeReturnPath/declare-duty"

  private val AmountOfVapingProductsReleasedUrl: String =
    s"$completeReturnPath/enter-amount-released"

  private val DeclareDutyCYAUrl: String =
    s"$completeReturnPath/duty-check-your-answers-summary"

  private val CheckYourAnswersUrl: String =
    s"$completeReturnPath/check-your-answers"

  private val ReturnSubmittedUrl: String =
    s"$completeReturnPath/return-submitted"

  private val ReturnDeclarationUrl: String =
    s"$completeReturnPath/return-declaration"

  // ---------- Duty Suspended URLs ----------
  private val declareDutySuspenseUrl: String =
    s"$dutySuspendedPath/suspended-products"

  private val dutySuspendedCYAUrl: String =
    s"$dutySuspendedPath/duty-suspense-check-your-answers-summary"

  private val enterDutySuspenseUrl: String =
    s"$dutySuspendedPath/enter-received-or-moved-amount"

  // ---------- Spoilt Adjustment URLs ----------
  private val declareSpoiltProductsUrl  = s"$adjustmentPath/declare-spoilt-products"
  private val selectSpoiltPeriodUrl     = s"$adjustmentPath/select-spoilt-period"
  private val enterSpoiltAmountUrl      = s"$adjustmentPath/enter-spoilt-amount"
  private val spoiltProductsCYAUrl      = s"$adjustmentPath/check-your-spoilt-products-answers"
  private val removeSpoiltAdjustmentUrl = s"$adjustmentPath/remove-spoilt-adjustment"

  // ---------- Over / Under Adjustment URLs ----------
  private val declareAdjustmentsUrl         = s"$adjustmentPath/declare-adjustments"
  private val selectAdjustmentPeriodUrl     = s"$adjustmentPath/select-period"
  private val adjustmentCheckYourAnswersUrl = s"$adjustmentPath/check-your-answers"
  private val adjustmentReasonUrl           = s"$adjustmentPath/reason-for-adjustment"
  private val removeAdjustmentUrl           = s"$adjustmentPath/remove-adjustment"

  // ---------- View Payments URLs ----------
  val viewPaymentsUrl: String =
    s"$vapingDutyPath/view-payments"

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
    http("Get Auth Session")
      .get(authSessionUrl)
      .check(
        status.is(200),
        regex("""data-session-id="sessionId"[\s\S]*?<code[^>]*>(session-[^<]+)</code>""")
          .saveAs("sessionId"),
        regex("""data-session-id="authToken"[\s\S]*?<code[^>]*>[\s\S]*?(Bearer [A-Za-z0-9+/=]+)""")
          .saveAs("bearerToken")
      )

  def getVpdSummary(vpdId: String): HttpRequestBuilder =
    http("Get VPD Summary")
      .get(s"$vapingDutyAccountBaseUrl/vaping-duty-account/vpd/summary/$vpdId")
      .header("authorization", s => s("bearerToken").as[String])
      .header("x-correlation-id", "5678")
      .header("x-request-id", "12334")
      .check(status.is(200))

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
      .get(vapingDutyPath)
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
      .formParam("email", emailAddress)
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

  val getCompleteReturnStartPage: HttpRequestBuilder =
    http("Get Complete Return Start Page")
      .get(s"$vapingDutyBaseUrl#{submitPeriodUrl}")
      .check(status.is(200))
      .check(css("a.govuk-button", "href").saveAs("taskListUrl"))

  val getCompleteReturnTaskListPage: HttpRequestBuilder =
    http("Get Complete Return Task List Page")
      .get(s"$vapingDutyBaseUrl#{taskListUrl}")
      .check(status.is(200))

  val getDeclareDutyPage: HttpRequestBuilder =
    http("Get Declare Duty Page")
      .get(s"$DeclareDutyUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postDeclareDutyPage(hasDutyToDeclare: Boolean): HttpRequestBuilder =
    http("Post Declare Duty Page")
      .post(s"$DeclareDutyUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", hasDutyToDeclare)
      .check(status.is(303))

  val getAmountOfVapingProductsReleasedPage: HttpRequestBuilder =
    http("Get Amount Of Vaping Products Released Page")
      .get(s"$AmountOfVapingProductsReleasedUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postAmountOfVapingProductsReleasedPage(amount: String): HttpRequestBuilder =
    http("Post Amount Of Vaping Products Released Page")
      .post(s"$AmountOfVapingProductsReleasedUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", amount)
      .check(status.is(303))

  val getDeclareDutySuspendedPage: HttpRequestBuilder =
    http("Get Declare Duty Page")
      .get(s"$declareDutySuspenseUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postDeclareDutySuspendedPage(hasDutyToDeclare: Boolean): HttpRequestBuilder =
    http("Post Declare Duty Page")
      .post(s"$declareDutySuspenseUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", hasDutyToDeclare)
      .check(status.is(303))

  val getAmountOfVapingProductsMovedOrReceivedPage: HttpRequestBuilder =
    http("Get Amount Of Vaping Products Moved Or Received Page")
      .get(s"$enterDutySuspenseUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postAmountOfVapingProductsMovedOrReceivedPage(volumeReceived: String, volumeMoved: String): HttpRequestBuilder =
    http("Post Amount Of Vaping Products Moved Or Received Page")
      .post(s"$enterDutySuspenseUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("volumeReceived", volumeReceived)
      .formParam("volumeMoved", volumeMoved)
      .check(status.is(303))

  val getCheckYourAnswersPage: HttpRequestBuilder =
    http("Get Check Your Answers Page")
      .get(s"$CheckYourAnswersUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postCheckYourAnswersPage(): HttpRequestBuilder =
    http("Post Check Your Answers Page")
      .post(s"$CheckYourAnswersUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))

  val getReturnSubmittedPage: HttpRequestBuilder =
    http("Get Return Submitted Page")
      .get(s"$ReturnSubmittedUrl?period=#{period}")
      .check(status.is(200))

  val getViewYourReturnsPage: HttpRequestBuilder =
    http("Get View Your Returns Page")
      .get(ViewYourReturnsUrl)
      .check(status.is(200))
      .check(css("a.govuk-task-list__link[href*='before-you-start']", "href").saveAs("submitPeriodUrl"))
      .check(css("a.govuk-task-list__link[href*='view-your-returns/']", "href").saveAs("periodKey"))
      .check(regex("""before-you-start\?period=([A-Z0-9]+)""").saveAs("period"))

  val getViewIndividualReturnsPage: HttpRequestBuilder =
    http("Get View Individual Returns Page")
      .get(s"$vapingDutyBaseUrl#{periodKey}")
      .check(status.is(200))

  val getDeclareSpoiltProductsPage: HttpRequestBuilder =
    http("Get Declare Spoilt Products Page")
      .get(s"$declareSpoiltProductsUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postDeclareSpoiltProductsPage(hasSpoiltProducts: Boolean): HttpRequestBuilder =
    http("Post Declare Spoilt Products Page")
      .post(s"$declareSpoiltProductsUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", hasSpoiltProducts)
      .check(status.is(303))

  val getSelectSpoiltPeriodPage: HttpRequestBuilder =
    http("Get Select Spoilt Period Page")
      .get(s"$selectSpoiltPeriodUrl?period=#{period}")
      .check(status.is(200))
      .check(css("a.govuk-task-list__link", "href").saveAs("spoiltPeriodSelectUrl"))

  val getEnterSpoiltAmountPage: HttpRequestBuilder =
    http("Get Enter Spoilt Amount Page")
      .get(s"$vapingDutyBaseUrl#{spoiltPeriodSelectUrl}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postEnterSpoiltAmountPage(amount: String): HttpRequestBuilder =
    http("Post Enter Spoilt Amount Page")
      .post(s"$vapingDutyBaseUrl#{spoiltPeriodSelectUrl}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", amount)
      .check(status.is(303))

  // ---------- Over / Under Adjustment requests ----------
  val getDeclareAdjustmentsPage: HttpRequestBuilder =
    http("Get Declare Adjustments Page")
      .get(s"$declareAdjustmentsUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postDeclareAdjustmentsPage(hasAdjustments: Boolean): HttpRequestBuilder =
    http("Post Declare Adjustments Page")
      .post(s"$declareAdjustmentsUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", hasAdjustments)
      .check(status.is(303))

  val getSelectAdjustmentPeriodPage: HttpRequestBuilder =
    http("Get Select Adjustment Period Page")
      .get(s"$selectAdjustmentPeriodUrl?period=#{period}")
      .check(status.is(200))
      .check(
        css("a.govuk-task-list__link[href*='enter-over-or-under-declaration-amount']", "href")
          .saveAs("adjustmentPeriodSelectUrl")
      )

  val getEnterOverOrUnderDeclarationAmountPage: HttpRequestBuilder =
    http("Get Enter Over Or Under Declaration Amount Page")
      .get(s"$vapingDutyBaseUrl#{adjustmentPeriodSelectUrl}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postEnterOverOrUnderDeclarationAmountPage(adjustmentType: String, amount: String): HttpRequestBuilder = {
    val volumeField = if (adjustmentType == "overDeclared") "overDeclaredVolume" else "underDeclaredVolume"
    http("Post Enter Over Or Under Declaration Amount Page")
      .post(s"$vapingDutyBaseUrl#{adjustmentPeriodSelectUrl}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("adjustmentType", adjustmentType)
      .formParam(volumeField, amount)
      .check(status.is(303))
  }

  val getAdjustmentCheckYourAnswersPage: HttpRequestBuilder =
    http("Get Adjustment Check Your Answers Page")
      .get(s"$adjustmentCheckYourAnswersUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postAdjustmentCheckYourAnswersPage(addAnother: Boolean): HttpRequestBuilder =
    http("Post Adjustment Check Your Answers Page")
      .post(s"$adjustmentCheckYourAnswersUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", addAnother)
      .check(status.is(303))

  val getDeclareDutyCYAPage: HttpRequestBuilder =
    http("Get Add Another Spoilt Adjustment Page")
      .get(s"$DeclareDutyCYAUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  val getReturnDeclarationPage: HttpRequestBuilder =
    http("Get Return Declaration Page")
      .get(s"$ReturnDeclarationUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postReturnDeclarationPage(
    fullName: String,
    capacity: String,
    email: String
  ): HttpRequestBuilder =
    http("Post Return Declaration Page")
      .post(s"$ReturnDeclarationUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("fullName", fullName)
      .formParam("capacityInWhichSigned", capacity)
      .formParam("email", email)
      .check(status.is(303))

  val getDutySuspendedCYAPage: HttpRequestBuilder =
    http("Get Duty Suspended Check Your Answers Page")
      .get(s"$dutySuspendedCYAUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  val getViewPaymentsPage: HttpRequestBuilder =
    http("Get View Your Returns Page")
      .get(viewPaymentsUrl)
      .check(status.is(200))

  val getAdjustmentReasonPage: HttpRequestBuilder =
    http("Get Adjustment Reason Page")
      .get(s"$adjustmentReasonUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postAdjustmentReasonPage(
    reason: String
  ): HttpRequestBuilder =
    http("Post Adjustment Reason Page")
      .post(s"$adjustmentReasonUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("adjustmentReason", reason)
      .check(status.is(303))

  val getSpoiltAdjustmentCYAPage: HttpRequestBuilder =
    http("Get Spoilt Adjustment Check Your Answers Page")
      .get(s"$spoiltProductsCYAUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postSpoiltAdjustmentCYAPage(addAnother: Boolean): HttpRequestBuilder =
    http("Post Spoilt Adjustment Check Your Answers Page")
      .post(s"$spoiltProductsCYAUrl?period=#{period}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", addAnother)
      .check(status.is(303))

  val getAdjustmentCYAWithRemoveLink: HttpRequestBuilder =
    http("Get Adjustment Check Your Answers Page")
      .get(s"$adjustmentCheckYourAnswersUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())
      .check(
        css("a.govuk-link[href*='remove-adjustment']", "href")
          .saveAs("removeAdjustmentUrl")
      )

  val getRemoveAdjustmentPage: HttpRequestBuilder =
    http("Get Remove Adjustment Page")
      .get(s"$vapingDutyBaseUrl#{removeAdjustmentUrl}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postRemoveAdjustmentPage(remove: Boolean): HttpRequestBuilder =
    http("Post Remove Adjustment Page")
      .post(s"$vapingDutyBaseUrl#{removeAdjustmentUrl}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", remove)
      .check(status.is(303))

  val getSpoiltCYAWithRemoveLink: HttpRequestBuilder =
    http("Get Spoilt Adjustment Check Your Answers Page")
      .get(s"$spoiltProductsCYAUrl?period=#{period}")
      .check(status.is(200))
      .check(saveCsrfToken())
      .check(
        css("a.govuk-link[href*='remove-spoilt-adjustment']", "href")
          .saveAs("removeSpoiltAdjustmentUrl")
      )

  val getRemoveSpoiltAdjustmentPage: HttpRequestBuilder =
    http("Get Remove Spoilt Adjustment Page")
      .get(s"$vapingDutyBaseUrl#{removeSpoiltAdjustmentUrl}")
      .check(status.is(200))
      .check(saveCsrfToken())

  def postRemoveSpoiltAdjustmentPage(remove: Boolean): HttpRequestBuilder =
    http("Post Remove Spoilt Adjustment Page")
      .post(s"$vapingDutyBaseUrl#{removeSpoiltAdjustmentUrl}")
      .formParam("csrfToken", "#{csrfToken}")
      .formParam("value", remove)
      .check(status.is(303))

}
