package uk.co.puce4.passgen

import java.security.SecureRandom

class PasswordGenerator(private val dictionary: List<String>) {
    private val random = SecureRandom()

    fun hasWords(): Boolean = dictionary.isNotEmpty()

    fun generate(wordCount: Int, includeNumber: Boolean, separator: String): String {
        if (dictionary.isEmpty()) return "Error: No words"

        val words = (1..wordCount).map {
            dictionary[random.nextInt(dictionary.size)]
        }

        val base = words.joinToString(separator)
        return if (includeNumber) "$base$separator${random.nextInt(100)}" else base
    }
}