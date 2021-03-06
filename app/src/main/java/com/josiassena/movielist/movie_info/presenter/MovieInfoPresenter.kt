package com.josiassena.movielist.movie_info.presenter

import android.app.DownloadManager
import com.hannesdorfmann.mosby.mvp.MvpPresenter
import com.josiassena.core.MovieVideosResult
import com.josiassena.core.Result
import com.josiassena.movielist.movie_info.view.MovieInfoView
import io.reactivex.Maybe

/**
 * @author Josias Sena
 */
interface MovieInfoPresenter : MvpPresenter<MovieInfoView> {
    fun getPreviewsForMovieFromId(movieId: Int)
    fun getMovieFromId(movieId: Int): Maybe<Result>
    fun unSubscribe()
    fun playVideoFromPreview(preview: MovieVideosResult)
    fun downloadMoviePoster(request: DownloadManager.Request)
    fun addMovieToFavorites(movieId: Int)
    fun checkIfIsFavoriteMovie(movieId: Int)
    fun removeMovieFromFavorites(movieId: Int)
}