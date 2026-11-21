<!--
  How to build, run, test and verify this app. Written by setup's phase 2,
  with every command tested rather than copied from a README.

  This is the most load-bearing context file: a verifier agent that cannot
  start the app cannot verify anything, and then the factory produces
  confident unverified PRs.
-->

# Runbook — bsf-fixtures/java-service

**There is no application here yet.** Every command below is empty because
the repository contains no build file and no source tree — not because setup
failed to find one. Read the "What a first change must add" section at the
bottom before writing the first line of Java.

The sandbox tier is `static`. A verifier working in this repository **cannot
run anything**, and must not claim it did.

## Build

```bash
# nothing to build
```

No `pom.xml`, `build.gradle`, `build.gradle.kts`, `settings.gradle`, `gradlew`
or `mvnw` exists, and there is no `src/` tree. `commands.build` in the sandbox
manifest is `null`.

The toolchain is nevertheless installed on the runner and was checked:
Temurin JDK **25.0.5** (`JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64`), Apache
Maven **3.9.15**, Node 24.22.3, Python 3.14.4, Docker 29.4.2. `gradle` is on
`PATH` but `gradle -v` fails without a project.

## Run

```bash
# nothing to run
```

There is no app URL. Nothing listens on any port, so there is no page to open
and no screenshot to take.

## Test

```bash
# no tests
```

No `src/test/` tree, no test framework on any dependency list, and no
dependency list. `commands.test` is `null`, which is a fact about the
repository rather than a gap in the manifest. `.factory/guardrails/coverage.json`
carries a 0% floor to match.

## Verify

How to see for yourself that the app works, as a developer would — there is no
app, so this is instead how to see that the *factory installation* is sound.
All three were run and all three pass:

1. **The factory is intact.**

   ```bash
   .factory/bin/bsf doctor --root .
   ```

   Expect `Nothing broken.` and `installed=true`. It notes that secrets,
   variables and the data ref were not checked without `--repo OWNER/REPO`.

2. **The artifacts that should agree, agree.**

   ```bash
   .factory/bin/bsf consistency --root .
   ```

   1 of 6 checks applies here; the other five need a `package.json`, a
   `.env.example` or a runbook in the repo. The one that applies reports
   `env_no_example`: 33 environment variables are read and no `.env.example`
   names them. **That is expected and should not be "fixed"** — all 33 are the
   factory's own GitHub Actions secrets and variables (`ANTHROPIC_API_KEY`,
   `FACTORY_APP_CLIENT_ID`, `FACTORY_APP_PRIVATE_KEY`, `FACTORY_SANDBOX_ENV`,
   the `GH_AW_*` set, and so on), read by workflows rather than by an
   application.

3. **The sandbox manifest validates.**

   ```bash
   .factory/bin/bsf sandbox check --file .factory/sandbox/manifest.json
   ```

   Expect `valid sandbox manifest, tier static`.

There is nothing else to verify. A change to a Markdown or YAML file can be
read; it cannot be executed.

## Seed data

```bash
# no seed data
```

Nothing persists anything, so there is nothing to seed.

## Commands that change persistent state

These are kept out of the build path on purpose. Each one writes to something
that outlives the process, so running it against the wrong target is the kind
of mistake that cannot be undone by reverting a commit.

| Command | Writes to | Only when |
| --- | --- | --- |
| — (the application has none) | — | — |
| `.factory/bin/bsf practice add "…"` | Opens a PR against this repo adding a line to `practices.md`. | A human asked for a new practice. Not part of any build or test. |
| `.factory/bin/bsf sync-settings --apply` | Branch protection on this repo. | Installing or repairing the factory, by a human. Adds checks, never removes. |
| `.factory/bin/bsf store-media`, `bsf rollup`, `bsf migrate --apply` | The factory's data and media refs. | Driven by the factory's own workflows, never by hand during product work. |

## Services this app needs

| Service | How it starts | Reached at |
| --- | --- | --- |
| — (none) | — | — |

Nothing in the repository connects to a database, cache, broker, object store
or mail catcher, so the sandbox declares no services. The first code that does
needs one added to the manifest — an official image on a loopback port with
throwaway credentials — before it can be exercised.

## The sandbox's environment

Built from this list, and from nothing else. It does not inherit a
developer's environment and it does not read any `.env` file, because a
documented build command that runs a migration would otherwise migrate
whatever a developer's credentials happen to point at.

| Variable | Value in the sandbox |
| --- | --- |
| — (the environment is empty) | — |

No application code reads any environment variable, because there is no
application code. The 33 variables `bsf consistency` reports are the
repository owner's GitHub Actions secrets, consumed by the factory's own
workflows; none of them is placed in the sandbox, and none should be. Beyond
the package registries the sandbox reaches no host (`egress` is empty) and
fakes no third party (`stubs` is empty).

The sandbox does pass through `PATH`, `HOME`, `TMPDIR`, `LANG`, `CI` and
`RUNNER_TEMP`, which is how `java` and `mvn` stay reachable.

## What a first change must add

The sandbox becomes useful — `build_and_test`, and then `renders` — as soon as
there is something to build. A change that touches any of these paths makes
this runbook and the manifest out of date, and should carry the sandbox
amendment with it:

| Adding | Makes possible | Also needs |
| --- | --- | --- |
| `pom.xml` (or the Gradle equivalent) and `src/main/**` | A real `install` and `build` command; tier `build_and_test`. | Choosing Maven or Gradle, and a package root. Maven 3.9.15 and JDK 25 are already installed. |
| `src/test/**` | A real `test` command; the coverage half of gate 2. | A coverage floor in `.factory/guardrails/coverage.json` above 0. |
| An HTTP entry point and `application.yml`/`application.properties` | A real `run` command, an `app_url`, and tier `renders` — the verifier can then open the app and screenshot it. | Binding to `127.0.0.1` on a fixed port, and `self`-kind env entries for the port and origin. |
| A repository, entity or migration | Exercising anything behind a datastore. | A throwaway service in the manifest on a loopback port, and a `service`-kind variable pointing at it. |
| A linter config (Checkstyle, SpotBugs, PMD, Spotless) | Gate 1 having something to compare. | Re-baselining `.factory/guardrails/baselines.json`, which currently has an empty rules map. |
