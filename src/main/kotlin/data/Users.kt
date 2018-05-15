package data

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Date
import org.joda.time.DateTime

object Users : IntIdTable() {
    val username: Column<String> = varchar("username", 20).uniqueIndex()
    val email : Column<String> = varchar("email", 50)
    val firstName : Column<String> = varchar("first_name", 50)
    val lastName : Column<String> = varchar("last_name", 50)
    val birthday : Column<DateTime> = date("birthday")
    val password : Column<String> = varchar("password", 255)
    val photo : Column<String> = varchar("photo", 255)
    val gender = enumeration("gender", Gender::class.java)
    val biography : Column<String> =  text("biography")
    val isActivate : Column<Boolean> = bool("is_activate")
    val code : Column<Int> = integer("code")
 }

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var username by Users.username
    var email by Users.email
    var firstName by Users.firstName
    var lastName by Users.lastName
    var birthday by Users.birthday
    var password by Users.password
    var photo by Users.photo
    var gender by Users.gender
    var biography by Users.biography
    var isActivate by Users.isActivate
    var code by Users.code
}