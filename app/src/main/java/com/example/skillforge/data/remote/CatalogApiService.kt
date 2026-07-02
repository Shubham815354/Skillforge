package com.example.skillforge.data.remote

import com.example.skillforge.data.model.CatalogResponse
import retrofit2.http.GET

interface CatalogApiService {
    @GET("android-assesment/notes/refs/heads/main/data.json")
    suspend fun getCatalog(): CatalogResponse
}
