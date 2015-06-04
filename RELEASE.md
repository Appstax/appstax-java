# Release

Here's how you publish a new version of the Java SDK:

1. Push all commits to GitHub.
1. Check that the last [commit build is passing](https://travis-ci.org/Appstax/appstax-java) on Travis.
1. Check that you've set the env vars `BINTRAY_USER` and `BINTRAY_KEY`.
1. Publish the new version: `./scripts/release`.
