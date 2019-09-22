package ca.doophie.nhlroster.services

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import ca.doophie.nhlroster.Utils
import ca.doophie.nhlroster.models.Player
import ca.doophie.nhlroster.models.Team
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.caverock.androidsvg.SVG
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class NHLService {

    companion object {
        private val apiURL = "https://statsapi.web.nhl.com/api/v1/"

        fun getTeams(callback: (List<Team>)->Unit) {
            Thread {
                val url = URL("${apiURL}teams?season=${Utils.selectedSeason}")

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"  // optional default is GET

                    inputStream.bufferedReader().use {
                        val lines = it.readLines().fold("") { sum, cur -> sum + cur }

                        val teamsJson = JSONObject(lines).optJSONArray("teams") ?: return@Thread

                        val teams = ArrayList<Team>()
                        for (i in 0 until teamsJson.length()) {
                            teams.add(Team.fromJSON(teamsJson.getJSONObject(i)))
                        }

                        callback(teams)
                    }
                }
            }.start()
        }

        fun getTeamRoster(team: Team, callback: (List<Player>) -> Unit) {
            Thread {
                val url = URL("${apiURL}teams/${team.id}?expand=team.roster&season=${Utils.selectedSeason}")

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"  // optional default is GET

                    try {
                        inputStream.bufferedReader().use {
                            val lines = it.readLines().fold("") { sum, cur -> sum + cur }

                            val teams = JSONObject(lines).optJSONArray("teams") ?: return@Thread

                            val team = teams.optJSONObject(0)
                            val roster = team.optJSONObject("roster")
                            val playersJson = roster?.optJSONArray("roster") ?: return@Thread

                            val players = ArrayList<Player>()
                            for (i in 0 until playersJson.length()) {
                                val player = Player.fromJSON(playersJson.getJSONObject(i))

                                players.add(player)

                                // load in portrait for player
                                NHLService.getPlayerPortrait(player) {
                                    player.portrait = it
                                }

                                //load other details for each player
                                loadPlayerDetails(player)
                                loadPlayerStats(player, Utils.selectedSeason)
                            }

                            callback(players)
                        }
                    } catch (e: Exception) {
                        Log.e("NHLService", "Failed to get roster: $e")
                    }
                }
            }.start()
        }

        private fun loadPlayerDetails(player: Player) {
            Thread {
                val url = URL("${apiURL}people/${player.id}")

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"  // optional default is GET

                    inputStream.bufferedReader().use {
                        val lines = it.readLines().fold("") { sum, cur -> sum + cur }

                        val nationality = JSONObject(lines).optJSONArray("people")
                            ?.optJSONObject(0)
                            ?.optString("nationality") ?: return@Thread

                        player.nationality = nationality
                    }
                }
            }.start()
        }

        private fun loadPlayerStats(player: Player, season: String) {
            Thread {
                val url = URL("${apiURL}people/${player.id}/stats?stats=statsSingleSeason&season=$season")

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"  // optional default is GET

                    inputStream.bufferedReader().use {
                        val lines = it.readLines().fold("") { sum, cur -> sum + cur }

                        val playerDetails = JSONObject(lines).optJSONArray("stats")
                            ?.optJSONObject(0)
                            ?.optJSONArray("splits")
                            ?.optJSONObject(0)
                            ?.optJSONObject("stat") ?: return@Thread

                        player.addDetailsFromJSON(playerDetails)
                    }
                }
            }.start()
        }

        fun getTeamLogo(team: Team, callback: (BitmapDrawable)->Unit) {
            Thread {
                val url = URL("https://www-league.nhlstatic.com/images/logos/teams-current-primary-light/${team.id}.svg")
                val conf = Bitmap.Config.ARGB_8888
                val bitmap = Bitmap.createBitmap(128, 128, conf)

                val canvas = Canvas(bitmap)

                try {
                    val svg = SVG.getFromInputStream(url.openConnection().getInputStream())

                    svg.renderToCanvas(canvas)

                    callback(BitmapDrawable(Resources.getSystem(), bitmap))
                } catch (e: Exception) {
                    Log.e("NHLService", "Failed to get team ${team.name} logo: $e")
                }
            }.start()
        }

        fun getCountryFlag(countryCode: String, callback: (Bitmap)->Unit) {
            Thread {
                val url = URL("https://www.countryflags.io/${Utils.getTwoDigitCountryCode(countryCode)}/shiny/64.png")

                try {
                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream()) ?: return@Thread

                    callback(bmp)
                } catch (e: Exception) {
                    Log.e("NHLService", "Failed to load player image $e")
                }
            }.start()
        }

        fun getPlayerPortrait(player: Player, callback: (Bitmap)->Unit) {
            Thread {
                val url = URL("https://nhl.bamcontent.com/images/headshots/current/168x168/${player.id}.jpg")

                try {
                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream()) ?: return@Thread

                    callback(bmp)
                } catch (e: Exception) {
                    Log.e("NHLService", "Failed to load player image $e")
                }
            }.start()
        }
    }

}