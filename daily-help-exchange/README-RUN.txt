DAILY HELP EXCHANGE - RUN INSTRUCTIONS
======================================

CORRECT FLOW
Register -> Login -> Home dashboard -> Logout

1. CREATE THE POSTGRESQL DATABASE
---------------------------------
Open pgAdmin Query Tool and run:

CREATE DATABASE help_exchange;

The username and password in src/main/resources/application.properties must
match your PostgreSQL username and password.

2. OPEN THE CORRECT BACKEND FOLDER
----------------------------------
In PowerShell:

cd "PATH_TO_PROJECT\daily-help-exchange"

This must be the folder that contains pom.xml.

3. START THE PROJECT
--------------------
Windows PowerShell:

.\mvnw.cmd clean spring-boot:run

Or, if Maven is installed:

mvn clean spring-boot:run

4. OPEN THE WEBSITE
-------------------
Open this URL:

http://localhost:8091

The Login/Register screen appears first. The Home dashboard appears only after
a successful login. Use the Logout button to end the server session.

IMPORTANT
---------
- Do not run commands from the outer "project 3" folder.
- Do not open src/main/resources/static/index.html directly in the browser.
- Do not run the old duplicate React prototype for this version.
- Existing SHA-256 passwords are automatically converted to BCrypt after the
  user's next successful login.

MULTI-USER TASK FLOW
--------------------
- Every logged-in user can post a task.
- All logged-in users see the same shared task list.
- Any logged-in user can claim an OPEN task.
- The backend records the creator and claimant from the logged-in account.
- Only one user can claim a task; later claim attempts receive an error.

To test two accounts at the same time, use two separate browser sessions:
- Account 1: normal Chrome window
- Account 2: Chrome Incognito window or Microsoft Edge

Two normal tabs share one login session, so they cannot represent two users.
