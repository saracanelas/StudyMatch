# StudyMatch
A full-stack platform for forming student groups using academic profiles and adaptive matching criteria.

## Development Workflow

All development follows the workflow below:

Issue → Branch → Implementation → Pull Request → Review → Merge

### Branches

The `main` branch contains the stable version of the project. Direct pushes to `main` are not allowed.

Branches should be created from `main` and follow these naming conventions:

- `feature/<short-description>` — new functionality
- `fix/<short-description>` — bug fixes
- `docs/<short-description>` — documentation changes
- `chore/<short-description>` — configuration and maintenance tasks

### Issues

Each task should have a GitHub Issue before implementation begins.

The Issue should be assigned to the responsible team member and labelled according to the type of work.

### Pull Requests

When the work is complete, a Pull Request must be opened from the working branch into `main`.

The Pull Request should:

- clearly describe the changes made;
- reference the related Issue;
- be reviewed by another team member;
- have all review comments resolved;
- pass the required GitHub Actions checks.

A team member must not approve their own Pull Request.

At least one meaningful review discussion should take place during the sprint.

### Merging

A Pull Request can be merged only after:

- the required review approval is obtained;
- all conversations are resolved;
- the GitHub Actions checks pass.

After merging, the feature branch should be deleted.
