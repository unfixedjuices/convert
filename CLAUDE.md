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
- Stacked PRs: a change is a chain of small branches, each PR targeting the
  branch below it, each level passing `./gradlew check` on its own. Name
  levels for their content (`convert/units`, `convert/screens`).
- Bump `versionCode` on every shipped build; `versionName` is strict semver.
