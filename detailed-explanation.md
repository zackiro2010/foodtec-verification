# FoodTec Verification Service - CI/CD Documentation

This document describes the Continuous Integration and Continuous Delivery (CI/CD) implementation for the FoodTec Verification Service.

## CI/CD Architecture

The project uses **GitHub Actions** for automated build, test, and delivery processes.

### 1. Continuous Integration (CI)
- **Workflow File:** `.github/workflows/pr-build.yml`
- **Trigger:** Any Pull Request opened or updated against `master` or `main`.
- **Purpose:** 
    - Verify that new code compiles.
    - Run all unit tests.
    - Enforce a **90% code coverage** threshold (via JaCoCo).
- **Outcome:** PRs that fail to meet these standards are blocked from merging.

### 2. Continuous Delivery (CD)
- **Workflow File:** `.github/workflows/cd-pipeline.yml`
- **Trigger:** Any push (merge) to the `master` or `main` branches.
- **Purpose:** 
    - Re-verify the build and test suite on the base branch.
    - Package the Spring Boot services into executable JAR files.
    - Upload the JAR files as build artifacts to GitHub.
- **Future Steps:** This pipeline can be extended to deploy the JARs to a cloud environment (e.g., AWS, Azure, GCP) or build and push Docker images to a container registry.

## Quality Gates

- **Unit Tests:** JUnit 5 tests must pass for both Gateway and Inventory services.
- **Code Coverage:** JaCoCo is configured to fail the build if the aggregate coverage is below 90%.
- **Static Analysis:** The Gradle `check` task is integrated into the CI/CD pipeline.

## Build Artifacts

After a successful run of the CD pipeline, the following artifacts are generated:
- `gateway-service.jar`
- `inventory-service.jar`

These can be downloaded from the "Actions" tab in the GitHub repository.
