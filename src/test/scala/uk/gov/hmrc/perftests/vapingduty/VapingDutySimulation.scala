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
    GetEnrolmentDoYouHaveAnApprovalIdPage,
    PostEnrolmentDoYouHaveAnApprovalIdPage(true)
  )

  setup(
    "vaping-duty-journey-user-without-enrolment-to-claim",
    "Vaping Duty Journey User Without Enrolment To Claim"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.organisation()),
    GetEnrolmentDoYouHaveAnApprovalIdPage,
    PostEnrolmentDoYouHaveAnApprovalIdPage(false),
    GetYouNeedAnApprovalIDPage
  )

  setup(
    "vaping-duty-journey-user-with-enrolment-already-claimed",
    "Vaping Duty Journey User With Enrolment Already Claimed"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.organisation(enrolled = true)),
    GetAlreadyEnrolledPage
  )

  setup(
    "vaping-duty-journey-user-with-enrolment-already-claimed-accesses-index-page",
    "Vaping Duty Journey User With Enrolment Already Claimed Accesses Index Page"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.organisation(enrolled = true), baseUrl),
    navigateToVapingDutyPage
  )

  setup(
    "vaping-duty-journey-user-with-agent-account",
    "Vaping Duty Journey User With Agent account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.agent()),
    GetEnrolmentOrganisationSignInPage
  )

  setup(
    "vaping-duty-journey-user-with-individual-account",
    "Vaping Duty Journey User With Individual account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.individual()),
    GetEnrolmentOrganisationSignInPage
  )

  runSimulation()
}
