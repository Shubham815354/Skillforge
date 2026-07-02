# Skillforge

An Android learning app (Home → Categories/Popular Courses → Course Detail → Lesson) built with
Kotlin and traditional XML views, backed by a single remote JSON catalog.

## Stack

- **UI**: XML layouts wrapped in `<layout>` (Data Binding, used purely for typed view access — no
  `<variable>` tags or binding expressions in any layout; all view state is set programmatically in
  Kotlin, the same way you'd use ViewBinding)
- **Navigation**: Navigation Component (single Activity + 5 Fragments + `nav_graph.xml`)
- **Lists**: RecyclerView throughout, incl. a multi-view-type adapter for Home (categories carousel
  + course rows) and a shared `LessonAdapter`/`CourseRowAdapter`/`CategoryTileAdapter` reused across
  screens
- **Networking**: Retrofit + OkHttp + Gson, called from coroutines
- **Images**: Coil (`ImageView.load(url)`) — every image in the app (course thumbnails, instructor
  avatars) is loaded from the real API data, nothing hardcoded
- **State**: `ViewModel` + `StateFlow` (`Loading` / `Success` / `Error`), collected via
  `repeatOnLifecycle`, with pull-to-refresh and a retry button on failure
- **Theme**: Cream + teal palette, Plus Jakarta Sans (variable font), light theme only

## Screens

- **Home** — category tiles carousel (color pulled from the API's `iconColor` field), a searchable
  "Popular courses" list, "See all" for both
- **Categories** (See all) — full grid of categories
- **Course list** (See all) — flat list, optionally filtered by category
- **Course Detail** — hero image, rating/students/duration/level, instructor card (with a
  Follow/Following toggle), description, and the lesson list marking free vs. locked lessons
- **Lesson** — video-player-style header (thumbnail, play/pause, draggable seek bar) + a
  Lessons/Notes/Resources tab row, with the currently-open lesson highlighted in its own list

## Architecture

```
data/
  model/         Course, Category, Instructor, Lesson (mirrors data.json)
  remote/        Retrofit service + client, pointed at the raw GitHub JSON
  repository/    CourseRepository — thin interface over the API for testability
ui/
  catalog/       CatalogViewModel — single source of truth, exposes CatalogUiState
  home/          HomeFragment + HomeAdapter (categories section / course-list section)
  categorylist/  CategoryListFragment — "See all categories" grid
  courselist/    CourseListFragment — "See all courses" flat list, optional category filter
  coursedetail/  CourseDetailFragment
  lesson/        LessonFragment (mocked video player — the JSON's videoUrls are example.com
                 placeholders, so this is a play/pause + seekbar UI, not real playback)
  common/        LessonAdapter, CourseRowAdapter, CategoryTileAdapter — shared across screens
util/            Formatters, LevelColors (shared beginner/intermediate/advanced color mapping)
res/
  navigation/    nav_graph.xml
  layout/        fragment_*.xml (screens) + item_*.xml (RecyclerView rows), all <layout>-wrapped
  drawable/      Hand-written vector icons + shape backgrounds (chips, badges, circles, gradients)
  values/        colors.xml, themes.xml, text_appearances.xml, shape_styles.xml
```

One `CatalogViewModel`, scoped to the Activity via `activityViewModels()`, is shared across every
fragment, so the catalog is fetched once. `CourseDetailFragment` and `LessonFragment` look up the
course/lesson by ID (passed as a plain `Bundle` nav arg) from the already-loaded list.

`CatalogViewModel` exposes a `Factory` — its constructor has a default-value parameter, which
Kotlin does not compile into a real zero-arg constructor, so the default reflection-based
`ViewModelProvider` factory can't instantiate it. Use `by activityViewModels { CatalogViewModel.Factory }`,
not the parameterless `by activityViewModels()`.

## Running it

1. Open the project root in Android Studio and let Gradle sync.
2. Run the `app` configuration on an emulator or device (minSdk 24).
3. Debug APK from the command line: `./gradlew assembleDebug` (output in
   `app/build/outputs/apk/debug/`).

## Tests

- `FormattersTest` — duration/hour/student-count/clock formatting helpers.
- `CatalogViewModelTest` — loading → success/error transitions, course/lesson lookup, category and
  course flattening/filtering, using a fake `CourseRepository` and `kotlinx-coroutines-test`.

## How I used AI

**Tools used**: Claude Code (Claude, Anthropic), as a coding assistant. I directed the overall
structure and decisions — what screens to build, the switch from Compose to XML, which design to
match — and used it to speed up scaffolding, boilerplate, and layout iteration, reviewing and testing
the result at each step rather than accepting output blind.

**Example prompts I sent**:
1. *"check outh this link and make the exact app it wants with exact ui and api integration"* —
   pointed it at the assignment page and the live JSON API to start the build.
2. *"i dont want jetpack compose i want normal xml using <layout and no variable in xml"* — my call
   to redo the UI layer in XML + Data Binding instead of Compose, with Data Binding used strictly
   for typed view access (no `<variable>`/`@{}` expressions anywhere).
3. *"now match this page"* (with a screenshot attached) — sent once per screen to line up the Home,
   Course Detail, and Lesson screens against the design references I was given.

**Something it got right**: A Gradle sync failed with *"the plugin is already on the classpath with
an unknown version"* after I had it add several dependencies. Instead of guessing, it checked current
Google/JetBrains docs and correctly identified that AGP 9 now ships built-in Kotlin support that
conflicts with explicitly applying `org.jetbrains.kotlin.android`; I had it remove the redundant
plugin declaration, which matched Google's actual documented migration path.

**Something it got wrong (and how I fixed it)**: It introduced a launch crash in the category tile
icon code — `binding.tileIconBackground.drawable.mutate().setTint(...)` was first written as
`.background.mutate()`, but that view's color comes from `android:src`, not `android:background`, so
`.background` was `null` and the app crashed on first launch. I caught this from the crash log and
had it switch to `.drawable.mutate()`, the property that actually reflects `android:src`.
