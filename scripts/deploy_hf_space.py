#!/usr/bin/env python3
"""
Deploy NeuralRail to a Hugging Face Docker Space (one command).

Prereq (run once, in your terminal so the token is NOT in any chat):
    pip install -U huggingface_hub
    huggingface-cli login        # paste an HF token with WRITE access

Then:
    python scripts/deploy_hf_space.py            # space name defaults to neuralrail-ctc
    python scripts/deploy_hf_space.py my-space   # custom name

It creates (or updates) the Space, uploads the repo, and prints the live URL.
HF builds the Dockerfile (CPU torch) and serves on app_port 8000.
"""
import sys
import os
from huggingface_hub import HfApi, HfFolder

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SPACE_NAME = sys.argv[1] if len(sys.argv) > 1 else "neuralrail-ctc"

token = HfFolder.get_token() or os.environ.get("HF_TOKEN")
if not token:
    sys.exit("No HF token. Run `huggingface-cli login` first (token needs WRITE access).")

api = HfApi(token=token)
user = api.whoami()["name"]
repo_id = f"{user}/{SPACE_NAME}"
print(f"Deploying to Space: {repo_id}")

api.create_repo(repo_id=repo_id, repo_type="space", space_sdk="docker", exist_ok=True)

# HF Spaces need YAML frontmatter (sdk + app_port). Prepend it to the README body
# so the Space card stays professional. Read the repo README for the body.
with open(os.path.join(ROOT, "README.md"), encoding="utf-8") as f:
    body = f.read()
frontmatter = (
    "---\n"
    "title: NeuralRail CTC\n"
    "emoji: 🚆\n"
    "colorFrom: gray\n"
    "colorTo: orange\n"
    "sdk: docker\n"
    "app_port: 8000\n"
    "pinned: false\n"
    "license: mit\n"
    "---\n\n"
)
space_readme = os.path.join(ROOT, ".hf_space_README.md")
with open(space_readme, "w", encoding="utf-8") as f:
    f.write(frontmatter + body)

IGNORE = [".venv/*", "**/__pycache__/*", "*.pyc", "node_modules/*",
          "web/dist/*", ".git/*", "runs/*", "*.log", ".hf_space_README.md",
          "eval/results.csv"]

print("Uploading project (this can take a minute for the model + curves)…")
api.upload_folder(folder_path=ROOT, repo_id=repo_id, repo_type="space",
                  ignore_patterns=IGNORE, commit_message="Deploy NeuralRail CTC")
# Overwrite README with the frontmatter version so HF configures the Space.
api.upload_file(path_or_fileobj=space_readme, path_in_repo="README.md",
                repo_id=repo_id, repo_type="space", commit_message="Space config")
os.remove(space_readme)

url = f"https://huggingface.co/spaces/{repo_id}"
print(f"\n✓ Done. HF is now building the Docker image. Live in a few minutes at:\n  {url}")
print("  (watch the build logs on that page; first build downloads CPU torch)")
