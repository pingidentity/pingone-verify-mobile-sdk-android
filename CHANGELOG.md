# Changelog

## v4.0.1 - July 1st, 2026

### Fixed

- Verify v4 SDK bug-fixes
- IdCapture SDK dependency .pom file added

## v4.0.0 - June 27th, 2026

Major release with a redesigned public API and open-source VerifyUI. Contains breaking changes.

- For integration guides and quick starts, see [`README.md`](./README.md).
- For a full list of API changes and upgrade steps, see [`MIGRATION.md`](./MIGRATION.md).
- For a complete public API reference, see [`Class Reference.md`](./Class%20Reference.md).

## v3.2.0 - April 28th, 2026

### Added

- Location collection UX handled in verification flow for GeoLocation enabled verify policy

### Fixed

- Crash fixes during document capture retry flow

## v3.1.6 - April 15th, 2026

### Fixed

- License updated to restore selfie capture functionality

## v3.1.4 - February 26th, 2026

### Fixed

- Microblink document payload handling fixes
- Microblink SDK defined in build.gradle instead of .aar dependency

## v3.1.3 - February 24th, 2026

### Fixed

- IDR&D selfie optional-checks handled 

## v3.1.2 - February 12th, 2026

### Fixed

- MicroBlink payload updated
- Document upload retry UX improvements
- Bug-fixes & stability improvements
- Removed redundant certificate dependency

## v3.1.1 - January 13th, 2026

### Fixed

- BlinkID License key updated

## v3.1.0 - January 15th, 2026

### Fixed

- BlinkID Version changed to 7.6.1

## v3.0.1 - January 14th, 2026

### Hotfix

- Indonesian Government ID expiry-date bug fixed

## v3.0.0 - December 15th, 2025

### Added

- Selfie Payload Size made configurable
- Voice verification support has been removed. This feature is no longer supported and has been discontinued in this release.

### Fixed

- Retry UI: Redundant cancel button causing DocumentSubmissionError has been removed
- Support for the legacy selfieCaptureSettings has been discontinued.
- UI/UX Improvements

## v2.3.7 - April 15th, 2026

### Hotfix

- License updated to restore selfie capture functionality

### Fixed

- Crash due to license issue in Selfie capture is fixed

## v2.3.6 - November 29th, 2025

### Fixed

- App Navigation handling bug fixes
- UI/UX Improvements

## v2.3.5 - November 20th, 2025

### Fixed

- BackActionHandler null-check added

## v2.3.4 - November 8th, 2025

### Fixed

- AppEvent missing events logged
- Localization text issues fixed
- Branding theme bug fixes
- UI/UX Improvements

## v2.3.3 - October 29th, 2025

### Fixed

- Branding theme iconTint handling fixed

## v2.3.0 - August 25th, 2025

### Added

- Localization support through PingOne
- Strengthened security for document uploads
- Support for Aadhaar Verification
- Improvements to support backend driven flows
- Support for selfie authentication mode

### Fixed

- Minor changes to improve UI/UX
- Defects related to verification flows
