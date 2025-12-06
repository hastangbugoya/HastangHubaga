package com.example.hastanghubaga.widget

import com.example.hastanghubaga.domain.repository.supplement.SupplementRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@EntryPoint
@InstallIn(SingletonComponent::class)
interface SupplementWidgetEntryPoint {
    fun supplementRepository(): SupplementRepository
}