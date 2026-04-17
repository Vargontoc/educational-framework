#!/usr/bin/env python3
"""
Validation script for ai-educational-child agent.
Tests all event x personality combinations against local Ollama.

Usage:
    python validate.py [--host http://localhost:11434] [--run-id run-001]
"""

import argparse
import json
import os
import re
import sys
import time
from datetime import datetime
from pathlib import Path

try:
    import requests
except ImportError:
    print("ERROR: 'requests' not installed. Run: pip install requests")
    sys.exit(1)

BASE_DIR   = Path(__file__).parent.parent
TESTS_DIR  = Path(__file__).parent
RESULTS_DIR = TESTS_DIR / "results"
MODELFILE   = BASE_DIR / "Modelfile"
PERSONALITIES_DIR = BASE_DIR / "personalities"


# ── Helpers ────────────────────────────────────────────────────────────────────

def load_modelfile_system() -> str:
    """Extract the SYSTEM block content from the Modelfile."""
    text = MODELFILE.read_text(encoding="utf-8")
    match = re.search(r'SYSTEM\s+"""(.*?)"""', text, re.DOTALL)
    if not match:
        raise ValueError("SYSTEM block not found in Modelfile")
    return match.group(1).strip()


def load_personality_prompt(name: str) -> str:
    """Extract the Prompt Injection section from a personality file."""
    path = PERSONALITIES_DIR / f"{name}.md"
    text = path.read_text(encoding="utf-8")
    match = re.search(r"## Prompt Injection\n\n(.*?)(?=\n##|\Z)", text, re.DOTALL)
    if not match:
        raise ValueError(f"Prompt Injection section not found in {name}.md")
    return match.group(1).strip()


def assemble_system_prompt(template: str, personality_prompt: str, fixture: dict, meta: dict) -> str:
    return (
        template
        .replace("{companion_name}", meta["companion_name"])
        .replace("{child_name}",     fixture["child_name"])
        .replace("{personality_prompt}", personality_prompt)
        .replace("{language}",       meta["language"])
    )


def build_user_message(fixture: dict, meta: dict) -> str:
    payload = {k: v for k, v in fixture.items() if k != "id"}
    payload["companion_name"] = meta["companion_name"]
    payload["language"]       = meta["language"]
    return json.dumps(payload, ensure_ascii=False)


def count_words(text: str) -> int:
    return len(text.split())


def check_constraints(response_obj: dict, fixture: dict, constraints: dict) -> list[str]:
    violations = []
    text = response_obj.get("text", "")

    word_count = count_words(text)
    if word_count > constraints["max_words"]:
        violations.append(f"word_count {word_count} exceeds max {constraints['max_words']}")

    if response_obj.get("emotion") not in constraints["valid_emotions"]:
        violations.append(f"invalid emotion: {response_obj.get('emotion')!r}")

    if response_obj.get("tts_speed") not in constraints["valid_tts_speeds"]:
        violations.append(f"invalid tts_speed: {response_obj.get('tts_speed')!r}")

    text_lower = text.lower()
    for word in constraints["banned_words"]:
        if re.search(r'\b' + re.escape(word) + r'\b', text_lower):
            violations.append(f"banned word found: '{word}'")

    return violations


def call_ollama(host: str, system_prompt: str, user_message: str) -> tuple[str, float]:
    url = f"{host}/api/generate"
    payload = {
        "model":  "qwen2.5:7b-instruct-q5_K_M",
        "system": system_prompt,
        "prompt": user_message,
        "stream": False,
        "options": {"num_predict": 40, "temperature": 0.7}
    }
    t0 = time.time()
    resp = requests.post(url, json=payload, timeout=60)
    elapsed = time.time() - t0
    resp.raise_for_status()
    return resp.json().get("response", ""), elapsed


# ── Main ───────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host",   default="http://localhost:11434")
    parser.add_argument("--run-id", default=f"run-{datetime.now().strftime('%Y%m%d-%H%M%S')}")
    args = parser.parse_args()

    with open(TESTS_DIR / "fixtures.json", encoding="utf-8") as f:
        fixtures_data = json.load(f)

    meta        = fixtures_data["meta"]
    events      = fixtures_data["events"]
    personalities = fixtures_data["personalities"]
    constraints = fixtures_data["constraints"]

    system_template = load_modelfile_system()

    results   = []
    total     = len(events) * len(personalities)
    passed    = 0
    failed    = 0

    print(f"\n{'─'*60}")
    print(f"  ai-educational-child validation  |  run: {args.run_id}")
    print(f"  host: {args.host}  |  tests: {total}")
    print(f"{'─'*60}\n")

    for personality in personalities:
        personality_prompt = load_personality_prompt(personality)

        for fixture in events:
            label = f"{personality:<10} / {fixture['event']:<25}"
            system = assemble_system_prompt(system_template, personality_prompt, fixture, meta)
            user   = build_user_message(fixture, meta)

            try:
                raw_response, elapsed = call_ollama(args.host, system, user)

                # Try to extract JSON from response
                json_match = re.search(r'\{.*\}', raw_response, re.DOTALL)
                if not json_match:
                    raise ValueError("No JSON object found in response")

                response_obj = json.loads(json_match.group())
                json_valid   = True
                violations   = check_constraints(response_obj, fixture, constraints)
                status       = "PASS" if not violations else "FAIL"

            except Exception as e:
                response_obj = {}
                raw_response = str(e)
                json_valid   = False
                violations   = [f"exception: {e}"]
                status       = "ERROR"
                elapsed      = 0.0

            if status == "PASS":
                passed += 1
            else:
                failed += 1

            symbol = "✓" if status == "PASS" else "✗"
            print(f"  {symbol} {label}  [{elapsed:.1f}s]")
            if violations:
                for v in violations:
                    print(f"      → {v}")

            results.append({
                "run_id":      args.run_id,
                "personality": personality,
                "event":       fixture["event"],
                "fixture_id":  fixture["id"],
                "status":      status,
                "json_valid":  json_valid,
                "word_count":  count_words(response_obj.get("text", "")),
                "emotion":     response_obj.get("emotion"),
                "tts_speed":   response_obj.get("tts_speed"),
                "text":        response_obj.get("text", ""),
                "violations":  violations,
                "elapsed_s":   round(elapsed, 2)
            })

    print(f"\n{'─'*60}")
    print(f"  PASSED: {passed}/{total}   FAILED: {failed}/{total}")
    print(f"{'─'*60}\n")

    RESULTS_DIR.mkdir(exist_ok=True)
    output_path = RESULTS_DIR / f"{args.run_id}.json"
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump({"summary": {"run_id": args.run_id, "total": total, "passed": passed, "failed": failed},
                   "results": results}, f, indent=2, ensure_ascii=False)

    print(f"  Results saved → {output_path}\n")
    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
