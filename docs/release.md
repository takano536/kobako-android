# Android Release

## 安全上の前提

GitHub Actions の Secrets は公開リポジトリのファイルには入らず、fork からの Pull Request にも渡りません。ただし、次の条件を守る前提で運用します。

- Release workflow は `v*` タグの push だけで動かす。`pull_request_target` では Secrets を使わない。
- `main` と `v*` タグを保護し、書き込み権限者を限定する。
- GitHub Environment `release` を作り、Required reviewers を設定する。署名 Secrets は Environment secrets に置く。
- workflow の `GITHUB_TOKEN` は `contents: read` のみ。Release 作成には短命な GitHub App token を別に発行する。
- 署名鍵は GitHub Release 用に専用で作る。F-Droid の署名鍵とは共用しない。
- keystore、パスワード、`keystore.properties`、App の秘密鍵はコミットしない。漏えい時は鍵をローテーションし、同じ applicationId のままでは署名が変わるため、利用者には再インストールが必要になる。
- GitHub の管理者や workflow を変更できる権限者は Secrets を読める立場にある。完全なゼロトラストではない。より強くするなら、手元で署名した APK を Release に手動添付する。

## データの流れ

### 署名鍵

| 項目 | 取得元 | 保存先 | workflow での扱い |
| --- | --- | --- | --- |
| `ANDROID_KEYSTORE_BASE64` | 手元で `keytool` から生成した `kobako-release.jks` を Base64 化 | GitHub Environment `release` の Secret | runner の `$RUNNER_TEMP/release.keystore` にデコードして Gradle に渡す |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 作成時に決めたパスワード | 同じ Environment Secret | `KEYSTORE_PASSWORD` として Gradle に渡す |
| `ANDROID_KEY_ALIAS` | keystore 作成時の alias | 同じ Environment Secret | `KEY_ALIAS` として Gradle に渡す |
| `ANDROID_KEY_PASSWORD` | 鍵のパスワード | 同じ Environment Secret | `KEY_PASSWORD` として Gradle に渡す |

keystore 本体はリポジトリに置かず、手元の安全な場所にバックアップします。runner の keystore は job の最後に削除します。

### Release App

| 項目 | 取得元 | 保存先 | 用途 |
| --- | --- | --- | --- |
| `RELEASE_APP_CLIENT_ID` | GitHub App `kobako-release` の設定画面 | Repository または Environment `release` の Variable | App token の発行 |
| `RELEASE_APP_BOT_LOGIN` | `kobako-release[bot]` | 同じ Variable | App の slug 検証 |
| `RELEASE_APP_PRIVATE_KEY` | App 設定の Private keys で生成・ダウンロードした PEM | Environment `release` の Secret | `actions/create-github-app-token` が短命 token を発行 |

`takano536/kobako` の `RELEASE_APP_CLIENT_ID` と `RELEASE_APP_BOT_LOGIN` と同じ App を使えます。ただし Secret の値は GitHub から読み出せないため、`RELEASE_APP_PRIVATE_KEY` は元の PEM を用意するか、App 設定で新しい private key を生成して Android 側に設定します。既存の kobako 側の鍵はそのまま残せます。

App を `kobako-android` にインストールし、Contents の Read and write を許可してください。App のインストール対象が `kobako` だけの場合、token 発行 step で失敗します。

## 必要な Secrets / Variables

GitHub Environment `release` に次を設定します。

Secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `RELEASE_APP_PRIVATE_KEY`

Variables（Repository または Environment）:

- `RELEASE_APP_CLIENT_ID`
- `RELEASE_APP_BOT_LOGIN`

## keystore の作成（Windows / PowerShell）

```powershell
keytool -genkeypair -v -keystore kobako-release.jks -alias kobako -keyalg RSA -keysize 4096 -validity 10000
[Convert]::ToBase64String([IO.File]::ReadAllBytes("kobako-release.jks")) | Set-Clipboard
```

表示された Base64 を `ANDROID_KEYSTORE_BASE64` に設定します。`kobako-release.jks` は安全な場所にバックアップし、リポジトリに置かないでください。

## Release

```bash
git tag v0.1.0
git push origin v0.1.0
```

workflow が `assembleRelease`、署名検証、GitHub Release への APK と SHA256 の添付まで行います。Release の作成者は `kobako-release[bot]` になります。

## 初回だけ必要な設定

1. GitHub App `kobako-release` を `takano536/kobako-android` にインストールする。
2. Environment `release` を作り、Required reviewers を設定する。
3. 上記の Secrets / Variables を Environment `release` に設定する。
4. `main` と `v*` タグの保護を有効にする。
5. `v0.1.0` タグを push して Release workflow を実行する。
