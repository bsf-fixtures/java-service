<!--
  What has gone wrong before, and what was learned. Seeded by setup from the
  mined corrections, then added to by the scheduled review.

  This file is for failures whose lesson could not be turned into a check. A
  correction that became a rule does not belong here: the rule is the record,
  and repeating it in prose means two things to keep in step.
-->

# History — java-service

## Lessons

| What happened | What to do instead | Where it came from |
| --- | --- | --- |

Nothing yet. There is nothing to seed this from: the repository has a single
squashed commit (`5b76e35`, 2026-11-21), no issues or pull requests predating
it that describe a correction, and no mined agent sessions were available to
this setup round. The first entries will come from the scheduled review.

## Problem classes already eliminated

Kept so nobody proposes a rule that exists, and so a regression is
recognisable as one.

| Problem class | How it was eliminated | When |
| --- | --- | --- |
| Tests that pass or fail depending on the day they run | The overdue rule takes the day as a parameter (`Loan.overdueOn(LocalDate)`, `Loan.daysLate(LocalDate)`) and the endpoint accepts `?on=`, so every test states an exact date and an exact expected list. `LocalDate.now()` survives in one place, as the default for a missing parameter. | Present in the repository's first commit, `5b76e35` (2026-11-21); stated as the intent in README.md and in `Loan`'s Javadoc |
| An off-by-one on the due day — treating a loan as late on the day it is due | `overdueOn` is `today.isAfter(due)`, and `LoanTest.isNotOverdueOnItsDueDay` pins it. | Same commit |

## Escalations

Work the factory could not finish, and why. A pattern across several of these
is a signal about the factory rather than about the work.

| Work item | Where it stopped | Cause |
| --- | --- | --- |

None. This is the factory's first setup on this repository; no work items have
run.
