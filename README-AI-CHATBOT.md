# PeoplePulse AI Chatbot

The project now includes a floating **PeoplePulse AI** chatbot for Candidate, Manager and Admin portals.

## Enable the AI

Set the OpenAI API key as an environment variable before starting Spring Boot:

### Windows PowerShell
```powershell
$env:OPENAI_API_KEY="your_api_key_here"
$env:OPENAI_MODEL="gpt-5.6-luna"
```

### Windows CMD
```cmd
set OPENAI_API_KEY=your_api_key_here
set OPENAI_MODEL=gpt-5.6-luna
```

Then start the backend normally.

The key is read by Spring Boot from `OPENAI_API_KEY`; it is not stored in the React application.

## Role-aware behavior

- Candidate: only their own application information and public jobs are supplied to the AI.
- Manager: recruitment and job information is supplied.
- Admin: recruitment and job information is supplied.
- The chatbot is informational and does not perform approve/reject/create/delete actions.

The chatbot uses the OpenAI Responses API. The default model is configurable through `OPENAI_MODEL` and defaults to `gpt-5.6-luna`.
