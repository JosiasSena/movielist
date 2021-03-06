package com.josiassena.movielist.movies.presenter

import com.hannesdorfmann.mosby.mvp.MvpBasePresenter
import com.josiassena.core.MovieResults
import com.josiassena.helpers.network.NetworkManager
import com.josiassena.movieapi.Api
import com.josiassena.movielist.app.App
import com.josiassena.movielist.app_helpers.data_providers.movies.MovieProvider
import com.josiassena.movielist.app_helpers.data_providers.movies.MoviesNowPlayingProvider
import com.josiassena.movielist.app_helpers.data_providers.movies.TopRatedMoviesProvider
import com.josiassena.movielist.app_helpers.data_providers.movies.UpcomingMoviesProvider
import com.josiassena.movielist.movies.view.MoviesView
import io.reactivex.MaybeObserver
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import javax.inject.Inject

/**
 * @author Josias Sena
 */
class MoviesPresenterImpl : MvpBasePresenter<MoviesView>(), MoviesPresenter, AnkoLogger {

    private lateinit var moviesObserver: Observer<MovieResults?>

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var networkManager: NetworkManager

    @Inject
    lateinit var movieProvider: MovieProvider

    @Inject
    lateinit var topRatedMoviesProvider: TopRatedMoviesProvider

    @Inject
    lateinit var upcomingMoviesProvider: UpcomingMoviesProvider

    @Inject
    lateinit var moviesNowPlayingProvider: MoviesNowPlayingProvider

    init {
        App.component.inject(this)

        moviesObserver = object : Observer<MovieResults?> {

            override fun onSubscribe(disposable: Disposable) {
                MoviesDisposableLifeCycleObserver.getCompositeDisposable().add(disposable)
            }

            override fun onError(throwable: Throwable) {
                error(throwable.message, throwable)
            }

            override fun onNext(movies: MovieResults) {
                if (isViewAttached) {
                    view?.displayMovies(movies)
                }
            }

            override fun onComplete() {
            }
        }
    }

    override fun getMoviesForGenreId(genreId: Int) {
        if (isViewAttached) {
            view?.showLoading()
        }

        movieProvider.getMovies(genreId, object : MaybeObserver<MovieResults?> {

            override fun onSubscribe(disposable: Disposable) {
                MoviesDisposableLifeCycleObserver.getCompositeDisposable().add(disposable)
            }

            override fun onError(throwable: Throwable) {
                error(throwable.message, throwable)

                if (isViewAttached) {
                    view?.hideLoading()

                    if (!networkManager.isInternetConnectionAvailable()) {
                        view?.showEmptyStateView()
                    }
                }
            }

            override fun onSuccess(movieResults: MovieResults) {
                if (isViewAttached) {
                    view?.hideLoading()

                    if (movieResults.results.isEmpty()) {
                        if (!networkManager.isInternetConnectionAvailable()) {
                            view?.showEmptyStateView()
                        } else {
                            view?.showNoInternetConnectionError()
                        }
                    } else {
                        view?.displayMovies(movieResults)
                    }
                }
            }

            override fun onComplete() {
            }
        })
    }

    override fun getMoreMoviesForGenreId(genreId: Int, page: Int) {
        if (isViewAttached) {
            view?.showLoading()
        }

        movieProvider.getMoviesPaginated(genreId, page, object : MaybeObserver<MovieResults?> {

            override fun onSubscribe(disposable: Disposable) {
                MoviesDisposableLifeCycleObserver.getCompositeDisposable().add(disposable)
            }

            override fun onError(throwable: Throwable) {
                error(throwable.message, throwable)

                if (isViewAttached) {
                    view?.hideLoading()
                }
            }

            override fun onSuccess(movieResults: MovieResults) {

                if (isViewAttached) {
                    view?.hideLoading()

                    if (movieResults.results.isEmpty()) {
                        if (networkManager.isInternetConnectionAvailable()) {
                            view?.showEmptyStateView()
                        } else {
                            view?.showNoInternetConnectionError()
                        }
                    } else {
                        view?.addMoreMovies(movieResults)
                    }
                }
            }

            override fun onComplete() {
            }
        })
    }

    override fun getTopRatedMovies() = topRatedMoviesProvider.getTopRatedMovies(moviesObserver)

    override fun getUpcomingMovies() = upcomingMoviesProvider.getUpcomingMovies(moviesObserver)

    override fun getMoviesNowPlaying() = moviesNowPlayingProvider.getMoviesNowPlaying(moviesObserver)

}
