package com.amefure.minnanotanjyoubi.DI

import com.amefure.minnanotanjyoubi.Repository.RootRepository
import com.amefure.minnanotanjyoubi.Repository.RootRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt に対して Repository 関連の依存関係をどのように提供するかを定義するクラス
 *
 * このモジュールはアプリケーション全体（Singletonスコープ）で有効
 * RootRepository（インターフェース）を要求されたときに、RootRepositoryImpl（実装）を注入することを定義
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideRootRepository(
        impl: RootRepositoryImpl
    ): RootRepository = impl
}
