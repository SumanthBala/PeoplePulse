# PeoplePulse — Profile Page Update

This package is based on `PeoplePulse-DB-Analytics-Login-Security-Full.zip` and adds a persistent Profile page without deleting or recreating the existing H2 database.

## Profile fields
- Profile picture (image upload, max 5 MB)
- First Name
- Last Name
- Email
- Phone Number
- Role (read-only)
- Skills

## Backend
- `ProfileController.java`
- `User.java` profile fields
- Existing H2 schema repair extended for profile columns
- Profile picture stored in `APP_USERS` as a BLOB with its content type; DELETE `/api/profile/{username}/photo` removes the stored photo

## Frontend
- Profile navigation is shown in the left sidebar below Applications / recruitment navigation; clicking Profile opens the profile page
- Profile editor and photo upload
- Change/Add Photo and Remove Photo controls
- Existing Admin Projects & Jobs and Employees navigation is retained

## H2 data
The existing `./data/peoplepulse_db` is NOT included in this ZIP and is not deleted by the changes. Start the application against your existing `data` directory.

## Verification
ZIP integrity was checked with `unzip -tq`. Maven/frontend dependency builds were not run in this environment.
