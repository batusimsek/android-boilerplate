package com.adesso.movee.scene.movielist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adesso.movee.domain.FetchNowPlayingMoviesUseCase
import com.adesso.movee.internal.util.Failure
import com.adesso.movee.internal.util.functional.Either
import com.adesso.movee.scene.movielist.model.MovieUiModel
import com.adesso.movee.testutil.extansion.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class MovieListViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var fetchNowPlayingMoviesUseCase: FetchNowPlayingMoviesUseCase

    private lateinit var viewModel: MovieListViewModel

    private val mockMovieUiModelList = mockk<List<MovieUiModel>>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        coEvery {
            fetchNowPlayingMoviesUseCase.run(any())
        } returns Either.Right(mockMovieUiModelList)

        viewModel = MovieListViewModel(
            fetchNowPlayingMoviesUseCase = fetchNowPlayingMoviesUseCase
        )
    }

    @Test
    fun `verify fetching now playing movies when fetchNowPlayingMovies returns successfully`() = runBlocking {
        coVerify {
            fetchNowPlayingMoviesUseCase.run(any())
        }

        Assert.assertEquals(viewModel.nowPlayingMovies.getOrAwaitValue(), mockMovieUiModelList)
    }

    @Test
    fun `verify fetching now playing movies when fetchNowPlayingMovies returns failed`() = runBlocking {
        val mockFailure = mockk<Failure> {
            every { message } returns "errorMessage"
        }

        coEvery {
            fetchNowPlayingMoviesUseCase.run(any())
        } returns Either.Left(mockFailure)

        Assert.assertNull(viewModel.failurePopup.value)

        viewModel.fetchNowPlayingMovies()

        coVerify {
            fetchNowPlayingMoviesUseCase.run(any())
        }

        Assert.assertNotNull(viewModel.failurePopup.value)
    }
}
