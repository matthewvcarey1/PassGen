package uk.co.puce4.passgen

import java.security.SecureRandom

class PasswordGenerator(private val dictionary: List<String>) {
    private val random = SecureRandom()

    fun hasWords(): Boolean = dictionary.isNotEmpty()

    fun generate(wordCount: Int, includeNumber: Boolean): String {
        if (dictionary.isEmpty()) return "Error: No words"

        val words = (1..wordCount).map {
            dictionary[random.nextInt(dictionary.size)]
        }

        val base = words.joinToString("-")
        return if (includeNumber) "$base-${random.nextInt(10)}" else base
    }
}