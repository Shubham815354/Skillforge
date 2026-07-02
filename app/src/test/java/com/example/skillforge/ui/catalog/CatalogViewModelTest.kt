package com.example.skillforge.ui.catalog

import com.example.skillforge.data.model.CatalogResponse
import com.example.skillforge.data.model.Category
import com.example.skillforge.data.model.Course
import com.example.skillforge.data.model.Instructor
import com.example.skillforge.data.model.Lesson
import com.example.skillforge.data.model.Meta
import com.example.skillforge.data.repository.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleLesson(id: String) = Lesson(
        id = id,
        title = "Lesson $id",
        durationMinutes = 10,
        isFree = true,
        videoUrl = "https://example.com/v/$id",
        content = "Content for $id"
    )

    private fun sampleCourse(id: String) = Course(
        id = id,
        title = "Course $id",
        subtitle = "Subtitle",
        thumbnailUrl = "https://example.com/thumb.png",
        level = "Beginner",
        durationHours = 5.0,
        rating = 4.5,
        studentsEnrolled = 100,
        language = "English",
        lastUpdated = "2026-01-01",
        tags = emptyList(),
        instructor = Instructor("inst_1", "Jane Doe", "Engineer", "https://example.com/a.png", "Bio"),
        description = "Description",
        lessons = listOf(sampleLesson("les_1"), sampleLesson("les_2"))
    )

    private fun sampleResponse() = CatalogResponse(
        meta = Meta("Skillforge", "1.0", "2026-01-01"),
        categories = listOf(
            Category(
                id = "cat_1",
                name = "Category 1",
                description = "Desc",
                iconColor = "#000000",
                courseCount = 1,
                courses = listOf(sampleCourse("course_1"))
            ),
            Category(
                id = "cat_2",
                name = "Category 2",
                description = "Desc 2",
                iconColor = "#111111",
                courseCount = 1,
                courses = listOf(sampleCourse("course_2"))
            )
        )
    )

    private class FakeCourseRepository(
        private val response: CatalogResponse? = null,
        private val error: Exception? = null
    ) : CourseRepository {
        override suspend fun getCatalog(): CatalogResponse {
            error?.let { throw it }
            return response!!
        }
    }

    @Test
    fun `initial state is loading before catalog resolves`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(response = sampleResponse()))
        assertTrue(viewModel.uiState.value is CatalogUiState.Loading)
    }

    @Test
    fun `successful load exposes categories`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(response = sampleResponse()))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CatalogUiState.Success)
        assertEquals("cat_1", (state as CatalogUiState.Success).categories.first().id)
    }

    @Test
    fun `failed load exposes error state`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(error = java.io.IOException("offline")))
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CatalogUiState.Error)
    }

    @Test
    fun `findCourse returns match by id after load`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(response = sampleResponse()))
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("Course course_1", viewModel.findCourse("course_1")?.title)
        assertNull(viewModel.findCourse("missing"))
    }

    @Test
    fun `findLesson returns course and lesson pair`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(response = sampleResponse()))
        dispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.findLesson("course_1", "les_2")
        assertEquals("course_1", result?.first?.id)
        assertEquals("les_2", result?.second?.id)
    }

    @Test
    fun `categories returns loaded categories or empty list before load`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(response = sampleResponse()))
        assertTrue(viewModel.categories().isEmpty())

        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.categories().size)
    }

    @Test
    fun `allCourses flattens courses across every category`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(response = sampleResponse()))
        dispatcher.scheduler.advanceUntilIdle()

        val ids = viewModel.allCourses().map { it.id }
        assertEquals(listOf("course_1", "course_2"), ids)
    }

    @Test
    fun `coursesForCategory returns only that category's courses`() = runTest {
        val viewModel = CatalogViewModel(FakeCourseRepository(response = sampleResponse()))
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("course_2"), viewModel.coursesForCategory("cat_2").map { it.id })
        assertTrue(viewModel.coursesForCategory("missing").isEmpty())
    }
}
