package com.example.ei
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.OAuthProvider



class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main)

        /*
        * Genera una instancia de Firebase, esta es utilizada para autenticar al usuario con Google.
        * */
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        /*
        * Usa la función GoogleSignIn.getClient() para obtener un cliente de inicio de sesión de Google.
        * */
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInButton: SignInButton = findViewById(R.id.googleSignInButton)
        signInButton.setOnClickListener {
            signIn()
        }

        LoginManager.getInstance().registerCallback(callbackManager, object: FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult) {
                Toast.makeText(baseContext, "Authentication success.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, FingerprintAuthActivity::class.java)
                startActivity(intent)
            }
            override fun onCancel() {
                Toast.makeText(baseContext, "Authentication canceled.", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        })
        val facebookSignInButton: LoginButton = findViewById(R.id.facebookSignInButton)
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            callbackManager,
            listOf()
        )


        val githubSignInButton: Button = findViewById(R.id.githubSignInButton)
        githubSignInButton.setOnClickListener {
            signInWithGitHub()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //startActivity(Intent(this, TaskListActivity::class.java))
                    Toast.makeText(baseContext, "Authentication success.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, FingerprintAuthActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGitHub() {
        val provider = OAuthProvider.newBuilder("github.com")

        // Agregar scopes si es necesario (opcional)
        val scopes = listOf("user:username")
        provider.scopes = scopes

        var failure:Exception = Exception()
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener {
                    // El usuario ya está autenticado
                    Toast.makeText(this, "Authentication success.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, FingerprintAuthActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Authentication failed. Esta", Toast.LENGTH_SHORT).show()
                }
        } else {
            auth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    // Autenticación exitosa
                    Toast.makeText(this, "Authentication success.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, FingerprintAuthActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Authentication failed. Esta ${exception}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}