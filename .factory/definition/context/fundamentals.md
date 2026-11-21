<!--
  The conventions no check enforces. Everything here is something a script
  could not decide, which is why it is prose.

  The bar for a line in this file: it has to be a judgement. If a static rule
  could catch it, it belongs in .factory/guardrails/ instead, because a rule
  is reproducible and prose is something agents argue with.

  Each line carries where it came from, so a line nobody can justify can be
  removed later.
-->

# Fundamentals — bsf-fixtures/java-service

## Dominant patterns

For each recurring concern, the pattern most of this codebase already uses.
Follow it rather than introducing a second way of doing the same thing.

**There are none for the application.** No Java has been written here, so
there is no error-handling style, no layering, no naming convention, no
dependency-injection approach and no test style to follow. The first change
that writes code is choosing all of them, and should say in its PR which it
chose — the next change will follow it, and the scheduled review will record
it here.

| Concern | The pattern here | Source |
| --- | --- | --- |
| — | — | — |

The patterns that *do* hold are about the factory's own installation, not the
product, and they bind any change that touches those files:

| Concern | The pattern here | Source |
| --- | --- | --- |
| Changing a role's behaviour | Edit the prompt in `.github/workflows/factory-role-<role>.md` and recompile. The `*.lock.yml` beside it is generated — its first lines say `DO NOT EDIT`, and `factory-audit` fails when a lock file has drifted from its source. | `.github/workflows/factory-role-spec.lock.yml` header; `.github/workflows/factory-audit.yml` |
| Where a role's prompt lives | With the workflow, never in `.factory/definition/roles/*.yaml`. The YAML records the role's shape for the dashboard and the policy check; keeping the prompt in both would let the two drift. | `.factory/definition/roles/spec.yaml`, `prompt:` |
| Changing the definition | `.factory/definition/**`, `.factory/guardrails/**`, `.github/workflows/**` and `.factory/bin/**` are protected paths: a change needs a trusted human's approval, not an agent's. | `.factory/definition/policy.yaml`, `paths.definition` / `paths.protected` |
| Upgrading the runner | `.factory/bin/` moves as a unit — `bsf`, `VERSION` and `runner.tgz` together. It is the program every gate runs, and jobs take it from the default branch so a PR cannot grade itself with a modified copy. | `.factory/definition/policy.yaml` comment on `.factory/bin/**`; `.github/workflows/factory-setup-trial.yml` |

## Product intent

What this product is trying to be, where that would change a decision an
agent has to make.

The README states the whole of it: *"Build Software Factory: Fixture of a Java
service to install factory on."* So the intent that is actually recorded is
**the repository exists to host a Java service and to exercise the factory on
it** — the service itself is yet to be written, and nothing in the repository
says what it should do.

Decisions that follow from that, and the ones that do not:

- **It is a service, not a library or a CLI.** That is the one shape the name
  and the README commit to, so a change should assume something long-running
  that answers over the network, and should bind it to `127.0.0.1` on a fixed
  port so the sandbox can reach it.
- **It is Java.** The runner has Temurin 25 and Maven 3.9.15 installed and
  ready. Nothing commits the repo to Maven over Gradle, or to any framework.
- **Nothing states a domain, a user, an API shape, a persistence choice or a
  deployment target.** An agent that needs one of these should take it from
  the work item, and say in the PR that it was the work item's choice rather
  than the repository's — do not infer a product from the repository name.

## Taste

Things a reviewer would ask for that no rule states.

Nothing has been observed from the code, because there is no code, and no
practice has been stated yet (`.factory/definition/context/practices.md` is
an empty table and `.factory/guardrails/candidates.json` is an empty list).
Two lines survive the bar anyway, both from what setup found rather than from
preference:

- **Do not build structure the work item did not ask for.** On a repository
  this empty, every change is tempting to turn into a scaffold. A package
  tree, a config layer and an abstraction with one implementation are all much
  harder to remove later than to add now, and the second caller is the moment
  to generalise. *(From setup: there is nothing here to constrain a change, so
  nothing but judgement will.)*
- **A change that adds code must leave the sandbox able to run it.** Adding a
  build file, a test tree or an entry point without amending the sandbox
  manifest leaves the factory confidently unable to verify anything it writes
  next. The runbook's "What a first change must add" table says what each
  addition requires. *(From setup: the sandbox is `static`, so today nothing
  the factory produces here can be executed.)*

## Deliberate exceptions

Places that look like they break a rule and should stay as they are, with the
reason. Without this section, cleanup keeps rediscovering them.

| Place | Looks wrong because | Why it stays |
| --- | --- | --- |
| `bsf consistency` reports `env_no_example`: 33 environment variables read with no `.env.example` naming them. | It reads as undocumented configuration. | All 33 are the factory's own GitHub Actions secrets and variables (`ANTHROPIC_API_KEY`, `FACTORY_APP_CLIENT_ID`, `FACTORY_APP_PRIVATE_KEY`, `FACTORY_SANDBOX_ENV`, the `GH_AW_*` set…), read by workflows and by `bsf`, never by an application. Writing a `.env.example` for them would document the owner's credentials as if they were app config. Expect this finding until real application configuration exists. |
| `.factory/guardrails/baselines.json` has an empty `rules` map, and `coverage.json` has a 0% floor. | It looks like the guardrails were never set up. | There is no linter configured and no test to cover. Both are correct values for an empty repository, and both must be raised deliberately — by re-baselining — when a linter and tests arrive, not quietly. |
| The sandbox manifest has `null` for all four commands and an empty `env`. | It looks like setup gave up. | It is the measured truth: there is no build file, no test tree, no entry point, and no application code that reads a variable. The alternative — commands guarded so they exit 0 without doing anything — would make gate 2 pass vacuously on every change. See `episode` attempt 5 in the manifest. |

## Known mismatches to settle before the first Java lands

Not exceptions — things that are simply not yet right for a Java repository,
recorded so the first change does not have to rediscover them.

| What | Why it matters |
| --- | --- |
| `policy.yaml`'s `paths.tests` globs are `**/*.test.*`, `**/*.spec.*`, `**/test_*.py`, `tests/**` — all JavaScript- or Python-shaped. | Maven and Gradle put tests in `src/test/java/**/*Test.java`, which none of those patterns match, so the policy check would not recognise a Java test as a test. Needs a trusted human's approval to change, so raise it rather than working around it. |
| `paths.guardrails` lists `.eslintrc*`, `.semgrep.yml`, `pyproject.toml#tool.ruff` and friends — no Java linter config. | A `checkstyle.xml` or `spotbugs-exclude.xml` added later would not be treated as a guardrail, so weakening it would not be caught. |
