package dev.sajidali.onplayer.core

interface VideoController {

    fun showLoader()
    fun hideLoader()
    fun show(showButtons: Boolean = false)
    fun isControlsVisible(): Boolean
    fun hide()
    fun onStateChanged(current: MediaPlayer.State, target: MediaPlayer.State)
    fun showInfo(info: String)
    fun setSubTitles(info: String)

}