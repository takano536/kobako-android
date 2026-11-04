# AGENTS.md

kobako-android で作業するエージェント向けの前提。リリース手順は `docs/release.md`。

## ビルド

- JDK 21、`compileSdk` / `targetSdk` は 36、`minSdk` は 26
- SDK は環境変数で渡す（`local.properties` はコミットしない）

```bash
ANDROID_HOME=/home/takanori/Android/Sdk ./gradlew :app:assembleDebug
```

- クリーンビルドは実測 1分11秒、APK は約12.5MB
- ユニットテスト: `./gradlew :app:testDebugUnitTest`

## エミュレータ（AgentBox CT130）

実機は使わず、使い捨てのエミュレータで確認する。入口は `~/bin/kobako-emu` の1本だけ。

```bash
kobako-emu start     # boot_completed まで待つ（実測 40〜50秒）
kobako-emu status
kobako-emu install app/build/outputs/apk/debug/app-debug.apk
kobako-emu launch    # io.github.takano536.kobako/.MainActivity
kobako-emu shot /tmp/screen.png
kobako-emu stop      # 確認が終わったら必ず実行する
```

- AVD `kobako`: API 36（AOSP、Google Play Servicesなし）、pixel_5（1080x2340 / 440dpi）、RAM 2560MB
- メモリは 6GiB しかない。Gradle とエミュレータを同時に動かさない（ビルド → 起動 → 撮影 → 停止）
- `screencap` が単色（特に真っ黒）のフレームは証拠にしない。色数や分散を確認し、確認できなければ `未確認` と書く
- 画面遷移を見るときは `kobako-emu` の後に `adb -s emulator-5554 shell input tap <x> <y>` を使ってよい
- `adb devices` に実機が出ても操作しない

## コミット

Conventional Commits（例: `feat(android): ...`、`fix(android): ...`、`docs(android): ...`）。
