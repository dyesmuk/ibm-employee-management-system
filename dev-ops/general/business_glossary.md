# IBM Freshers — IT Industry Glossary
### Techno-business vocabulary, registers & expressions used in the Indian IT industry

---

> **How to use this guide:** Terms marked ⚠️ are the ones that most commonly trip up freshers — either because they have dual meanings, carry cultural nuance, or are used so casually that no one thinks to explain them.

---

## ⚠️ Terms That Trip Up Freshers Most

| Term | What freshers think it means | What it actually means in context |
|---|---|---|
| **Bandwidth** | Network speed/capacity | ALSO: a person's available time. "I don't have the bandwidth for this sprint." |
| **EOD / EOM / EOQ** | — | End of Day / End of Month / End of Quarter. Deadline shorthand in every email. |
| **Escalation** | Conflict or complaint | A formal, professional step to raise an unresolved issue to higher authority. Not rude — expected. |
| **LGTM** | — | "Looks Good To Me." Approval signal in code reviews and Slack. |
| **Sync** | Data synchronisation | Also: a short meeting to align. "Quick sync at 3?" |
| **Grooming** | Personal grooming | Backlog grooming — reviewing and estimating upcoming tasks before a sprint. |
| **Runway** | Airport runway | How many sprints/weeks a team can sustain before needing more resources. |
| **Ping** | Network ping command | To send someone a quick message. "Ping me on Teams when it's done." |
| **North star** | Navigation | The overarching vision or goal guiding a product or team. |
| **Loop in** | — | To include someone in a conversation or email thread. |

---

## 🖥️ Technical Terms

### Architecture & Design

**Presentation layer**
The front-end / UI part of an application — what the user sees and interacts with. Preferred over "front end" in formal client discussions because it signals systems thinking.
> *"The presentation layer team will handle the React components."*

**Business logic layer**
The middle tier that processes rules, calculations, and decisions — sits between the presentation and data layers.
> *"Discount calculations belong in the business logic layer, not the UI."*

**Data layer**
The part of the system responsible for storage, retrieval, and persistence of data.
> *"We need to optimise queries at the data layer before going live."*

**Monolith**
A single, unified application where all components are tightly coupled. The opposite of microservices.
> *"The legacy monolith makes it hard to deploy individual features without downtime."*

**Microservices**
An architectural style where a large application is broken into small, independently deployable services.
> *"We're migrating the monolith to a microservices architecture over three quarters."*

**API (Application Programming Interface)**
A contract that defines how two systems communicate with each other.
> *"The mobile app calls the payment API to process every transaction."*

**Tech stack**
The full combination of technologies — languages, frameworks, databases, and cloud services — used to build a product.
> *"Our tech stack is React, Node.js, PostgreSQL, deployed on IBM Cloud."*

**Cloud-native**
Designed specifically to run on cloud platforms, taking advantage of scalability, containers, and managed services.
> *"IBM Cloud Pak is built for cloud-native deployments."*

---

### Performance & Infrastructure

**Input / Output / Throughput (I/O)**
I/O refers to data flowing into and out of a system. Throughput is how much data is processed per unit time — a key performance benchmark.
> *"High disk I/O is bottlenecking our throughput on the nightly ETL job."*

**Latency**
The delay between a request being made and the response arriving. Lower latency = faster system.
> *"API latency spiked to 800ms during peak load — we need to investigate."*

**Bandwidth (technical)**
The maximum data transfer capacity of a network connection. About volume, not just speed.
> *"We need higher bandwidth to stream large model files to the client's data centre."*

**Uptime / Downtime**
Uptime is how long a system has been running without failure. Downtime is when it's unavailable.
> *"Our SLA requires 99.9% uptime — that's less than 9 hours of downtime per year."*

**Scalability**
A system's ability to handle increased load — either by adding more machines (horizontal) or bigger machines (vertical).
> *"The architecture needs to be scalable to handle 10x the users during peak season."*

**Infra / Infrastructure**
Servers, networks, storage, and cloud resources that support applications.
> *"The infra team needs to provision a new environment for QA testing."*

---

### Code & Development

**Repo / Repository**
A version-controlled codebase, usually on Git (GitHub, GitLab, or Bitbucket). Every project has one.
> *"Clone the repo first, then raise a PR with your changes."*

