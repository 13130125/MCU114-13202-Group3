package com.example.thread // 確保這是您的 package 名稱

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel // <-- 不再需要這個

// 1. 匯入 AndroidViewModel 和 Application
class WorkViewModel(app: Application) : AndroidViewModel(app) { // 2. 將 ViewModel() 改為 AndroidViewModel(app)

    private val handlerThread = HandlerThread("VM-Work").apply { start() }
    private val worker = Handler(handlerThread.looper)

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _status = MutableLiveData("Idle")
    val status: LiveData<String> = _status

    @Volatile private var running = false

    // 3. 新增一個 MediaPlayer 變數來控制音樂
    private var mediaPlayer: MediaPlayer? = null

    // 4. (核心) 新增一個私有的 stopMusic() 函式，用於停止和釋放音樂資源
    private fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {
            // 忽略停止時可能發生的錯誤
        }
    }

    fun start() {
        if (running) return
        running = true
        _status.postValue("Preparing…")
        _progress.postValue(0)
        worker.post {
            try {
                Thread.sleep(10000) // 準備工作
                _status.postValue("Working…")

                // 5. (核心) 當工作開始時，播放音樂
                val context = getApplication<Application>().applicationContext
                stopMusic() // 先停止任何可能在播放的舊音樂
                mediaPlayer = MediaPlayer.create(context, R.raw.background_music)
                mediaPlayer?.isLooping = true // 讓背景音樂循環播放
                mediaPlayer?.start()

                for (i in 1..100) {
                    if (!running) break
                    Thread.sleep(3500) // 真正的背景工作
                    _progress.postValue(i)
                }

                // 6. (核心) 當工作完成時，停止音樂
                stopMusic()
                _status.postValue(if (running) "背景工作結朿！" else "Canceled")
                running = false
            } catch (_: InterruptedException) {
                // 7. (核心) 當工作被中斷時 (例如 Sleep 被打斷)，也要停止音樂
                stopMusic()
                _status.postValue("Canceled")
                running = false
            }
        }
    }

    fun cancel() {
        running = false
        // 注意：我們不需要在 "cancel()" 中呼叫 stopMusic()，
        // 因為 "worker" 執行緒中的迴圈會自行偵測到 running == false，
        // 並在跳出迴圈後執行第 6 點的 stopMusic()。
    }

    override fun onCleared() {
        running = false
        handlerThread.quitSafely()

        // 8. (重要) 當 ViewModel 被銷毀時 (例如 App 關閉)，
        // 必須停止音樂，否則音樂會一直在背景播放！
        stopMusic()
        super.onCleared()
    }
}