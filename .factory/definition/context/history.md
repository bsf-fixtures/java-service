<!--
  What has gone wrong before, and what was learned. Seeded by setup from the
  mined corrections, then added to by the scheduled review.

  This file is for failures whose lesson could not be turned into a check. A
  correction that became a rule does not belong here: the rule is the record,
  and repeating it in prose means two things to keep in step.
-->

# History — bsf-fixtures/java-service

There is no history. The repository has a single squashed commit (`64218cf`,
"Upgrade the factory to e29d8e0 (#3)"), no other branch, and no application
code that could have gone wrong. No corrections were mined: this is a fresh
fixture rather than someone's working repository, so there are no agent
sessions to read, and `.factory/guardrails/candidates.json` is an empty list.

Every table below is empty because nothing has happened yet, not because
setup did not look. The scheduled review fills them as the factory runs.

## Lessons

| What happened | What to do instead | Where it came from |
| --- | --- | --- |
| — | — | — |

## Problem classes already eliminated

Kept so nobody proposes a rule that exists, and so a regression is
recognisable as one.

| Problem class | How it was eliminated | When |
| --- | --- | --- |
| — | — | — |

## Escalations

Work the factory could not finish, and why. A pattern across several of these
is a signal about the factory rather than about the work.

| Work item | Where it stopped | Cause |
| --- | --- | --- |
| — | — | — |

## The one thing setup already knows will bite

Not a lesson yet — nothing has failed — but the first predictable failure, so
it is recognisable when it happens rather than diagnosed from scratch:

the sandbox is tier `static`, so **no change made here can be run, tested or
seen**. The first work item that writes Java will produce a PR the verifier
cannot verify. That is expected and is the designed trigger for a sandbox
amendment: the build, test and run paths are listed under
`capabilities.not_exercised` in the manifest precisely so that touching them
opens an amendment episode rather than producing a confident unverified PR.
If instead the factory merges that PR claiming verification, the fault is in
the gates reading the tier, and it belongs in Lessons above.
