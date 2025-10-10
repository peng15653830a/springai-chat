Cleanup summary (2025-09-26)

- Removed unused DTO classes (no code references):
  - `com.example.dto.request.ChatExecutionParams`
  - `com.example.dto.request.StructuredOutputRequest`
  - `com.example.dto.request.ChatRequest`

- Added Maven profile `code-cleanup` for static analysis:
  - `spotbugs-maven-plugin` (XML report at `target/spotbugsXml.xml`)
  - `maven-compiler-plugin` with `-Xlint:all` (warnings enabled during cleanup runs)

- Notes
  - The detection is conservative to avoid deleting Spring-managed components (annotated classes) which are used via reflection.
  - For broader removal (entire classes not referenced in code), prefer validating with a full build and a manual smoke test.
