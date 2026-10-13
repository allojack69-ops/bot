# Threads Analytics MVP — first access check

This first milestone only checks whether the configured Meta token can look up target profiles and retrieve recent public posts. It does not generate or publish posts.

## Configure GitHub Actions

In **Settings → Secrets and variables → Actions** for this repository:

1. Add repository secret `THREADS_ACCESS_TOKEN` containing the token. Do not commit it or paste it into issues/logs.
2. Add repository variable `THREADS_TARGETS` as a comma-separated list of public Threads usernames, e.g. `account_one,account_two` (replace with real usernames).
3. Optional: set `THREADS_API_BASE` to `https://graph.threads.net` and `THREADS_API_VERSION` to `v1.0` if you need to override the defaults.

Then open **Actions → Threads API access check → Run workflow**.

## Interpreting results

- `profile_lookup: OK`: API recognized the username.
- `profile_posts: OK`: endpoint returned a list of posts. A zero count is not necessarily a permission failure.
- HTTP 401/403: token validity, app mode, permissions, or account access may be wrong.
- HTTP 400/404: verify endpoint/version and username; check current Meta documentation for the permissions enabled for this app.
- Network error: check runner connectivity and Meta service status.

The workflow runs daily at 08:15 UTC and can also be run manually. Scheduled runs may be delayed by GitHub.

## Important scope boundary

The official Threads API's supported discovery endpoints and permissions can change. This smoke test is deliberately narrow and does not scrape the website or bypass access controls. Confirm that the app is approved for the exact profile-discovery endpoints before treating results as complete.

## Next milestone after a successful test

1. Store posts by post ID to prevent duplicates.
2. Capture snapshots of available public metrics daily.
3. Generate a report/Threads draft artifact.
4. Add a separate manual approval step. Publishing must remain disabled until explicitly configured and approved.
