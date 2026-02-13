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

import uk.gov.hmrc.performance.simulation.PerformanceTestRunner
import uk.gov.hmrc.perftests.vapingduty.VapingDutyRequests._
import uk.gov.hmrc.perftests.vapingduty.models.AuthUser

class VapingDutySimulation extends PerformanceTestRunner {

  setup(
    "vaping-duty-journey-user-with-enrolment-to-claim",
    "Vaping Duty Journey User With Enrolment To Claim"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.organisation()),
    getEnrolmentDoYouHaveAnApprovalIdPage,
    postEnrolmentDoYouHaveAnApprovalIdPage(true)
  )

  setup(
    "vaping-duty-journey-user-without-enrolment-to-claim",
    "Vaping Duty Journey User Without Enrolment To Claim"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.organisation()),
    getEnrolmentDoYouHaveAnApprovalIdPage,
    postEnrolmentDoYouHaveAnApprovalIdPage(false),
    getYouNeedAnApprovalIDPage
  )

  setup(
    "vaping-duty-journey-user-with-enrolment-already-claimed",
    "Vaping Duty Journey User With Enrolment Already Claimed"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.organisation(enrolled = true)),
    getAlreadyEnrolledPage
  )

  setup(
    "vaping-duty-journey-user-with-enrolment-already-claimed-accesses-index-page",
    "Vaping Duty Journey User With Enrolment Already Claimed Accesses Index Page"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.organisation(enrolled = true), vapingDutyBaseUrl),
    navigateToVapingDutyPage
  )

  setup(
    "vaping-duty-journey-user-with-agent-account",
    "Vaping Duty Journey User With Agent account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.agent()),
    getOrganisationAdministratorRequiredPage
  )

  setup(
    "vaping-duty-journey-user-with-individual-account",
    "Vaping Duty Journey User With Individual account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.individual()),
    getOrganisationAdministratorRequiredPage
  )

  setup(
    "Vaping-Duty-Journey-User-updates-contact-preference-to-post",
    "Vaping Duty Journey User updates contact preference to post"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(
      AuthUser.organisation(enrolled = true, AuthUser.contactPreferencePostIdentifier),
      howDoYouWantToBeContactedUrl
    ),
    getHowDoYouWantToBeContactedPage,
    postHowDoYouWantToBeContactedPage("post"),
    getConfirmYourPostalAddressPage,
    getPostalAddressConfirmationPage
  )

  setup(
    "Vaping-Duty-Journey-User-updates-contact-preference-to-email",
    "Vaping Duty Journey User updates contact preference to email"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(
      AuthUser.organisation(enrolled = true, AuthUser.contactPreferenceEmailIdentifier),
      howDoYouWantToBeContactedUrl
    ),
    getAuthSession,
    getHowDoYouWantToBeContactedPage,
    postHowDoYouWantToBeContactedPage("email"),
    getWhatEmailAddressToBeContactedPage,
    postWhatEmailAddressToBeContactedPage(emailAddressToVerify),
    getPasscodes(emailAddressToVerify),
    getEmailConfirmationCodePage,
    postEmailConfirmationCodePage(),
    getEmailAddressConfirmationPage,
    getHowDoYouWantToBeContactedPage,
    postHowDoYouWantToBeContactedPage("email"),
    getWhatEmailAddressToBeContactedPage,
    postWhatEmailAddressToBeContactedPage(emailAddressToVerify),
    getSubmitEmailConfirmationPage,
    postSubmitEmailConfirmationPage(),
    getEmailAddressConfirmationPage,
    getSubmitPreviousVerifiedEmailPage,
    postSubmitPreviousVerifiedEmailPage()
  )

  setup(
    "Vaping-Duty-Journey-User-Email-Account-Lock-Out",
    "Vaping Duty Journey User Email Account Lock Out"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(
      AuthUser.organisation(enrolled = true, AuthUser.contactPreferenceEmailIdentifier),
      howDoYouWantToBeContactedUrl
    ),
    getAuthSession,
    getHowDoYouWantToBeContactedPage,
    postHowDoYouWantToBeContactedPage("email"),
    getWhatEmailAddressToBeContactedPage,
    postWhatEmailAddressToBeContactedPage(emailAddressToVerify),
    getPasscodes(emailAddressToVerify),
    getEmailConfirmationCodePage,
    getAccountLockOutPage
  )

  runSimulation()
}
