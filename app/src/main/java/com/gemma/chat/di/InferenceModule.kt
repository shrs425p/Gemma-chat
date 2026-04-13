package com.gemma.chat.di

import com.gemma.chat.data.inference.GemmaInferenceEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InferenceModule {

    @Provides
    @Singleton
    fun provideGemmaInferenceEngine(): GemmaInferenceEngine = GemmaInferenceEngine()
}
