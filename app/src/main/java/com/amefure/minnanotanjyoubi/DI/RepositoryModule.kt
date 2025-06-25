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
@Module // Hiltに依存性提供の定義をする場所であることを示す
@InstallIn(SingletonComponent::class) // アプリ全体で共有する依存性（シングルトン）であることを示す
object RepositoryModule {

    @Provides // 対象のインターフェースにどの実態を渡すかを定義する
    fun provideRootRepository(
        impl: RootRepositoryImpl
    ): RootRepository = impl
}
