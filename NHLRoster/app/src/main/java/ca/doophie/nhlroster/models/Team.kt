package ca.doophie.nhlroster.models

import org.json.JSONObject

data class Team(
    val name: String,
    val id: Int
) {

    companion object {
        fun fromJSON(json: JSONObject) : Team {
            return Team(
                json.getString("name"),
                json.getInt("id")
            )
        }
    }

}