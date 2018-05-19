package com.josiassena.movielist.settings.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.api.ApiException
import com.hannesdorfmann.mosby.mvp.MvpActivity
import com.josiassena.movielist.R
import com.josiassena.movielist.app.App
import com.josiassena.movielist.settings.presenter.SettingsPresenter
import com.rapidsos.helpers.extensions.hide
import com.rapidsos.helpers.extensions.show
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_settings.*
import org.jetbrains.anko.longToast
import java.util.*
import javax.inject.Inject

class SettingsActivity : MvpActivity<View, SettingsPresenter>(), View {

    private val authUi by lazy { AuthUI.getInstance() }
    private val providers = Arrays.asList(AuthUI.IdpConfig.GoogleBuilder().build())

    @Inject
    lateinit var settingsPresenter: SettingsPresenter

    companion object {
        private const val TAG = "SettingsActivity"
        private const val RC_SIGN_IN = 5124
    }

    override fun createPresenter(): SettingsPresenter {
        return settingsPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        App.component.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStart() {
        super.onStart()

        presenter.updateCurrentUserData(presenter.getCurrentUser())

        btnGoogleSignIn.setOnClickListener { signIn() }
        btnSignOut.setOnClickListener { signOut() }
    }

    private fun signIn() {
        val signInIntent = authUi.createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val currentUser = presenter.getCurrentUser()

                    longToast("Signed in successfully. Welcome ${currentUser?.displayName}")

                    presenter.updateCurrentUserData(currentUser)

                    showSignOutButton()

                } catch (apiException: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.e(TAG, "Google sign in failed.", apiException)
                }
            } else {
                if (response == null) {
                    Log.e(TAG, "Google sign in failed: Canceled by user.")
                } else {
                    val error = response.error

                    Log.e(TAG, "Google sign in failed with error code: ${error?.errorCode}")
                    Log.e(TAG, "Google sign in failed: ${error?.localizedMessage}")
                }
            }
        }
    }

    private fun signOut() {
        authUi.signOut(this)
                .addOnCompleteListener {

                    showSignInButton()

                    presenter.onSignedOut()
                }
    }

    override fun showSignOutButton() {
        btnGoogleSignIn.hide()
        btnSignOut.show()
    }

    override fun showSignInButton() {
        btnSignOut.hide()
        btnGoogleSignIn.show()
    }

}