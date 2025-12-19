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
    postAuthLoginPage(AuthUser.nonEnrolled("Organisation")),
    GetEnrolmentApprovalPage,
    PostEnrolmentApprovalPage(true)
  )

  setup(
    "vaping-duty-journey-user-without-enrolment-to-claim",
    "Vaping Duty Journey User Without Enrolment To Claim"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.nonEnrolled("Organisation")),
    GetEnrolmentApprovalPage,
    PostEnrolmentApprovalPage(false),
    GetVPDIDApprovalRequiredPage
  )

  setup(
    "vaping-duty-journey-user-with-enrolment-already-claimed",
    "Vaping Duty Journey User With Enrolment Already Claimed"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.enrolled("Organisation")),
    GetAlreadyEnrolledPage
  )

  setup(
    "vaping-duty-journey-user-with-agent-account",
    "Vaping Duty Journey User With Agent account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.nonEnrolled("Agent")),
    GetEnrolmentOrganisationSignInPage
  )

  setup(
    "vaping-duty-journey-user-with-individual-account",
    "Vaping Duty Journey User With Individual account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage(AuthUser.nonEnrolled("Individual")),
    GetEnrolmentOrganisationSignInPage
  )

  runSimulation()
}
