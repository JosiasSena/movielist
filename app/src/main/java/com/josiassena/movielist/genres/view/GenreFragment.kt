package com.josiassena.movielist.genres.view

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hannesdorfmann.mosby.mvp.MvpFragment
import com.josiassena.core.Genres
import com.josiassena.movielist.R
import com.josiassena.movielist.app.App
import com.josiassena.movielist.app_helpers.listener.SearchObservable
import com.josiassena.movielist.app_helpers.observers.fragment.GenreLifeCycleObserver
import com.josiassena.movielist.genres.presenter.GenrePresenterImpl
import com.josiassena.movielist.genres.view.rec_view.GenresAdapter
import com.josiassena.helpers.extensions.hide
import com.josiassena.helpers.extensions.show
import com.josiassena.helpers.extensions.showLongSnackBar
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_genre.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.util.concurrent.TimeUnit

class GenreFragment : MvpFragment<GenreView, GenrePresenterImpl>(), GenreView, AnkoLogger {

    private val genresAdapter = GenresAdapter()

    private var genresRetrieved: Genres? = null
    private var isShouldExecuteGenreSearchQuery = false

    companion object {
        private const val GENRES_KEY = "genres_key"

        fun newInstance() = GenreFragment()
    }

    override fun createPresenter() = GenrePresenterImpl()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_genre, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        App.component.inject(this)

        savedInstanceState?.let { genresRetrieved = it.getParcelable(GENRES_KEY) }

        super.onViewCreated(view, savedInstanceState)

        lifecycle.addObserver(GenreLifeCycleObserver)

        initRecView()

        getGenres()

        refreshLayout.setOnRefreshListener { presenter.getGenres() }

        tvNoGenres.setOnClickListener { presenter.checkIsNetworkAvailable() }

        listenToSearchViewChanges()
    }

    private fun getGenres() {
        if (genresRetrieved != null) {
            showRecyclerView()

            genresRetrieved?.let {
                genresAdapter.submitList(it.genres)
            }
        } else {
            presenter.getGenres()
        }
    }

    private fun listenToSearchViewChanges() {
        SearchObservable.fromView(activity?.genreSearchView)
                .subscribeOn(Schedulers.io())
                .filter {
                    if (isShouldExecuteGenreSearchQuery) {
                        return@filter true
                    } else {
                        isShouldExecuteGenreSearchQuery = true
                        return@filter false
                    }
                }
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<String> {
                    override fun onSubscribe(disposable: Disposable) {
                        GenreLifeCycleObserver.getCompositeDisposable().add(disposable)
                    }

                    override fun onError(throwable: Throwable) {
                        error(throwable.message, throwable)
                    }

                    override fun onNext(query: String) {
                        presenter.getGenresFromQuery(query)
                    }

                    override fun onComplete() {
                    }
                })
    }

    private fun initRecView() {
        rvGenre.adapter = genresAdapter
        rvGenre.layoutManager = GridLayoutManager(context, 2)
        rvGenre.itemAnimator = LandingAnimator()
        rvGenre.setItemViewCacheSize(50)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run { putParcelable(GENRES_KEY, genresRetrieved) }
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        hideLoading()
    }

    override fun displayGenres(genres: Genres) {
        genresRetrieved = genres

        showRecyclerView()

        genresAdapter.submitList(genres.genres)
    }

    override fun showRecyclerView() {
        tvNoGenres.hide()
        rvGenre.show()
    }

    override fun showEmptyStateView() {
        rvGenre.hide()
        tvNoGenres.show()
    }

    override fun showLoading() {
        refreshLayout.isRefreshing = true
    }

    override fun hideLoading() {
        refreshLayout.isRefreshing = false
    }

    override fun showNoInternetConnectionError() {
        context?.showLongSnackBar(rvGenre, R.string.no_internet)
    }
}
