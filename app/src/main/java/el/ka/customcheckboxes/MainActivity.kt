package el.ka.customcheckboxes

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
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
    private var isChangingProgress = false

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MAIN_ACTIVITY = this

        initMediaPlayer()
        initSongs()
        initBtnListeners()
        progress_bar.setOnSeekBarChangeListener(this)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
    }

    private fun initBtnListeners() {
        btn_next.setOnClickListener { changeSong(ModeChanging.Next) }
        btn_prev.setOnClickListener { changeSong(ModeChanging.Previous) }

        btn_toggle_play.setOnClickListener {
            changeTogglePlayIcon(ModePlaying.Toggle)
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                pausedTime = mediaPlayer.currentTime
            } else {
                startPlayingSong(songs[currentSong])
            }
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
        changeTogglePlayIcon(ModePlaying.Playing)
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
        progress_bar.progress = 0
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
            text_current_time.text = progress.toTime()

            if (!isChangingProgress) {
                progress_bar.progress = progress
            }

            handler.postDelayed(runnable, SECOND.toLong())
        }
        handler.postDelayed(runnable, SECOND.toLong())
    }

    private fun initSeekBar() {
        val totalTime = mediaPlayer.totalTime
        text_total_time.text = totalTime.toTime()

        progress_bar.max = totalTime
    }

    override fun onCompletion(p0: MediaPlayer?) {
        changeSong(ModeChanging.Next)
    }

    // Progress bar - start
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            mediaPlayer.seekTo(progress * SECOND)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        isChangingProgress = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        isChangingProgress = false
        continueSong()
    }
    // Progress bar - end
}

// TODO request permission
// TODO create class for player
