import data.User
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.default
import io.ktor.content.files
import io.ktor.content.static
import io.ktor.features.CallLogging
import io.ktor.features.ConditionalHeaders
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.*
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.locations
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.mapNotNull
import org.jetbrains.exposed.sql.transactions.transaction
import repository.UserRepository

@Location("/")
class HomeUrl()

@Location("/login")
class LoginUrl()

@Location("/logout")
class LogoutUrl()

@Location("/register")
class RegisterUrl()

@Location("/{username}/activate")
class ActivateUrl(val username: String)

@Location("/{username}/like")
class LikeUrl(val username: String)

@Location("/{username}/unlike")
class UnLikeUrl(val username: String)

@Location("/{username}")
data class UserUrl(val username: String)

@Location("/{username1}/chats/{username2}")
data class ChatUrl(val username1: String, val username2: String)

@Location("/{username}/info")
data class InfoUrl(val username: String)

@Location("/{username}/photos")
data class PhotoUrl(val username: String)

@Location("/{username}/bloque")
data class BloqueUrl(val username: String)

@Location("/{username}/unbloque")
data class UnbloqueUrl(val username: String)

@Location("/{username}/report")
data class ReportUrl(val username: String)

data class Session(val username: String)


fun Application.main() {
    db.connect()
    db.init()
    val chat = Chat

    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(WebSockets)
    install(ConditionalHeaders)
    install(Sessions) {
        cookie<Session>(SESSION)
    }
    install(Routing) {
        static("public") {
            files("src/main/resources")
            default("index.html")
        }
        homeRoute()
        loginRoute()
        logoutRoute()
        registerRoute()
        activateRoute()
        userRoute()
        chatRoute()
        likeRoute()
        unlikeRoute()
        infoRoute()
        photoRoute()
        bloqueRoute()
        unbloqueRoute()
        reportRoute()

        webSocket("/ws/{username1}/{username2}") {
            var user : User? = null
            var user1 : User? = null
            var user2 : User? = null
            val username1 = call.parameters["username1"]
            val username2 = call.parameters["username2"]

            val username : String? = call.sessions.get<Session>()?.username
            if (username == null || username1  == null ||username2 == null) call.respondRedirect(application.locations.href(LoginUrl()))
            else {
                transaction {
                    user = UserRepository.getByUsername(username)
                    user1 = UserRepository.getByUsername(username1)
                    user2 = UserRepository.getByUsername(username2)
                }
                if (user == null || user1 == null || user2 == null || user != user1) call.respondRedirect(application.locations.href(HomeUrl()))
                else {
                    chat.memberJoin(user1!!, this)

                    try {
                        incoming.mapNotNull { it as? io.ktor.http.cio.websocket.Frame.Text }.consumeEach { frame ->
                            val msg = frame.readText()
                            val photo = if (user!!.photo == "default") "public/photos/35x35.png" else "/public/photos/${user!!.photo}"
                            if (msg != MSG_INIT) chat.sendMessage(username1 = user1!!.username, username2 = user2!!.username, type = MSG_CHAT, message = msg, photo = photo)
                        }
                    } finally {
                        chat.memberLeft(user!!)
                        call.sessions.clear<Session>()
                    }
                }
            }
        }
    }
}




