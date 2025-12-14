# PhishNet - Cybersecurity Incident Reporting System
PhishNet is a comprehensive Database Application developed for the CCINFOM course to manage and analyze cybersecurity incidents. Built with JavaFX and MySQL, it replaces traditional spreadsheet tracking with a scalable, structured platform that handles incident reporting, perpetrator tracking, evidence validation, and automated threat assessment.

## Overview
Malicious digital attacks (phishing, ransomware, scams) are rising threats. PhishNet addresses the complexity of tracking these incidents by linking victims, perpetrators, and attack types in a relational database.

The system serves two main user groups:

1. Victims: Public users who can securely report incidents, upload evidence, and track their case status.

2. Administrators/Security Staff: Professionals who validate reports, verify evidence, manage threat levels, and generate analytical reports.

<br>

## Key Features
- Secure Authentication: Implements Argon2id hashing for robust password security (replacing standard SHA-256).

- Incident Reporting: Victims can submit detailed reports linking perpetrators (Phone, Email, URL, IP) to specific attack types.

- Evidence Management: Specialized upload handling for screenshots, chat logs, and files with an admin verification workflow.

- Automated Threat Escalation: The system automatically flags perpetrators as "Malicious" if they target ≥3 unique victims within 7 days.

- Victim Support Logic: Automatically flags victim accounts for additional support if they report >5 incidents in a single month.

- Recycle Bin & Audit Trails: "Soft delete" functionality for rejected reports/evidence with full restoration capabilities and detailed logging for all status changes.

- Analytics Dashboard: Generates visual reports including:

  - Monthly Attack Trends (Time-of-day analysis)

  - Top Perpetrators

  - High-Risk Victim Activity

  - Evidence Submission Summaries

<br>

## Tech Stack
- Language: Java 17+

- GUI Framework: JavaFX 21

- Database: MySQL 8.0

- Build Tool: Maven

- Security: Password4j (Argon2 Integration)

- Design Pattern: MVC (Model-View-Controller) with DAO Pattern

<br>

## Installation & Setup
### Prerequisites
- Java JDK 17 or higher

- Maven

- MySQL Server

### Database Configuration
Before running the application, you must set up the database schema and seed data.

1. Open your MySQL Workbench or Terminal.

2. Execute the Structure script first:

```SQL
source path/to/CCINFOM S24-2-sql.sql
```

3. Execute the Inserts script second (populates Admins, Attack Types, and Test Data):

```SQL
source path/to/CCINFOM S24-2-sql-inserts.sql
```

4. Important: Verify your database credentials in src/main/java/util/DatabaseConnection.java. Update the DB_USER and DB_PASSWORD fields to match your local MySQL configuration.

<br>

## How to Run
Open your terminal in the project root directory (where pom.xml is located) and run:

```Bash
mvn javafx:run
```
*Note: Default Admin Login credentials can be found in the SQL inserts file or the documentation (Password: PhishNetAdmin124).*

<br>

## Project Workflow & Logic
The application follows a strict transactional flow:

1. Registration/Login: Users register securely. Passwords are hashed using Argon2id before storage.

2. Transaction 1 - Reporting: A victim submits a report. The system checks if the Perpetrator exists; if not, it creates a new record. The report is set to "Pending".

3. Transaction 2 - Evidence: Victims upload files. These are stored in uploads/evidence/ and linked to the report.

4. System Logic - Auto-Analysis:

    - Perpetrator Check: If the perpetrator has attacked multiple unique victims recently, their ThreatLevel is auto-escalated to "Malicious".

    - Victim Check: If the victim submits frequent reports, their AccountStatus is updated to "Flagged".

5. Transaction 3 & 4 - Admin Review: Admins log in to the dashboard to:

    - Validate or Reject reports.

    - Verify evidence authenticity.

    - Manually adjust Threat Levels.

6. Archival: Rejected items are moved to RecycleBinReports and RecycleBinEvidence tables for audit purposes, allowing for restoration if rejected in error.
