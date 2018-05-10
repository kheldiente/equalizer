package midien.kheldiente.equalizer

import android.Manifest
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat



class MainActivity : AppCompatActivity() {

    private val PERMISSION_RECORD_AUDIO_REQUEST_CODE = 88

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
        setupPermissions()
    }

    private fun setup() {
        mediaPlayer?.let {
            mediaPlayer = MediaPlayer.create(this, R.raw.htmlthesong)
            mediaPlayer?.isLooping = true
        }
    }

    private fun startMediaPlayer() {
        mediaPlayer?.isPlaying?.let {
            if(!it!!)
                mediaPlayer?.start()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.isPlaying?.let {
            if(it!!)
                mediaPlayer?.stop()

            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    /**
     * App Permissions for Audio
     */
    private fun setupPermissions() {
        // If we don't have the record audio permission...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                val permissionsWeNeed = arrayOf(Manifest.permission.RECORD_AUDIO)
                requestPermissions(permissionsWeNeed, PERMISSION_RECORD_AUDIO_REQUEST_CODE)
            }
        } else {
            startMediaPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        setup()
        startMediaPlayer()
    }

    override fun onStop() {
        super.onStop()
        stopMediaPlayer()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_RECORD_AUDIO_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The permission was granted! Start up the visualizer!
                    startMediaPlayer()
                } else {
                    Toast.makeText(this, "Permission for audio not granted. Visualizer can't run.", Toast.LENGTH_LONG).show()
                    finish()
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }
        }
        // Other permissions could go down here
    }


}
