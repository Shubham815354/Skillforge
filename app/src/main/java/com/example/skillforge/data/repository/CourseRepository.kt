package com.example.skillforge.data.repository

import com.example.skillforge.data.model.CatalogResponse
import com.example.skillforge.data.remote.CatalogApiService
import com.example.skillforge.data.remote.RetrofitInstance

interface CourseRepository {
    suspend fun getCatalog(): CatalogResponse
}

class DefaultCourseRepository(
    private val api: CatalogApiService = RetrofitInstance.api
) : CourseRepository {
    override suspend fun getCatalog(): CatalogResponse = api.getCatalog()
}
