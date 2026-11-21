<!--
  How to build, run, test and verify this app. Written by setup's phase 2,
  with every command tested rather than copied from a README.

  This is the most load-bearing context file: a verifier agent that cannot
  start the app cannot verify anything, and then the factory produces
  confident unverified PRs.
-->

# Runbook — java-service

## Build

```bash
mvn -B -ntp -DskipTests package
```

Run the install command first in a fresh sandbox — it prepares the Java
truststore the build needs and warms the local repository:

```bash
TS=/tmp/bsf-truststore.p12; CA="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")/lib/security/cacerts"; if [ ! -s "$TS" ] && [ -r "$CA" ]; then cp "$CA" "$TS"; fi; if [ ! -s "$TS" ]; then D="$(mktemp -d)"; csplit -sz -f "$D/c" -b "%03d.pem" /etc/ssl/certs/ca-certificates.crt '/BEGIN CERTIFICATE/' '{*}'; for f in "$D"/c*.pem; do keytool -importcert -noprompt -trustcacerts -alias "$(basename "$f" .pem)" -file "$f" -keystore "$TS" -storetype PKCS12 -storepass changeit >/dev/null 2>&1 || true; done; rm -rf "$D"; fi; mvn -B -ntp -DskipTests dependency:resolve
```

Notes, all measured in the sandbox rather than copied from the README:

- Maven and a JDK are already on `PATH`; nothing is installed by these
  commands except the project's own dependencies.
- The two settings in `MAVEN_OPTS` are not optional in an agent container:
  `~/.m2` there is root-owned (Maven cannot create its default local
  repository) and the JDK's `lib/security/cacerts` is a dangling symlink, so
  every download fails with `trustAnchors parameter must be non-empty` until a
  valid truststore is supplied. Both are set by the sandbox, so plain `mvn …`
  commands work; do not unset them.
- Timings from a cold local repository: install 44s (35s of that is building
  the truststore, skipped where the JDK's own is readable), build 5s, test 3s.
  Warm, install is ~9s.
- `-q` is deliberately not used, unlike the README: the output is how a failing
  step is told from a silent one.
- Output goes to `target/`, which is git-ignored; `mvn package` repackages the
  Spring Boot fat jar as `target/loans-0.1.0.jar`.

## Run

```bash
mvn -B -ntp spring-boot:run
```

The app is then at http://127.0.0.1:8080/loans/overdue?on=2026-11-20.

It answers about four seconds after the command starts, with a warm local
repository. `GET /` is a 404 — the only mapping is `/loans/overdue`. The
`on` parameter is an ISO date (`yyyy-MM-dd`) and is optional; without it the
endpoint uses the current day.

Use `127.0.0.1`, not `[::1]`: embedded Tomcat binds IPv4 (`0.0.0.0`), and a
client that resolves `localhost` to `::1` first does not reach it.

## Test

```bash
mvn -B -ntp test
```

Three JUnit 5 tests, about three seconds:

- `LoanTest.isNotOverdueOnItsDueDay` and `LoanTest.countsWholeDaysLate` cover
  the rule, including the boundary the README calls out.
- `LoanControllerTest.listsOverdueLoansMostOverdueFirst` calls the controller
  directly — there is no Spring context in the tests and no MockMvc.

Both the build and the test command fail outside this repo (`there is no POM
in this directory`), so a pass means this code was built and these tests ran.

## Verify

How to see for yourself that the app works, as a developer would:

1. `mvn -B -ntp test` — expect `Tests run: 3, Failures: 0, Errors: 0`.
2. `mvn -B -ntp spring-boot:run` in one shell, then in another:
   `curl -s 'http://127.0.0.1:8080/loans/overdue?on=2026-11-20'`. Expect
   exactly:
   `[{"title":"The Dispossessed","borrower":"ada","daysLate":10},{"title":"Invisible Cities","borrower":"alan","daysLate":2}]`
   — two loans, most overdue first, and "Middlemarch" (due 2026-11-25) absent.
3. Check the boundary over HTTP, not only in the unit test:
   `curl -s 'http://127.0.0.1:8080/loans/overdue?on=2026-11-10'` returns `[]`,
   because a loan is not overdue on its due day.
4. For a visual check, open the same URL in a browser: Chromium renders the
   JSON response, which is what the sandbox screenshots.
5. Stop the app with `Ctrl-C`, or by killing the process group of the
   `spring-boot:run` command; it forks a JVM, so killing only the Maven process
   can leave the app on port 8080.

## Seed data

```bash
# none
```

There is nothing to seed. The three loans are a hard-coded `List.of` in
`LoanController`, so the data is whatever the code says it is, and changing it
means editing that list.

## Commands that change persistent state

These are kept out of the build path on purpose. Each one writes to something
that outlives the process, so running it against the wrong target is the kind
of mistake that cannot be undone by reverting a commit.

| Command | Writes to | Only when |
| --- | --- | --- |

None. The app has no database, no file storage and no outbound calls; every
command above writes only to `target/` and to the Maven local repository.

## Services this app needs

| Service | How it starts | Reached at |
| --- | --- | --- |

None. The sandbox starts no containers.

## The sandbox's environment

Built from this list, and from nothing else. It does not inherit a
developer's environment and it does not read any `.env` file, because a
documented build command that runs a migration would otherwise migrate
whatever a developer's credentials happen to point at.

| Variable | Value in the sandbox |
| --- | --- |
| `SERVER_PORT` | `8080` — the port Spring Boot binds, made explicit because `app_url` and this runbook depend on it. The app itself reads no configuration. |
| `MAVEN_OPTS` | `-Djavax.net.ssl.trustStore=/tmp/bsf-truststore.p12 -Djavax.net.ssl.trustStoreType=PKCS12 -Djavax.net.ssl.trustStorePassword=changeit -Dmaven.repo.local=/tmp/bsf-m2` — build configuration only. `changeit` is the stock Java truststore password and that file holds public CA certificates; it is not a credential. |

Plus what the sandbox always passes through: `PATH`, `HOME`, `TMPDIR`,
`LANG`, `CI`, `RUNNER_TEMP`. Note that `JAVA_HOME` is *not* among them — Maven
resolves the JDK from `java` on `PATH`, which may be a newer JDK than the
Temurin 17 that CI pins. The build targets release 17 either way.
