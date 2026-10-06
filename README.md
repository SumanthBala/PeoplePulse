# PeoplePulse — DB Persistence Fixed

## Important
This project uses a **file-based H2 database**. Do not delete the existing `backend/data` folder if you want to keep your old records.

The backend uses:
`jdbc:h2:file:./data/peoplepulse_db;AUTO_SERVER=TRUE`

Start Spring Boot with the **backend directory as the IntelliJ working directory** so `./data` points to `backend/data`.

After starting the backend, open:
`http://localhost:8080/api/db/status`

This endpoint reads counts directly from the live Spring Boot H2 connection. The DB Persistence Portal displays these counts. The normal Admin Dashboard does not expose database persistence controls.

## Persistence changes
- Employee and project writes use `saveAndFlush()`.
- Candidate application writes and resume writes use `saveAndFlush()`.
- Employee requests are persisted in `employee_requests`; approval creates the real employee record.
- Project job fields are persisted in the `projects` table.
- `spring.jpa.hibernate.ddl-auto=update` preserves existing data and adds missing columns/tables.

## H2 console
Open `http://localhost:8080/h2-console`
JDBC URL:
`jdbc:h2:file:./data/peoplepulse_db`
User: `sa`
Password: blank

If the H2 console is started from a different working directory than the backend, use the exact JDBC URL/path shown by `/api/db/status` and do not create a second database.


## Separate DB Persistence login
The application and database administration logins are intentionally separate.

Application login:
- admin / admin123
- manager / manager123
- candidate / candidate123

DB Persistence login:
- dbadmin / dbadmin123

The DB Persistence portal provides live H2 connection details, table row counts, database metrics, and persisted login activity. The normal Admin portal no longer contains DB Persistence, Employees, or Projects administration screens.

Login attempts are stored in the `login_activity` table. Existing H2 data is preserved by `spring.jpa.hibernate.ddl-auto=update`; do not delete the existing `data` directory.

## Account lockout & administrator unlock
- Every failed login is persisted with a reason in `LOGIN_ACTIVITY`.
- Five consecutive failed attempts automatically lock the account.
- A locked account appears in **DB Persistence → Account Unlock Approval**.
- DB Persistence Admin can approve the unlock.
- Approval clears the lock and forces a password change before application access.
- New passwords must be at least 8 characters and the old password is verified during the forced reset.
- Recent Login Activity now includes a **Reason** column and LOCKED / UNLOCK_APPROVED / PASSWORD_CHANGED events.
- Existing H2 data is preserved; do not delete the `data` directory.
