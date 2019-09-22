package ca.doophie.nhlroster

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
import kotlinx.android.synthetic.main.fragment_player.*
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
    }

    private fun loadTeams() {
        NHLService.getTeams { teams ->
            Handler(Looper.getMainLooper()).post {
                teams_recycler.layoutManager = LinearLayoutManager(applicationContext)
                teams_recycler.adapter = TeamAdapter(teams, onTeamSelected)
            }
        }
    }

    private val onTeamSelected: (Team)->Unit = {
        // close drawer
        onBackPressed()

        title = it.name

        // load roster & attach fragment
        NHLService.getTeamRoster(it) { players ->
            val rosterFragment = RosterFragment(players, onPlayerSelected)
            content_view.attach(rosterFragment)
            attachedFragment = rosterFragment
        }
    }

    private val onPlayerSelected: (PlayerView)->Unit = {
        content_view.attach(PlayerFragment(it.player!!), listOf(
            TargetedTransition(attachedFragment!!, it.itemView.player_portrait, R.id.fragment_player_portrait),
            TargetedTransition(attachedFragment!!, it.itemView.player_details, R.id.fragment_player_details)
            ), "PlayerSelected"
        )
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_drawer, menu)
        return true
    }

}
