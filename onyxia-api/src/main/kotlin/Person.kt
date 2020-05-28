package fr.insee.onyxia
import fr.insee.onyxia.model.User

data class Person(val name: String) {
    var age: Int = 0
    var user: User? = null;

    override fun toString(): String {
        return super.toString()
    }
}