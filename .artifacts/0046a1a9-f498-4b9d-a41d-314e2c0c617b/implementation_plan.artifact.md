# Implementation Plan - Fix Resource Parsing Error (Unicode Path Issue)

The build failure `Failed to parse XML resource file ... values.xml` followed by `NoSuchFileException` is caused by a Unicode normalization mismatch in the project path. The project is located in a folder named `Tài liệu` (Vietnamese for "Documents") on OneDrive. The Android Gradle Plugin's resource parser is likely using a different Unicode normalization (Precomposed vs Decomposed) than the filesystem or Gradle, leading to a path resolution failure on Windows.

## User Review Required

> [!IMPORTANT]
> The root cause is the project's location in a folder with Vietnamese characters (`Tài liệu`) on OneDrive. OneDrive synchronization and Unicode normalization issues on Windows often cause these "File Not Found" errors during Android builds.

> [!TIP]
> **Recommended Solution:** Move the entire project folder to a path without special characters or spaces, such as `C:\AndroidProjects\HabitFlow`. This will permanently resolve most path-related build issues.

## Proposed Changes

### Build Configuration

#### [MODIFY] [gradle.properties](file:///C:/Users/lieuv/OneDrive/Tài liệu/filehoctap/LapTrinhAndroid/LAPTRINHTHIETBIDIDONG/Code/HabitFlow/gradle.properties)
- Ensure `file.encoding=UTF-8` is correctly set (already present).
- Keep `android.overridePathCheck=true` as it's required for projects in non-ASCII paths.

#### [NEW] [.gitignore](file:///C:/Users/lieuv/OneDrive/Tài liệu/filehoctap/LapTrinhAndroid/LAPTRINHTHIETBIDIDONG/Code/HabitFlow/.gitignore)
- Create a comprehensive `.gitignore` file.
- **Why:** Developing inside a OneDrive folder without ignoring the `build/` directory causes OneDrive to sync thousands of intermediate files, which leads to file locks and path errors.

### Workaround: Redirect Build Directory (If necessary)

If the `.gitignore` and a clean build do not resolve the issue, I will propose redirecting the build output to a "safe" path (e.g., `C:\build-temp\HabitFlow`) to bypass the problematic parent folder name.

## Verification Plan

### Automated Tests
- Run `./gradlew clean :app:parseDebugLocalResources` to verify the specific task that failed.
- Run `./gradlew assembleDebug` to ensure the full build succeeds.

### Manual Verification
- Check the `app/build/intermediates` directory to ensure `values.xml` is correctly generated and accessible.
