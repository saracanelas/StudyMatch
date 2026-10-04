# StudyMatch
A full-stack platform for forming student groups using academic profiles and adaptive matching criteria.

## Continuous Integration

GitHub Actions is used to automatically verify pull requests to `main`.

The CI workflow:
- runs the backend tests with Maven;
- builds the frontend with npm.

Pull requests must pass the CI checks before being merged.
