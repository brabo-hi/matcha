import data.User
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.cio.websocket.*
import io.ktor.locations.location
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import kotlinx.coroutines.experimental.channels.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import repository.ChatRepository
import repository.UserRepository
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.*
import kotlin.collections.HashMap

object Chat {
    private val members : MutableMap<String, WebSocketSession> = mutableMapOf()

    suspend fun memberJoin(user : User, socket: WebSocketSession) {
        if (!members.containsKey(user.username)) {
            transaction {
                UserRepository.getByUsername(user.username)?.let {
                    it.isOnline = true
                    it.date = DateTime.now()
                }
            }
            members[user.username] = socket
           // broadcast(user)
        }
    }

    fun memberLeft(user: User) {
        members.remove(user.username)
        transaction {
            UserRepository.getByUsername(user.username)?.let {
                it.isOnline = false
                it.date = DateTime.now()
            }
        }
    }

    suspend fun sendMessage(username1 : String, username2: String, type : String, message : String, photo: String) {
        members[username2]?.send(Frame.Text("$type$MSG_SEPARATOR$username1$MSG_SEPARATOR$username2$MSG_SEPARATOR$message$MSG_SEPARATOR${DateTime().toString(DateTimeFormat.shortDateTime())}$MSG_SEPARATOR$photo"))
        if (type == MSG_CHAT) transaction {  ChatRepository.add(username1 = username1, username2 = username2, message = message) }
    }

    suspend fun broadcast(user : User) {
        val src = if (user.photo == "default") "/public/images/profile_default.jpg" else "/public/photos/${user.photo}"
        for ((key, value) in members) if (key != user.username) { value.send(frame = Frame.Text("$MSG_ONLINE$MSG_SEPARATOR${user.username}$MSG_SEPARATOR$key$MSG_SEPARATOR$src")) }
    }

}