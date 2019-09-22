package ca.doophie.nhlroster.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.doophie.nhlroster.R
import ca.doophie.nhlroster.models.Team
import ca.doophie.nhlroster.services.NHLService
import kotlinx.android.synthetic.main.list_item_team.view.*

class TeamView(view: View) : RecyclerView.ViewHolder(view) {
    var team: Team? = null
        set(value) {
            field = value ?: return

            // remove image of last cell
            itemView.team_logo.setImageDrawable(null)

            itemView.name_text.text = value.name
            NHLService.getTeamLogo(value) {
                Handler(Looper.getMainLooper()).post {
                    itemView.team_logo.setImageDrawable(it)
                }
            }
        }
}

class TeamAdapter(var teams: List<Team>, private val onTeamSelected:(Team)->Unit) : RecyclerView.Adapter<TeamView>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamView {
        return TeamView(LayoutInflater.from(parent.context).inflate(R.layout.list_item_team, parent, false))
    }

    override fun getItemCount(): Int {
        return teams.count()
    }

    override fun onBindViewHolder(holder: TeamView, position: Int) {
        holder.team = teams[position]

        holder.itemView.setOnClickListener {
            onTeamSelected(teams[position])
        }
    }

}