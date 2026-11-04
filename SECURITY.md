# Security policy

## What counts as a security issue

- A patch that sends unintended data to a remote service.
- A patch that weakens the transport security of the application it patches.
- A build artefact that does not match the source in this repository.
- A leaked signing key or registry token.

## How to report

Report privately through GitHub's security advisory feature on this repository.
Do not open a public issue.

Include the bundle version, the application version, and the steps that reproduce
the problem. A captured request or a log is more useful than a description.

## What to expect

This is a single-maintainer project. Reports are acknowledged as soon as they are
seen. If a release is affected it is marked as such, and a fixed version is
published with an explanation.

## Signing

Releases are intended to be signed, so that a modified artefact can be refused by
the manager. The public key is committed to this repository. The private key is
never committed and is stored as an encrypted repository secret with an offline
backup.
