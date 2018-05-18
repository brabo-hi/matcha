package data

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.Column
import org.joda.time.DateTime

object Chats : IntIdTable() {
    val username1 : Column<String> = Likes.reference("chart_username1", Users.username)
    val username2 : Column<String> = Likes.reference("chart_username2", Users.username)
    val message : Column<String> = text("message")
    val date : Column<DateTime> = datetime("date")
}

class Chat(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Chat>(Chats)

    var username1 by Chats.username1
    var username2 by Chats.username2
    var message by Chats.message
    var date by Chats.date
}

data class ChatData(var username1: String,
                    var username2: String,
                    var message : String,
                    val date: DateTime)