# Convert (Light Phone III tool)

A fork of `lightphone/light-sdk`. The tool lives in `tool/`; everything else
is upstream and is not edited here. Pull upstream with
`git fetch upstream && git merge upstream/main`.

## Rules

- Kotlin, Compose, coroutines, and the SDK's `LightScreen` / `LightViewModel`
  shape. Use the SDK UI kit (`LightText`, `LightTopBar`, `LightBottomBar`,
  `LightScrollView`, `LightTextInputEditor`). No third-party dependencies
  beyond the plugin's allowlist; the build fails otherwise.
- Never write `AndroidManifest.xml` or set `applicationId`, `versionCode`,
  `versionName` or `namespace` in Gradle. `tool/lighttool.toml` owns them.
- Light's build server extracts only `tool/build.gradle.kts`,
  `tool/lighttool.toml` and `tool/src/main/{kotlin,java,res,assets}`. Nothing
  the tool needs may live anywhere else.
- Offline first. Units never touch the network. Currency fetches the ECB daily
  reference rates at most once a day and keeps the last copy. No other host.
- One purpose, the SDK's look, no colour. Match the phone's built-in tools.
- Tests for every conversion table and for rate parsing. `./gradlew check`
  must pass before any commit.

## Verify

Build, install on the LightOS emulator (system app, see
`docs/system_app`), launch, and screenshot with `adb exec-out screencap -p`.
Build success alone is not done.

## Conventions

- Small commits, one concern each. No emojis. Sparse comments.
- Stacked PRs, using GitHub's stack feature through `gh stack`: a change is
  a chain of small branches, each PR targeting the branch below it, each
  level passing `./gradlew check` on its own. Name levels for their content
  (`convert/units`, `convert/screens`). This clone has an `upstream` remote,
  so run the extension with `--remote github` and `GH_REPO` set to this repo.
- Bump `versionCode` on every shipped build; `versionName` is strict semver.

## Agent skills

### Issue tracker

Issues live in this repo's GitHub Issues, driven with `gh` and always with
`GH_REPO=unfixedjuices/convert` set. See `docs/agents/issue-tracker.md`.

### Triage labels

The five default triage labels, unchanged. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `CONTEXT.md` and `docs/adr/` at the repo root, created when a
term or decision is actually resolved. See `docs/agents/domain.md`.
