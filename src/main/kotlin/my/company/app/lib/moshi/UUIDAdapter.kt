package my.company.app.lib.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import java.util.UUID

class UUIDAdapter {

    @ToJson
    fun toJson(uuid: UUID?): String? = uuid?.toString()

    @FromJson
    fun fromJson(uuid: String?): UUID? {
        uuid ?: return null
        return try {
            UUID.fromString(uuid)
        } catch (e: Throwable) {
            throw JsonDataException("Invalid UUID: $uuid")
        }
    }
}
