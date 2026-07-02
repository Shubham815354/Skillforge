package com.example.skillforge.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.skillforge.data.model.Category
import com.example.skillforge.data.model.Course
import com.example.skillforge.data.model.Lesson
import com.example.skillforge.data.repository.CourseRepository
import com.example.skillforge.data.repository.DefaultCourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(val categories: List<Category>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class CatalogViewModel(
    private val repository: CourseRepository = DefaultCourseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    fun loadCatalog() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            _uiState.value = try {
                CatalogUiState.Success(repository.getCatalog().categories)
            } catch (e: IOException) {
                CatalogUiState.Error("Couldn't connect. Check your internet connection and try again.")
            } catch (e: Exception) {
                CatalogUiState.Error("Something went wrong while loading courses.")
            }
        }
    }

    fun findCourse(courseId: String): Course? {
        val state = _uiState.value
        if (state !is CatalogUiState.Success) return null
        return state.categories.asSequence()
            .flatMap { it.courses }
            .firstOrNull { it.id == courseId }
    }

    fun findLesson(courseId: String, lessonId: String): Pair<Course, Lesson>? {
        val course = findCourse(courseId) ?: return null
        val lesson = course.lessons.firstOrNull { it.id == lessonId } ?: return null
        return course to lesson
    }

    fun categories(): List<Category> {
        val state = _uiState.value
        return if (state is CatalogUiState.Success) state.categories else emptyList()
    }

    fun allCourses(): List<Course> = categories().flatMap { it.courses }

    fun coursesForCategory(categoryId: String): List<Course> =
        categories().firstOrNull { it.id == categoryId }?.courses.orEmpty()

    companion object {
        // A Kotlin constructor with a default-value parameter has no real zero-arg
        // constructor in bytecode, so the default reflection-based ViewModelProvider
        // factory can't instantiate this class. Provide an explicit factory instead.
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CatalogViewModel() as T
            }
        }
    }
}
