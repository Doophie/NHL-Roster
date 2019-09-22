package ca.doophie.nhlroster.models

import android.graphics.Bitmap
import android.util.Log
import org.json.JSONObject
import java.lang.Exception

data class Player(
    val name: String,
    val pos: String,
    val number: Int,
    val id: Int
) {

    var nationality: String? = null
    var portrait: Bitmap? = null
    var points: Int? = null
    var goals: Int? = null
    var assists: Int? = null
    var hits: Int? = null
    var penaltyMinutes: Int? = null
    var faceOffWins: String? = null
    var gamesPlayed: Int? = null
    var timeOnInce: String? = null


    companion object {
        fun fromJSON(json: JSONObject) : Player {
            return Player(
                json.getJSONObject("person").getString("fullName"),
                json.getJSONObject("position").getString("name"),
                json.optString("jerseyNumber", "-1").toInt(),
                json.getJSONObject("person").getInt("id")
            )
        }
    }

    fun addDetailsFromJSON(json: JSONObject) {
        Log.d("PlayerDetails", json.toString())

        try {
            points = json.getInt("points")
            goals = json.getInt("goals")
            assists = json.getInt("assists")
            hits = json.getInt("hits")
            penaltyMinutes = json.getInt("penaltyMinutes")
            faceOffWins = "${json.getDouble("faceOffPct")}%"
            gamesPlayed = json.getInt("games")
            timeOnInce = json.getString("timeOnIcePerGame")

        } catch (e: Exception) {
            Log.e("Player", "Failed to set details for player $name : $e")
        }
    }
}