# Android Release

## 全体像

```text
main へ conventional commit をマージ
  -> Release Please が Release PR を作成・更新
  -> Release PR をマージ
  -> vX.Y.Z タグと GitHub Release を自動作成
  -> Android Release workflow が署名済み APK をビルド
  -> 既存の GitHub Release に APK と SHA256 を添付
  -> Obtainium / F-Droid が新しいタグを検出
```

Release の作成者は `kobako-release[bot]`、APK のアップロードにも同じ App の短命 token を使います。署名 Secrets は Environment `release` の承認後にだけ使われます。

## バージョン管理

`app/build.gradle.kts` の次の行が唯一のバージョン正本です。

```kotlin
val appVersionName = "0.1.0" // x-release-please-version
```

`versionCode` は SemVer から自動計算します。

```text
major * 10000 + minor * 100 + patch
```

例: `0.1.0` = 100、`0.1.1` = 101、`0.2.0` = 200。

Release Please は `x-release-please-version` の行だけを書き換えます。`versionCode` を手で更新する必要はありません。タグは `vX.Y.Z` 形式で、`versionName` と一致しないと Release workflow が失敗します。

## Conventional Commits

Release PR のバージョンはコミット種別で決まります。

| コミット | バージョン |
| --- | --- |
| `fix:` | patch |
| `feat:` | minor（0.x では `bump-minor-pre-major` により minor） |
| `feat!:` / `BREAKING CHANGE:` | 破壊的変更 |
| `ci:` `docs:` `chore:` `test:` | 通常はリリース対象外 |

Release PR は `chore(release): X.Y.Z` として自動作成されます。この PR をマージするとタグと Release が作られます。通常の機能 PR をマージしただけではタグは作りません。

## Workflows

### `.github/workflows/release-please.yml`

`main` への push で起動します。

- `kobako-release` App の短命 token を発行
- Release PR の作成・更新
- Release PR マージ後のタグ・GitHub Release 作成

Release Please 用の `RELEASE_APP_PRIVATE_KEY` は **Repository Secret** に必要です。Environment `release` の Secret だけでは Release Please workflow から読めません。

### `.github/workflows/android-release.yml`

`release: published` で起動します。タグ push では起動しません。

- `environment: release` の承認待ち
- タグと `versionName` の一致確認
- keystore を一時ファイルにデコードして署名
- `apksigner verify` で署名検証
- 既存 Release に `app-release.apk` と `app-release.apk.sha256` を添付
- draft Release の場合は公開

Release の作成自体は Release Please が行います。workflow は Release を作りません。

手動で特定タグをビルドしたい場合は、Release を作成したうえで Actions から `Android Release` を `workflow_dispatch` 実行し、`tag` に `vX.Y.Z` を指定します。

## 必要な Secrets / Variables

### Repository Secret

- `RELEASE_APP_PRIVATE_KEY` — `kobako-release` App の Private key PEM

### Environment `release` の Secret

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `RELEASE_APP_PRIVATE_KEY` — 署名 job 用。Repository Secret と同じ値を置く

Environment Secret は Environment `release` を使う job にだけ渡ります。Release Please workflow は Environment を使わないため、同じ PEM を Repository Secret にも登録します。

### Repository Variables

- `RELEASE_APP_CLIENT_ID` — `kobako-release` の Client ID
- `RELEASE_APP_BOT_LOGIN` — `kobako-release[bot]`

## GitHub App の権限

`kobako-release` App に次を許可します。

- Contents: Read and write
- Pull requests: Read and write
- Issues: Read and write（Release PR のラベル用）
- Metadata: Read-only（自動）

App は `takano536/kobako-android` にインストールされている必要があります。App の権限を変更した場合は、インストール側の承認も更新してください。

## 初回セットアップ

1. Repository Secret `RELEASE_APP_PRIVATE_KEY` に App の PEM を登録する
2. Environment `release` に署名用 Secrets と `RELEASE_APP_PRIVATE_KEY` があることを確認する
3. App の権限と `kobako-android` へのインストールを確認する
4. `main` と `v*` タグの保護を確認する。タグ保護がある場合は `kobako-release` App を bypass 対象にする
5. この自動化 PR をマージする
6. `fix:` または `feat:` の変更を `main` にマージして Release PR を出す
7. Release PR をマージする
8. Environment `release` を承認する
9. Release に APK と SHA256 が付いたことを確認する

## 手動リリース（フォールバック）

Release Please を使わずに緊急リリースする場合も、タグを直接 push するのではなく Release を作成します。

```bash
# versionName を手で更新した場合だけ。通常は Release Please に任せる
git tag v0.1.1
git push origin v0.1.1

gh release create v0.1.1 --repo takano536/kobako-android   --title "kobako v0.1.1" --generate-notes
```

`gh release create` で published Release を作ると `Android Release` が起動し、APK が添付されます。タグだけを push しても workflow は起動しません。

## セキュリティ上の前提

- Release workflow は `release: published` と `workflow_dispatch` だけで動かす
- `pull_request_target` では Secrets を使わない
- `main` と `v*` タグを保護し、書き込み権限者を限定する
- Environment `release` の Required reviewers を維持する
- workflow の `GITHUB_TOKEN` は `contents: read` のみ。Release 作成・更新は App token を使う
- keystore、パスワード、`keystore.properties`、App の秘密鍵はコミットしない
- 署名鍵は GitHub Release 用に専用で作る。F-Droid の署名鍵とは共用しない
- GitHub の管理者や workflow を変更できる権限者は Secrets を読める立場にある

Environment の承認を外すと、`main` にマージできる人なら誰でも署名済み APK を公開できる状態になります。完全自動化より、承認を残す運用を推奨します。

## トラブルシューティング

- Release PR が作られない: Conventional Commits か、`ci` などのリリース対象外コミットだけになっていないか確認する
- `RELEASE_APP_PRIVATE_KEY` エラー: Repository Secret に登録されているか確認する。Environment Secret だけでは足りない
- App token エラー: App の権限、インストール対象、`RELEASE_APP_CLIENT_ID` / `RELEASE_APP_BOT_LOGIN` を確認する
- タグ作成に失敗する: タグ保護の bypass に App を追加する
- APK が Release に付かない: `Android Release` の Environment 承認待ちでないか確認する
- `tag does not match versionName`: Release PR をマージせずにタグだけが作られた可能性がある。Release PR のマージ後にできるタグを使う
