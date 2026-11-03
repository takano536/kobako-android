# Android Release

## 安全上の前提

GitHub Actions の Secrets は公開リポジトリのファイルには入らず、fork からの Pull Request にも渡りません。ただし、次の条件を守る前提で運用します。

- Release workflow は `v*` タグの push だけで動かす。`pull_request_target` では Secrets を使わない。
- `main` と `v*` タグを保護し、書き込み権限者を限定する。
- GitHub Environment `release` を作り、Required reviewers を設定する。署名 Secrets は Environment secrets に置く。
- Release job の権限は `contents: write` だけにする。
- 署名鍵は GitHub Release 用に専用で作る。F-Droid の署名鍵とは共用しない。
- keystore、パスワード、`keystore.properties` はコミットしない。漏えい時は鍵をローテーションし、同じ applicationId のままでは署名が変わるため、利用者には再インストールが必要になる。
- GitHub の管理者や workflow を変更できる権限者は Secrets を読める立場にある。完全なゼロトラストではない。より強くするなら、手元で署名した APK を Release に手動添付する。

## 必要な Secrets

GitHub Environment `release` に次を設定します。

| Secret | 内容 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | release keystore を Base64 にした文字列 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore のパスワード |
| `ANDROID_KEY_ALIAS` | 鍵の alias |
| `ANDROID_KEY_PASSWORD` | 鍵のパスワード |

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

workflow が `assembleRelease`、署名検証、GitHub Release への APK と SHA256 の添付まで行います。

## Release bot について

`takano536/kobako` の Release App は `repositories: kobako` にスコープされています。Android リポジトリで同じ App 名義の Release を作る場合は、App を `kobako-android` にもインストールし、workflow に App token の取得 step を追加する必要があります。この workflow は追加設定が不要な `GITHUB_TOKEN` で Release を作成します。
