package el.ka.customcheckboxes

import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener {
    companion object {
        const val DEFAULT_PAUSED_TIME = -1
        const val SECOND = 1000

        enum class ModePlaying {
            Toggle, Playing
        }

        enum class ModeChanging {
            Next, Previous
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var pausedTime = DEFAULT_PAUSED_TIME
    private var currentSong = 0
    private lateinit var songs: MutableList<Song>

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MAIN_ACTIVITY = this

        initMediaPlayer()
        initSongs()
        initBtnListeners()
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setOnPreparedListener(this)
    }

    private fun initBtnListeners() {
        btn_next.setOnClickListener { changeSong(ModeChanging.Next) }
        btn_prev.setOnClickListener { changeSong(ModeChanging.Previous) }

        btn_toggle_play.setOnClickListener {
            changeTogglePlayIcon(ModePlaying.Toggle)
            startPlayingSong(songs[currentSong])
        }
    }

    private fun changeTogglePlayIcon(mode: ModePlaying) {
        var res = R.drawable.ic_play

        if (mode == ModePlaying.Toggle) {
            res = if (mediaPlayer.isPlaying) R.drawable.ic_play else R.drawable.ic_pause
        } else if (mode == ModePlaying.Playing) {
            res = R.drawable.ic_pause
        }
        btn_toggle_play.setImageResource(res)
    }

    private fun startPlayingSong(song: Song) {
        if (pausedTime != DEFAULT_PAUSED_TIME) {
            continueSong()
        } else {
            startSong(song)
        }
    }

    private fun continueSong() {
        mediaPlayer.seekTo((progress_bar.progress * SECOND))
        mediaPlayer.start()
    }

    private fun startSong(song: Song) {
        this.current_name.text = song.name

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.setDataSource(this, song.uri)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    private fun changeSong(mode: ModeChanging) {
        currentSong = when (mode) {
            ModeChanging.Next -> {
                if (currentSong == songs.size - 1) 0 else currentSong + 1
            }

            ModeChanging.Previous -> {
                if (currentSong == 0) songs.size - 1 else currentSong - 1
            }
        }

        pausedTime = DEFAULT_PAUSED_TIME
        changeTogglePlayIcon(ModePlaying.Playing)
        startPlayingSong(songs[currentSong])
    }

    private fun initSongs() {
        songs = getSongs()
        currentSong = 0
    }

    override fun onPrepared(p0: MediaPlayer?) {
        initSeekBar()
        updateSeekBar()
    }

    private val MediaPlayer.currentTime: Int
        get() = this.currentPosition / 1000
    private val MediaPlayer.totalTime: Int
        get() = this.duration / 1000

    private fun Int.toTime(): String {
        val withoutHours = this % 3600
        val minutes = withoutHours / 60
        val seconds = withoutHours - minutes * 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    private fun updateSeekBar() {
        runnable = Runnable {
            val progress = mediaPlayer.currentTime
            progress_bar.progress = progress
            text_current_time.text = progress.toTime()
            handler.postDelayed(runnable, SECOND.toLong())
        }
        handler.postDelayed(runnable, SECOND.toLong())
    }

    private fun initSeekBar() {
        val totalTime = mediaPlayer.totalTime
        text_total_time.text = totalTime.toTime()

        progress_bar.max = totalTime
    }
}

// TODO request permission
// TODO set listener when seek bar changing
// TODO play next song when current song is end