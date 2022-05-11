package com.intellij.cce.workspace

import com.google.gson.*
import com.intellij.cce.core.Lookup
import com.intellij.cce.core.Session
import com.intellij.cce.core.Suggestion
import com.intellij.cce.core.TokenProperties
import com.intellij.cce.workspace.info.FileSessionsInfo
import org.apache.commons.lang.StringEscapeUtils
import java.lang.reflect.Type

class SessionSerializer {
  companion object {
    private val gson = GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(TokenProperties::class.java, TokenProperties.JsonAdapter)
      .create()
    // CHANGE IT
    private val gsonForJs = GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(TokenProperties::class.java, TokenProperties.JsonAdapter)
      .registerTypeAdapter(Suggestion::class.java, object : JsonSerializer<Suggestion> {
        override fun serialize(src: Suggestion, typeOfSrc: Type, context: JsonSerializationContext): JsonObject {
          val jsonObject = JsonObject()
          jsonObject.addProperty("text", escapeHtml(src.text))
          jsonObject.addProperty("presentationText", escapeHtml(src.presentationText))
          return jsonObject
        }
      })
      //.registerTypeAdapter(Lookup::class.java, object : JsonSerializer<Lookup> {
      //  override fun serialize(src: Lookup, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
      //    val jsonObject = JsonObject()
      //    jsonObject.addProperty("prefix", src.prefix)
      //    jsonObject.addProperty("latency", src.latency)
      //    jsonObject.addProperty("shownLatency", src.shownLatency)
      //    jsonObject.addProperty("restartLatency", src.restartLatency)
      //    jsonObject.add("features", context.serialize(src.features))
      //    jsonObject.addProperty("selectedPosition", src.selectedPosition)
      //    jsonObject.addProperty("isNew", src.isNew)
      //    jsonObject.add("correctElementInfo", context.serialize(src.correctElementInfo))
      //    jsonObject.addProperty("firstElementAddTime", src.firstElementAddTime)
      //    jsonObject.add("kindsExecutionInfo", context.serialize(src.kindsExecutionInfo))
      //    jsonObject.add("suggestions", context.serialize(src.suggestions.filter {
      //      it.isHighlighted
      //    }))
      //    return jsonObject
      //  }
      //})
      .create()

    private fun escapeHtml(value: String) =
      StringEscapeUtils.escapeHtml(value)
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("([\r\n\t])".toRegex(), "")
        .replace("""(\\r|\\n|\\t)""".toRegex(), "")
        .replace("\\", "&#92;")
        .replace("★", "*")
  }

  fun serialize(sessions: FileSessionsInfo): String = gson.toJson(sessions)

  fun serialize(sessions: List<Session>): String {
    val map = HashMap<String, Session>()
    for (session in sessions) {
      map[session.id] = session
    }
    return gsonForJs.toJson(map)
  }

  fun deserialize(json: String): FileSessionsInfo {
    return gson.fromJson(json, FileSessionsInfo::class.java)
  }
}