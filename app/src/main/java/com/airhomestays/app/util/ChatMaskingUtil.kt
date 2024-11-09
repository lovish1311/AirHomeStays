package com.airhomestays.app.util

class ChatMaskingUtil {

    private val numberWords: Map<String, Int> = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
        "fifteen" to 15, "sixteen" to 16, "seventeen" to 17, "eighteen" to 18, "nineteen" to 19,
        "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50, "sixty" to 60,
        "seventy" to 70, "eighty" to 80, "ninety" to 90, "hundred" to 100, "thousand" to 1000
    )

    // Convert word-based numbers to digits
    private fun convertWordsToDigits(text: String): String {
        var digitString = ""
        var wordBuffer = ""

        for (i in text.indices) {
            wordBuffer += text[i].toLowerCase().toString().trim()

            if (numberWords.containsKey(wordBuffer)) {
                digitString += numberWords[wordBuffer].toString()
                wordBuffer = "" // Reset the buffer
            }
        }

        return digitString
    }

    // Mask numbers (numeric or word-based)
    fun applyChatMasking(text: String): String {
        var maskedText = text

        // Replace numerical phone numbers of at least 5 digits
        maskedText = maskedText.replace(Regex("(\\+?\\d{1,4}[-\\s]?)?(\\(?\\d{2,4}\\)?[-\\s]?){2,}"), "*****")
        maskedText = maskedText.replace(Regex("\\d{5,}"), "*****")

        // Replace word-based phone numbers
        val wordToDigitText = convertWordsToDigits(maskedText)
        if (Regex("\\d{5,}").containsMatchIn(wordToDigitText)) {
            maskedText = "*****"
        }

        return maskedText
    }

    fun handleTextChange(input: String): String {
        return applyChatMasking(input)
    }
}
