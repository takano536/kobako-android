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

## 画面確認（必須）

UI に触れる変更は、PR の head で画面を撮って残す。Android は web のプレビューのようにユーザーが画面を開けないので、フレームが無い変更は確認できない。

- 範囲は変更が影響しうる画面。共有シェル・共通コンポーネント・行高・色トークン・ナビに触れたら5画面（概要 / 取引 / 固定費 / 残高 / 設定）、単一画面の中身だけならその画面でよい。影響する画面が欠けたら未完了とする
- 390x844 を基本。幅・折り返し・余白に触れたら 360x800 も撮る。テーマや色トークンに触れたらライトとダークの両方
- スクロール最上部、デモ表示のまま撮る。変更が折り返しより下にあるときはスクロールしたフレームも1枚
- 数値合わせのときは、フレームと実測表（要素 / 目標 / 実測 / 差）を対で載せる
- PR 本文には各フレームが何を示すかを書き、ローカルの絶対パスは載せない

## コミット

Conventional Commits（例: `feat(android): ...`、`fix(android): ...`、`docs(android): ...`）。
