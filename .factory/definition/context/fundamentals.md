<!--
  The conventions no check enforces. Everything here is something a script
  could not decide, which is why it is prose.

  The bar for a line in this file: it has to be a judgement. If a static rule
  could catch it, it belongs in .factory/guardrails/ instead, because a rule
  is reproducible and prose is something agents argue with.

  Each line carries where it came from, so a line nobody can justify can be
  removed later.
-->

# Fundamentals — java-service

## Dominant patterns

For each recurring concern, the pattern most of this codebase already uses.
Follow it rather than introducing a second way of doing the same thing.

| Concern | The pattern here | Source |
| --- | --- | --- |
| Time | A rule takes the day it is judged against as a parameter (`overdueOn(LocalDate today)`, `daysLate(LocalDate today)`) and never reads the clock. `LocalDate.now()` appears exactly once, in the controller, as the default for a missing `?on=`. | `src/main/java/fixtures/loans/Loan.java`, `LoanController.java`, README.md |
| Domain types | A `record`, not a class with getters: `Loan`, and the response type `OverdueLoan`. Behaviour lives on the record (`overdueOn`, `daysLate`) rather than in a service class. | `Loan.java`, `LoanController.java` |
| HTTP layer | `@RestController` with `@GetMapping`, returning the object and letting Jackson serialise it. No `ResponseEntity`, no `@ResponseStatus`, no exception handler, no service or repository layer between the controller and the data. | `LoanController.java` |
| Response shapes | The response type is a `record` nested in the controller that returns it (`LoanController.OverdueLoan`), not a separate DTO package. | `LoanController.java` |
| Collections | Immutable throughout: `List.of` for the data, a stream with `.filter`/`.map`/`.sorted`/`.toList()` for the work. Nothing mutable is exposed or accumulated. | `LoanController.java` |
| Sorting | An explicit comparator written at the call site (`(a, b) -> Long.compare(b.daysLate(), a.daysLate())`) rather than making the domain type `Comparable`. | `LoanController.java` |
| Tests | JUnit 5 with statically imported `assertEquals`/`assertFalse`, instantiating the class under test directly (`new LoanController()`). No `@SpringBootTest`, no MockMvc, no mocks, no AssertJ — although `spring-boot-starter-test` would provide all of them. | `src/test/java/fixtures/loans/LoanTest.java`, `LoanControllerTest.java` |
| Test naming | A sentence that states the rule: `isNotOverdueOnItsDueDay`, `countsWholeDaysLate`, `listsOverdueLoansMostOverdueFirst`. Package-private classes and methods, no `public`. | both test files |
| Build | Maven with `spring-boot-starter-parent` managing every version: no `<version>` on any dependency, one `<java.version>` property. | `pom.xml` |
| Comments | One Javadoc line per type or method, and only where it says something the signature does not — why overdue takes a day, what "whole days late" means. No comments inside method bodies. | all files under `src/main/java` |

## Product intent

A library's overdue-loans endpoint, kept deliberately small. The one product
decision that is written down, in both the README and the code's own comments:
**overdue is decided against a day passed in, never against the clock, so the
rule is testable — and a loan is not overdue on its due day.** A change that
makes the result depend on the current time, or that makes the due day count
as late, breaks the thing this service exists to demonstrate.

The repo describes itself as a fixture for the software factory's Java rule
pack, so it is also a place where build and test times are measured. That is a
reason to keep the build plain Maven and to avoid adding plugins the app does
not need.

## Taste

Things a reviewer would ask for that no rule states.

- Pass the day in. If a new rule needs "today", take it as a parameter and let
  the caller at the HTTP edge supply it.
- Prefer a record and a method on it to a service class. There is no service
  layer here, and adding one for a single call would be the larger change.
- Test the thing directly. Reach for a Spring context only when the change is
  about Spring wiring — serialisation, parameter binding, status codes.
- Keep the tests arithmetical: a fixed date in, an exact list out. No
  tolerance, no "contains".
- Do not put a version number in `pom.xml` where the parent already manages
  one, and do not add a dependency the code does not use.
- Keep the endpoint's JSON flat: title, borrower, and a number. No envelope,
  no nulls.
- One Javadoc line that explains why; delete anything that restates the
  signature.

## Deliberate exceptions

Places that look like they break a rule and should stay as they are, with the
reason. Without this section, cleanup keeps rediscovering them.

| Place | Looks like | Why it stays |
| --- | --- | --- |
| The hard-coded `List.of(...)` of three loans in `LoanController` | Data in a controller, with no repository | There is no persistence in this repo at all. Introducing a repository interface, or a database, to hold three literals would be a much bigger change than the code it replaces, and nothing has asked for it. |
| `LocalDate.now()` in `LoanController.overdue` | The clock being read, which the rule forbids | The default for a missing query parameter has to come from somewhere. It is at the HTTP edge, in one place, and the rule underneath it still takes the day as a parameter. |
| One package, `fixtures.loans`, for controller, domain and entry point | No layering | Three classes. A `domain`/`web` split here would be structure without content; the feature map says where the second feature goes. |
| `LoanControllerTest` calls `new LoanController()` instead of going through HTTP | The endpoint is never tested as an endpoint | The test is about the overdue rule's output and ordering, which is what changes. The gap is real and is recorded in the sandbox manifest as something verification covers by running the app and fetching the URL instead. |
| Tests depend on fixed dates in 2026 | Dates that will look stale | They are the point: fixed dates are what makes the expected output exact. They do not expire, because nothing compares them to the clock. |
