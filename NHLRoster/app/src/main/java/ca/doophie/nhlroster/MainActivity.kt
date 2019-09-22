package ca.doophie.nhlroster

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import androidx.recyclerview.widget.LinearLayoutManager
import ca.doophie.doophrame2.extensions.attach
import ca.doophie.doophrame2.fragmentFramework.Doophragment
import ca.doophie.doophrame2.transitions.TargetedTransition
import ca.doophie.nhlroster.adapters.PlayerView
import ca.doophie.nhlroster.adapters.TeamAdapter
import ca.doophie.nhlroster.fragments.PlayerFragment
import ca.doophie.nhlroster.fragments.RosterFragment
import ca.doophie.nhlroster.models.Player
import ca.doophie.nhlroster.models.Team
import ca.doophie.nhlroster.services.NHLService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_player.view.*

class MainActivity : AppCompatActivity() {

    private var attachedFragment: Doophragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        toolbar.title = "NHL Roster"
        toolbar.setTitleTextColor(applicationContext.getColor(R.color.colorText))

        val toggle = ActionBarDrawerToggle(
    this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        loadTeams()

        prev_season_button.setOnClickListener {
            onSeasonSwitch(false)
        }

        next_season_button.setOnClickListener {
            onSeasonSwitch(true)
        }

        updateSeasonText()
    }

    private fun loadTeams() {
        NHLService.getTeams { teams ->
            Handler(Looper.getMainLooper()).post {
                val adapter = teams_recycler.adapter as? TeamAdapter?

                if (adapter != null) {
                    adapter.teams = teams
                    adapter.notifyDataSetChanged()
                } else {
                    teams_recycler.layoutManager = LinearLayoutManager(applicationContext)
                    teams_recycler.adapter = TeamAdapter(teams, onTeamSelected)
                }
            }
        }
    }

    private fun updateSeasonText() {
        val season = Utils.selectedSeason
        current_season_text.text = "Stats for Season: ${season.substring(0, 4) + " - " + season.substring(4, season.count())}"
    }

    private val onTeamSelected: (Team)->Unit = {
        // close drawer
        onBackPressed()

        title = it.name

        // load roster & attach fragment
        NHLService.getTeamRoster(it) { players ->
            Handler(Looper.getMainLooper()).post {
                val rosterFragment = RosterFragment(players, onPlayerViewSelected)
                content_view.attach(rosterFragment)
                attachedFragment = rosterFragment
            }
        }
    }

    // attach player fragment with transition of names
    private val onPlayerViewSelected: (PlayerView)->Unit = {
        Handler(Looper.getMainLooper()).post {
            content_view.attach(
                PlayerFragment(it.player!!), listOf(
                    TargetedTransition(attachedFragment!!, it.itemView.player_portrait, R.id.fragment_player_portrait),
                    TargetedTransition(attachedFragment!!, it.itemView.player_details, R.id.fragment_player_details)
                ), "PlayerSelected"
            )
        }
    }

    private fun clearCurrentData() {
        content_view.removeAllViews()
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        // close drawer if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // if we are on roster, back just clears it
            if (attachedFragment as? RosterFragment? != null) {
                content_view.removeAllViews()
            } else if (attachedFragment as? PlayerFragment? != null) {
                // otherwise use fragment backstack
                super.onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_drawer, menu)
        return true
    }

    private fun onSeasonSwitch(isNext: Boolean) {
        if (isNext)
            Utils.nextSeason()
        else
            Utils.previousSeason()

        updateSeasonText()

        clearCurrentData()

        loadTeams()
    }

}
