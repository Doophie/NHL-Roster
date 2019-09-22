package ca.doophie.nhlroster.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ca.doophie.doophrame2.fragmentFramework.Doophragment
import ca.doophie.nhlroster.R
import ca.doophie.nhlroster.adapters.PlayerView
import ca.doophie.nhlroster.adapters.RosterAdapter
import ca.doophie.nhlroster.models.Player
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_roster.*

class RosterFragment(val roster: List<Player>, private val playerSelected: (PlayerView)->Unit) : Doophragment() {

    private var curSortingIndex = 0
    private var sortingOptions = listOf("Name", "Number", "Points -", "Points +")

    private var curFilterIndex = 0
    private var filters = listOf("No Filter", "Center", "Goalie", "Defenseman", "Left Wing", "Right Wing")

    private lateinit var rosterAdapter: RosterAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_roster, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roster_recycler.layoutManager = LinearLayoutManager(context)
        rosterAdapter = RosterAdapter(getSortedPlayers(getFilteredPlayer(roster)), playerSelected)
        roster_recycler.adapter = rosterAdapter

        setUpSorting()
        setUpFilter()
    }

    private fun setUpFilter() {
        filtered_by_text.text = "Filtered by: ${filters[curFilterIndex]}"
        filtered_by_text.setOnClickListener {
            curFilterIndex = (curFilterIndex + 1) % filters.count()
            rosterAdapter.players = getSortedPlayers(getFilteredPlayer(roster))
            rosterAdapter.notifyDataSetChanged()
            filtered_by_text.text = "Filtered by: ${filters[curFilterIndex]}"
        }
    }

    private fun setUpSorting() {
        sorted_by_text.text = "Sorted by: ${sortingOptions[curSortingIndex]}"
        sorted_by_text.setOnClickListener {
            curSortingIndex = (curSortingIndex + 1) % sortingOptions.count()
            rosterAdapter.players = getSortedPlayers(getFilteredPlayer(roster))
            rosterAdapter.notifyDataSetChanged()
            sorted_by_text.text = "Sorted by: ${sortingOptions[curSortingIndex]}"
        }
    }

    private fun getFilteredPlayer(players: List<Player>) : List<Player> {
        return if (curFilterIndex != 0)
            players.filter { it.pos == filters[curFilterIndex] }
        else
            players // no filter is 0 index
    }

    private fun getSortedPlayers(players: List<Player>) : List<Player> {
        // apply sorting
        return when (sortingOptions[curSortingIndex]) {
            "Name" -> players.sortedBy { it.name }
            "Points -" -> players.sortedBy { it.points ?: Int.MAX_VALUE } // Max so unknown is at bottom
            "Points +" -> players.sortedBy { -(it.points ?: -9999) }
            "Number" -> players.sortedBy { it.number }
            else -> players.sortedBy { it.name }
        }
    }

}