**PR / Pull Request**
A request to merge your code changes into the main branch. Requires peer review and approval before merging.
> *"Raise a PR and assign it to your tech lead for review."*

**Boilerplate**
Reusable template code included with minimal modification across projects.
> *"Use the IBM project boilerplate for setup — don't reinvent the wheel."*

**Technical debt**
Shortcuts or quick fixes taken during development that create more work in the future — like financial debt, it accrues interest.
> *"We need a sprint dedicated to paying down technical debt before the next release."*

**Tree shaking**
A build-time optimisation that removes unused code from the final production bundle, making it smaller and faster.
> *"With tree shaking enabled, we cut the frontend bundle size by 40%."*

**Refactoring**
Restructuring existing code without changing its external behaviour — to improve readability, performance, or maintainability.
> *"We're refactoring the authentication module this sprint — no new features, just cleanup."*

**Dependency**
An external library, package, or service that your code relies on to function.
> *"We have a dependency on the third-party payment SDK — check their changelog for breaking changes."*

**Debugging**
The process of identifying and fixing errors or unexpected behaviour in code.
> *"I've been debugging the session timeout issue since morning — it's a race condition."*

---

### DevOps & Deployment

**CI/CD (Continuous Integration / Continuous Delivery)**
Automated pipelines that build, test, and deploy code with minimal manual intervention.
> *"The CI/CD pipeline runs unit tests on every PR — broken tests block the merge."*

**Containerisation**
Packaging an application and its dependencies into a container (e.g. Docker) so it runs consistently across environments.
> *"We containerised the service to eliminate 'works on my machine' problems."*

**Environment (Dev / QA / Staging / Prod)**
Separate deployment contexts for different stages of development. Never test in Prod.
- **Dev** — where developers write and test code locally
- **QA** — where testers verify functionality
- **Staging** — a production-like environment for final checks
- **Prod** — the live system that real users interact with

> *"The bug reproduces in QA but not in Dev — compare the environment configs."*

**Deployment**
The process of releasing code from a development environment to a higher environment (usually Prod).
> *"Deployment is scheduled for Saturday midnight to minimise user impact."*

**Rollback**
Reverting a system to a previous stable version after a failed deployment.
> *"The new release caused errors — we did an emergency rollback within 15 minutes."*

---

## 💼 Business Terms

**SOW (Statement of Work)**
A formal document defining the scope, deliverables, timelines, and cost of a project. Legally binding.
> *"The SOW was signed last week — the project is now officially kicked off."*

**SLA (Service Level Agreement)**
A commitment on uptime, response times, or quality between a service provider and client. Violations can have financial penalties.
> *"Our SLA guarantees 99.9% uptime, so every outage is logged and reported."*

**KPI (Key Performance Indicator)**
A measurable value that shows how effectively a team or product is achieving a goal.
> *"Reducing customer onboarding time from 3 days to 1 is the KPI for this quarter."*

**ROI (Return on Investment)**
The business value or financial gain relative to the cost of a project or initiative.
> *"We need to show the client a clear ROI within 6 months to justify the engagement."*

**Stakeholder**
Any person or group with an interest in the project's outcome — client, end users, leadership, or your own team.
> *"We need stakeholder sign-off before moving to the build phase."*

**Use case**
A specific scenario describing how a user interacts with a system to achieve a goal. Forms the basis of requirements.
> *"The login use case covers both email/password and SSO flows."*

**Deliverable**
A specific, agreed-upon output — could be code, a report, a demo, or documentation.
> *"The first deliverable is a working prototype by end of Sprint 2."*

**Runway**
How long a team can sustain operations before needing more resources, budget, or a decision.
> *"With current velocity, we have a 3-sprint runway before the feature freeze."*

**North star**
The overarching goal or vision that guides product and business decisions.
> *"Our north star is to reduce resolution time for every IBM client to under 2 hours."*

**Scope creep**
The gradual expansion of a project's requirements beyond what was originally agreed — a major risk to timelines and budgets.
> *"Adding that new dashboard wasn't in the SOW — this is scope creep. We need a change request."*

**Change request (CR)**
A formal process to introduce new requirements or modifications to an agreed scope.
> *"The client wants two new reports — raise a change request so we can estimate the effort."*

**Sign-off**
Formal approval from a stakeholder or client that a deliverable or phase is accepted.
> *"We need client sign-off on the wireframes before the dev team can start building."*

