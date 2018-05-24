package midien.kheldiente.equalizer.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


object JsonUtil {

    @Throws(JSONException::class)
    fun toMap(jsonObj: JSONObject): Map<String, Any> {
        val map = HashMap<String, Any>()
        val keys = jsonObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            var value = jsonObj.get(key)
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            map.put(key, value)
        }
        return map
    }

    @Throws(JSONException::class)
    fun toList(array: JSONArray): List<Any> {
        val list = ArrayList<Any>()
        for (i in 0 until array.length()) {
            var value = array.get(i)
            if (value is JSONArray) {
                value = toList(value)
            } else if (value is JSONObject) {
                value = toMap(value)
            }
            list.add(value)
        }
        return list
    }

}
