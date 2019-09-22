package ca.doophie.nhlroster.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.doophie.doophrame2.fragmentFramework.Doophragment
import ca.doophie.nhlroster.R
import ca.doophie.nhlroster.models.Player
import ca.doophie.nhlroster.services.NHLService
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.view.*
import java.lang.Exception

class PlayerFragment(private val player: Player) : Doophragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val playerView = inflater.inflate(R.layout.fragment_player, container, false)

        if (player.portrait == null) {
            NHLService.getPlayerPortrait(player) {
                player.portrait = it

                Handler(Looper.getMainLooper()).post {
                    try {
                        playerView.fragment_player_portrait.setImageBitmap(it)
                    } catch (e: Exception) {
                        // This could occur if the user backs out before the image loads
                        Log.e("PlayerFragment", "Exception: $e")
                    }
                }
            }
        } else {
            playerView.fragment_player_portrait.setImageBitmap(player.portrait)
        }

        return playerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name_text.text = player.name
        position_text.text = "Position: ${player.pos}"
        number_text.text = if (player.number >= 0) "Number: ${player.number}" else "Number: Unknown"
        points_text.text = "Points: ${player.points}"
        goals_text.text = "Goals: ${player.goals}"
        assists_text.text = "Assits: ${player.assists}"

        val nationality = player.nationality ?: return
        NHLService.getCountryFlag(nationality) {
            Handler(Looper.getMainLooper()).post {
                fragment_flag.setImageBitmap(it)
            }
        }
    }
}