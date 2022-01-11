package com.example.exoplayertut

import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.exoplayertut.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.AdsConfiguration
import com.google.android.exoplayer2.MediaItem.Builder
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.AnalyticsListener.EventTime
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.common.collect.ImmutableList

const val TAG = "EXOPLAYER TEST"

class MainActivity : AppCompatActivity() {

    var url = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    var exoPlayer: ExoPlayer? = null
    lateinit var binding: ActivityMainBinding
    private var adsLoader: ImaAdsLoader? = null

    private val playerEventListener: Player.Listener = object : Player.Listener {

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            Log.d(TAG, "onMediaMetadataChanged: ${mediaMetadata.albumTitle}")

        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            when (playbackState) {
                Player.STATE_IDLE -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_IDLE")
                }
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_BUFFERING")
                }
                Player.STATE_READY -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_READY")
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "onPlayerStateChanged: STATE_ENDED")
                }
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Create an AdsLoader.
        adsLoader = ImaAdsLoader.Builder( /* context= */this).build()

    }

    private fun initializePlayer() {
        // Set up the factory for media sources, passing the ads loader and ad view providers.

//        val cronetDataSourceFactory: CronetDataSource.Factory = Factory(cronetEngine, executor)

        val dataSourceFactory: DataSource.Factory =
            DefaultDataSource.Factory(this)

        val mediaSourceFactory: MediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            .setAdsLoaderProvider { adsLoader }
            .setAdViewProvider(binding.exoplayer)

        // Create a SimpleExoPlayer and set it as the player for content and ads.
        exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        exoPlayer!!.addListener(playerEventListener)

        binding.exoplayer.player = exoPlayer
        adsLoader!!.setPlayer(exoPlayer)

        // Create the MediaItem to play, specifying the content URI and advertisement tag URI.
        val contentUri: Uri = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4".toUri()
        val adTagUri: Uri =
            Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=")

        val mediaItem: MediaItem = Builder()
            .setUri(contentUri)
            .setAdsConfiguration(AdsConfiguration.Builder(adTagUri).build())
            .build()

        // Prepare the content and ad to be played with the SimpleExoPlayer.
        exoPlayer!!.setMediaItem(mediaItem)
        exoPlayer!!.prepare()

        // Set PlayWhenReady. If true, content and ads will autoplay.
        exoPlayer!!.playWhenReady = false
    }

    // todo(HLS media) Use the explicit MIME type to build an HLS media item.
    private fun setHLSUriInExoPlayer() {

        val mediaItem: MediaItem = Builder()
            .setUri("https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8")
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()

        //HLS file format
        /*

        \#EXTM3U

     \#EXT-X-VERSION:5

    \#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio",NAME="English stereo",LANGUAGE="en",AUTOSELECT=YES,URI="f08e80da-bf1d-4e3d-8899-f0f6155f6efa_audio_1_stereo_128000.m3u8"

    \#EXT-X-STREAM-INF:BANDWIDTH=628000,CODECS="avc1.42c00d,mp4a.40.2",RESOLUTION=320x180,AUDIO="audio"

f08e80da-bf1d-4e3d-8899-f0f6155f6efa_video_180_250000.m3u8

\#EXT-X-STREAM-INF:BANDWIDTH=928000,CODECS="avc1.42c00d,mp4a.40.2",RESOLUTION=480x270,AUDIO="audio"

f08e80da-bf1d-4e3d-8899-f0f6155f6efa_video_270_400000.m3u8

\#EXT-X-STREAM-INF:BANDWIDTH=1728000,CODECS="avc1.42c00d,mp4a.40.2",RESOLUTION=640x360,AUDIO="audio"

f08e80da-bf1d-4e3d-8899-f0f6155f6efa_video_360_800000.m3u8

\#EXT-X-STREAM-INF:BANDWIDTH=2528000,CODECS="avc1.42c00d,mp4a.40.2",RESOLUTION=960x540,AUDIO="audio"

f08e80da-bf1d-4e3d-8899-f0f6155f6efa_video_540_1200000.m3u8

\#EXT-X-STREAM-INF:BANDWIDTH=4928000,CODECS="avc1.42c00d,mp4a.40.2",RESOLUTION=1280x720,AUDIO="audio"

f08e80da-bf1d-4e3d-8899-f0f6155f6efa_video_720_2400000.m3u8

\#EXT-X-STREAM-INF:BANDWIDTH=9728000,CODECS="avc1.42c00d,mp4a.40.2",RESOLUTION=1920x1080,AUDIO="audio"

f08e80da-bf1d-4e3d-8899-f0f6155f6efa_video_1080_4800000.m3u8

         */
    }

    private fun addMultipleItemToExoplayer() {
        // Build the media items.
        val firstItem: MediaItem = MediaItem.fromUri(url)
        val secondItem: MediaItem = MediaItem.fromUri(url)
        // Add the media items to be played.
        exoPlayer!!.addMediaItem(firstItem)
        exoPlayer!!.addMediaItem(secondItem)
        // Prepare the player.
        exoPlayer!!.prepare()
        // Start the playback.
        exoPlayer!!.play()
    }

    //TODO to dynamically modify a playlist by adding, moving and removing media item
    private fun modifyingPlayListDynamically() {
        // Adds a media item at position 1 in the playlist.
        exoPlayer!!.addMediaItem(/* index= */ 1, MediaItem.fromUri(url));
        // Moves the third media item from position 2 to the start of the playlist.
        exoPlayer!!.moveMediaItem(/* currentIndex= */ 2, /* newIndex= */ 0);
        // Removes the first item from the playlist.
        exoPlayer!!.removeMediaItem(/* index= */ 0);
    }

    //TODO Replacing and clearing the entire playlist are also supported:
    private fun replacingAndClearing() {
        // Replaces the playlist with a new one.
        val newItems: List<MediaItem> = ImmutableList.of(
            MediaItem.fromUri(url),
            MediaItem.fromUri(url)
        )
        exoPlayer!!.setMediaItems(newItems,  /* resetPosition= */true)
        // Clears the playlist. If prepared, the player transitions to the ended state.
        exoPlayer!!.clearMediaItems()
    }

    // todo fire message at specific position
    private fun fireMessageAtSpecifiedPosition() {
        //TODO Firing events at specified playback positions
        exoPlayer!!.createMessage { messageType, payload ->

            Toast.makeText(this, payload.toString(), Toast.LENGTH_LONG).show()
        }
            .setLooper(Looper.getMainLooper())
            .setPosition( /* windowIndex= */0,  /* positionMs= */2000)
            .setPayload("customPayloadDatass")
            .setDeleteAfterDelivery(false)
            .send()
    }

    // todo player analytic listener
    private fun exoPlayerAnalyticListener() {
        //TODO Using AnalyticsListener
        exoPlayer?.addAnalyticsListener(object : EventLogger(null) {
            override fun onLoadStarted(eventTime: EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
                super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)

            }
        })
    }

    override fun onStart() {
        super.onStart()
        //
        if (Util.SDK_INT > 23) {
            initializePlayer()
            binding.exoplayer.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            initializePlayer()
            binding.exoplayer.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            binding.exoplayer.onPause()
            releasePlayer()
        }

    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            binding.exoplayer.onPause()
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adsLoader!!.release()
    }

    private fun releasePlayer() {
        adsLoader!!.setPlayer(null)
        binding.exoplayer.player = null
        exoPlayer?.release()
        exoPlayer = null
    }

}