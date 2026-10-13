#!/usr/bin/env python3
"""Smoke-test access to selected public Threads profiles.

Required environment:
  THREADS_ACCESS_TOKEN: Meta/Threads API token
  THREADS_TARGETS: comma-separated usernames, without @ (or with @)
Optional:
  THREADS_API_BASE: defaults to https://graph.threads.net
"""
from __future__ import annotations

import json
import os
import sys
from typing import Any

import requests

API_BASE = os.getenv("THREADS_API_BASE", "https://graph.threads.net").rstrip("/")
API_VERSION = os.getenv("THREADS_API_VERSION", "v1.0")
TOKEN = os.getenv("THREADS_ACCESS_TOKEN", "").strip()
TARGETS = [
    value.strip().removeprefix("@")
    for value in os.getenv("THREADS_TARGETS", "").split(",")
    if value.strip()
]
TIMEOUT = 25
POST_FIELDS = "id,username,text,timestamp,permalink"


class ThreadsAPIError(RuntimeError):
    pass


def get_json(path: str, **params: Any) -> dict[str, Any]:
    url = f"{API_BASE}/{API_VERSION}/{path.lstrip('/')}"
    # Threads API accepts access_token as a query parameter. Never print the
    # prepared URL or request params: they contain the secret token.
    try:
        response = requests.get(
            url, params={**params, "access_token": TOKEN}, timeout=TIMEOUT
        )
    except requests.RequestException as exc:
        raise ThreadsAPIError(f"network error: {type(exc).__name__}: {exc}") from exc

    try:
        payload = response.json()
    except ValueError:
        payload = {}

    if not response.ok or (isinstance(payload, dict) and payload.get("error")):
        err = payload.get("error", {}) if isinstance(payload, dict) else {}
        message = err.get("message", "API returned a non-JSON error response")
        code = err.get("code", "unknown")
        subcode = err.get("error_subcode", "unknown")
        raise ThreadsAPIError(
            f"HTTP {response.status_code}; code={code}; "
            f"subcode={subcode}; message={message}"
        )

    if not isinstance(payload, dict):
        raise ThreadsAPIError("API response was not a JSON object")
    return payload


def check_target(username: str) -> bool:
    print(f"\n== @{username} ==")
    try:
        profile = get_json("profile_lookup", username=username)
        print("profile_lookup: OK")
        # Print only non-sensitive profile identifiers; no token or full payload.
        print("profile:", json.dumps({
            key: profile.get(key)
            for key in ("id", "username", "name")
            if profile.get(key) is not None
        }, ensure_ascii=False))

        posts = get_json(
            "profile_posts",
            username=username,
            fields=POST_FIELDS,
            limit=10,
        )
        items = posts.get("data", [])
        if not isinstance(items, list):
            raise ThreadsAPIError("profile_posts response has no list in 'data'")

        print(f"profile_posts: OK; returned_posts={len(items)}")
        for post in items[:3]:
            print(json.dumps({
                "id": post.get("id"),
                "timestamp": post.get("timestamp"),
                "text_preview": (post.get("text") or "")[:180],
                "permalink": post.get("permalink"),
            }, ensure_ascii=False))
        print(f"pagination_available={bool(posts.get('paging', {}).get('next'))}")
        return True
    except ThreadsAPIError as exc:
        print(f"FAILED: {exc}")
        print("Check API product/version, required permissions, token validity, and username.")
        return False


def main() -> int:
    if not TOKEN:
        print("ERROR: configure THREADS_ACCESS_TOKEN as a GitHub Actions secret.", file=sys.stderr)
        return 2
    if not TARGETS:
        print("ERROR: configure THREADS_TARGETS as a repository Actions variable.", file=sys.stderr)
        return 2

    print(f"Threads API smoke test; targets={len(TARGETS)}; base={API_BASE}/{API_VERSION}")
    results = [(name, check_target(name)) for name in TARGETS]
    passed = sum(ok for _, ok in results)
    print(f"\nSUMMARY: {passed}/{len(results)} profiles passed")
    return 0 if passed == len(results) else 1


if __name__ == "__main__":
    raise SystemExit(main())
