# secp192k1 Reseam patches

A [Reseam](https://reseam.app) patch bundle for Android apps.

[![Add to Reseam Manager](https://img.shields.io/badge/Add_to_Reseam_Manager-51AE79?style=for-the-badge&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI%2BPHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0xMC41IDNoM3Y3LjVIMjF2M2gtNy41VjIxaC0zdi03LjVIM3YtM2g3LjV6Ii8%2BPC9zdmc%2B)](https://reseam.app/add/?url=https://github.com/secp192k1/reseam-patches)

## Patches

<!-- PATCHES:START -->
<!-- PATCHES:END -->

## Setup

- JDK 17.
- Android SDK, with `ANDROID_HOME` pointing at it.
- The `reseam` CLI, on `PATH` or at the path in `RESEAM_BIN`. Use the version that matches the plugin version in `settings.gradle.kts`. Releases ship Linux x64 and Windows x64 binaries; on other platforms [build it from source](https://reseam.app/docs/cli/install/).
- A signing key: `reseam bundle keygen --out ~/.reseam/bundle-signing.key`. It prints the public key users will trust. Keep the key file private and out of the repository.

## Layout

```text
manifest.toml                     bundle name, author, description
settings.gradle.kts               Reseam plugin version
apps/<app>/patch/                 Kotlin patches, one module per app
apps/<app>/extensions/<name>/     Kotlin code injected into the app
apps/<app>/settings/              in-app settings and translated strings (optional)
shared/<name>/                    extensions used by more than one app
```

Modules need no build script. The `app.reseam.workspace` plugin configures them from the layout. How to write patches is in [Your first patch](https://reseam.app/docs/authoring/first_patch/).

## Build and test

```shell
./gradlew bundle
reseam bundle list build/reseam/secp192k1-patches.reseam
reseam patch app.apk \
  --bundle build/reseam/secp192k1-patches.reseam \
  --trust <public key> \
  --enable "<Patch name>" \
  --output patched.apk
```

## Release

Every push to `main` runs `.github/workflows/release.yml`. [semantic-release](https://github.com/semantic-release/semantic-release) picks the next version from the [Conventional Commits](https://www.conventionalcommits.org/) since the last tag. A `fix` commit bumps the patch version, a `feat` commit the minor version, and a breaking change the major version. Other commit types do not release. The workflow then builds and signs the bundle, publishes it with [`reseam release patches`](https://reseam.app/docs/cli/release/), rewrites the patch list above, and commits the updated `package.json` and `README.md` to `main`.

Add the signing key as the repository secret `BUNDLE_SIGNING_KEY_B64`: `base64 -w0 ~/.reseam/bundle-signing.key`.

The workflow also creates [GitHub build provenance attestations](https://docs.github.com/en/actions/how-tos/secure-your-work/use-artifact-attestations/use-artifact-attestations) for `secp192k1-patches.reseam`. Verify a downloaded bundle with the GitHub CLI:

```shell
gh attestation verify secp192k1-patches.reseam --repo secp192k1/reseam-patches \
  --signer-workflow secp192k1/reseam-patches/.github/workflows/release.yml
```

When you move to a new Reseam version, bump the plugin version in `settings.gradle.kts`. The release workflow downloads the CLI of that same version.

## License

AGPL-3.0-or-later, with additional terms under section 7 in [NOTICE](NOTICE).
