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

class VapingDutySimulation extends PerformanceTestRunner {

  setup(
    "vaping-duty-journey-access-vpd-with-organisation-account",
    "Vaping Duty Journey Access Vpd With Organisation Account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage,
    navigateToVapingDutyPage,
    GetEnrolmentApprovalPage,
    PostEnrolmentApprovalPage(true),
    GetChangeEnrolmentApprovalPage,
    PostChangeEnrolmentApprovalPage(true)
  )

  setup(
    "vaping-duty-journey-access-vpd-without-organisation-account",
    "Vaping Duty Journey Access Vpd Without an Organisation Account"
  ).withRequests(
    getAuthLoginPage,
    postAuthLoginPage,
    navigateToVapingDutyPage,
    GetEnrolmentApprovalPage,
    PostEnrolmentApprovalPage(false),
    GetChangeEnrolmentApprovalPage,
    PostChangeEnrolmentApprovalPage(false),
    GetEnrolmentOrganisationSignInPage
  )

  runSimulation()
}