**Account manager (AM)**
The IBM point of contact responsible for the client relationship — not a technical role.
> *"Loop in the account manager if the client escalates — don't handle it alone."*

**Engagement**
The term IBM uses for a client project or contract.
> *"This is a 12-month engagement with a Bangalore-based BFSI client."*

---

## 🔄 Process & Delivery Terms

**Agile**
An iterative software development methodology built on flexibility, collaboration, and incremental delivery. IBM projects almost always follow some form of Agile.
> *"We follow Agile, so requirements can evolve between sprints."*

**Scrum**
A specific Agile framework with defined roles (Scrum Master, Product Owner, Dev Team), ceremonies, and artefacts.
> *"Our Scrum Master facilitates the sprint ceremonies and removes blockers."*

**Sprint**
A fixed time period (usually 2 weeks) in Agile during which a defined set of tasks is completed and reviewed.
> *"The login feature is scoped for Sprint 4."*

**Stand-up / Daily scrum**
A short daily meeting (15 minutes max) where each team member answers: What did I do yesterday? What am I doing today? Any blockers?
> *"Raise the API dependency issue at stand-up so the team can help unblock you."*

**Backlog**
A prioritised list of all features, bugs, and tasks yet to be worked on. Owned and managed by the Product Owner.
> *"Add that feature request to the backlog — we'll groom it in the next refinement session."*

**Grooming / Backlog refinement**
The process of reviewing, clarifying, and estimating backlog items before they enter a sprint.
> *"During grooming, we broke the user story into four smaller subtasks."*

**Velocity**
The amount of work (measured in story points) a team completes in a sprint. Used for forecasting.
> *"Our velocity is 40 points per sprint — we can plan accordingly for the next quarter."*

**Story points**
A unit of estimation used in Agile to measure the complexity of a task, not time.
> *"The payment integration is estimated at 8 story points — it's complex."*

**UAT (User Acceptance Testing)**
Testing done by end users or client representatives to confirm the product meets requirements before go-live.
> *"The client's team is doing UAT this week — we're on standby for any fixes."*

**Go-live**
The moment a system or feature is deployed to production and real users start using it.
> *"Go-live is April 1st — all UAT issues must be resolved by March 25th."*

**Escalation** ⚠️
Raising an unresolved issue to a higher level of authority or urgency. In Indian IT culture, freshers often hesitate to escalate for fear of seeming incompetent. At IBM, escalation is a **professional, expected, and time-sensitive action**.
> *"If the dependency isn't resolved in 24 hours, escalate to your project manager — don't wait."*

**Blocker**
Anything preventing a team member from completing their work. Must be raised immediately, not at the end of the sprint.
> *"I have a blocker — I can't proceed with the API integration until the credentials are shared."*

**Retrospective (Retro)**
A meeting at the end of each sprint where the team reflects on what went well, what didn't, and what to improve.
> *"In the retro, we flagged that QA cycles are too short — we'll build in more buffer next sprint."*

**Release notes**
A document summarising what has changed, been added, or been fixed in a new version of software.
> *"Update the release notes before the deployment so the client's team knows what changed."*

---

## 💬 Culture & Communication Terms

**Onboarding**
The process of integrating a new employee (or new client) into tools, processes, and team culture.
> *"Your onboarding buddy will help you set up your IBM Intranet access and tools."*

**Shadowing**
Following and observing a senior colleague's work to learn — common in the first few weeks at IBM.
> *"In your first week, you'll be shadowing a senior BA on client discovery calls."*

**Loop in / Loop out** ⚠️
"Loop in" = include someone in a conversation, email, or decision. Used constantly in IBM communications.
> *"Loop in the security team before we finalise the API design."*

**Sync** ⚠️
A meeting or conversation to align on status or decisions. Short for "synchronise."
> *"Let's do a quick sync after lunch to align before the client demo."*

**Ping** ⚠️
To send someone a quick message on Slack or Teams.
> *"Ping me when the build is deployed to QA."*

**EOD / EOM / EOQ** ⚠️
- **EOD** — End of Day (typically 6 PM, but context-dependent)
- **EOM** — End of Month
- **EOQ** — End of Quarter
These appear in emails constantly. Missing an EOD deadline is a red flag.
> *"Please share the status report by EOD Friday."*

**LGTM** ⚠️
"Looks Good To Me" — approval signal in code reviews, Slack threads, and document reviews.
> *"Reviewed your PR — LGTM, go ahead and merge."*

**FYI / FYA**
"For Your Information" / "For Your Action." FYI = awareness only. FYA = you need to do something.
> *"FYI — the client has moved the demo to Thursday. FYA — please update the deck by Wednesday EOD."*

**ASAP**
As Soon As Possible. Often overused. When a manager says ASAP at IBM, treat it as high priority unless they specify otherwise.
> *"The client is asking for the data ASAP — deprioritise other tasks."*

**TBD / TBC**
"To Be Decided" / "To Be Confirmed." Placeholder used in planning documents.
> *"The go-live date is TBC — pending client UAT completion."*

**Bandwidth (culture)** ⚠️
In non-technical conversations, "bandwidth" means a person's availability and capacity.
> *"Do you have the bandwidth to review this design doc by EOD?"*

**Heads-up**
An informal advance warning or alert about something coming.
> *"Just a heads-up — the client wants to join the sprint review this Friday."*

**Action item**
A specific task assigned to a person, usually arising from a meeting.
> *"Action item from today's call: Priya to share updated wireframes by Thursday."*

**Deep dive**
A thorough, detailed analysis or discussion of a specific topic.
> *"The client wants a deep dive on the performance benchmarks in the next call."*

**Takeaway**
A key conclusion or learning from a meeting or document.
> *"The main takeaway from the retro was that we need better test coverage."*

**Bandwidth (dual meaning — summary)** ⚠️
| Context | Meaning |
|---|---|
| Technical discussion | Network capacity — how much data can be transferred |
| People / project discussion | A person's available time and mental capacity |

---

## 🏦 IBM & Indian IT-specific Terms

**BFSI**
Banking, Financial Services, and Insurance — one of IBM's largest industry verticals in India.
> *"This engagement is for a BFSI client in Mumbai — regulatory compliance is critical."*

**BU (Business Unit)**
A division within IBM focused on a specific service or industry. You'll be assigned to one.
> *"I'm in the Cloud & Cognitive BU."*

**Utilisation**
The percentage of a consultant's time that is billed to a client project. High utilisation = you're staffed and productive.
> *"Your utilisation for this quarter is 85% — keep that up."*

**Bench**
When a consultant is not assigned to a client project, they are "on the bench." Usually temporary.
> *"She's on the bench between projects — the staffing team is looking for her next engagement."*

**Band / Grade**
IBM's internal levels for employees (e.g., Band 6, Band 7). Determines compensation and role scope.
> *"You'll join as a Band 6 — promotion to Band 7 typically takes 2–3 years with strong performance."*

**GBS / GTS / GDC**
- **GBS** — Global Business Services (consulting and application services)
- **GTS** — Global Technology Services (infrastructure)
- **GDC** — Global Delivery Centre (offshore delivery hubs, including India)

**Offshore / Onshore**
- **Offshore** — work done from India (or another low-cost location)
- **Onshore** — work done at the client's location or country
> *"The design will be done onshore in the US; development is offshore in Bangalore."*

**CR (Change Request)**
A formal process to modify agreed scope, timeline, or budget. Every change to the SOW requires a CR.
> *"The client asked for a new module — we can't start until the CR is approved."*

---

## 📋 Quick-Reference Cheat Sheet

| Shorthand | Full form |
|---|---|
| API | Application Programming Interface |
| ASAP | As Soon As Possible |
| BFSI | Banking, Financial Services & Insurance |
| CI/CD | Continuous Integration / Continuous Delivery |
| CR | Change Request |
| EOD | End of Day |
| EOM | End of Month |
| EOQ | End of Quarter |
| FYA | For Your Action |
| FYI | For Your Information |
| GBS | Global Business Services |
| GDC | Global Delivery Centre |
| GTS | Global Technology Services |
| I/O | Input / Output |
| KPI | Key Performance Indicator |
| LGTM | Looks Good To Me |
| PR | Pull Request |
| QA | Quality Assurance |
| ROI | Return on Investment |
| SLA | Service Level Agreement |
| SOW | Statement of Work |
| TBC | To Be Confirmed |
| TBD | To Be Decided |
| UAT | User Acceptance Testing |

---

*Prepared for IBM Freshers Induction Programme — India*
*Version 1.0*
