package flashcards

import java.io.File
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.nio.charset.Charset

data class FlashCard(var term: String, var definition: String, var mistakes: Int) {

}



class Deck() {
    private val cards = mutableListOf<FlashCard>()
    private var log = ""
    private fun printlog(string: String) {
        println(string)
        log += "$string\n"
    }
    private fun readlog(): String {
        val input = readln()
        log += "$input\n"
        return input
    }
    private fun createCard() {
        val terms = cards.map { it.term }
        val values = cards.map { it.definition }
        printlog("The card:")
        var term = ""
        while (true) {
            term = readlog()
            if (term in terms) {
                printlog("The card \"$term\" already exists.")
                return
            }
            break
        }
        printlog("The definition of the card:")
        var definition = ""
        while (true) {
            definition = readlog()
            if (definition in values) {
                printlog("The definition \"$definition\" already exists.")
                return
            }
            break
        }
        printlog("The pair (\"$term\":\"$definition\") has been added.")
        cards.add(FlashCard(term, definition, 0))
    }

    private fun checkAnswer(term: String) {
        val card = cards.find { it.term == term }!!
        val cardIndex = cards.indexOfFirst { it.term == term }
        printlog("Print the definition of \"${term}\":")
        val answer = readlog()
        if (card.definition == answer) {
            printlog("Correct!")
        } else if (cards.any { answer == it.definition }) {
            val mistakenCard = cards.find { answer == it.definition }!!
            cards[cardIndex].mistakes++
            printlog("Wrong. The right answer is \"${card.definition}\", but your definition is correct for \"${mistakenCard.term}\".")
        } else{
            cards[cardIndex].mistakes++
            printlog("Wrong. The right answer is \"${card.definition}\".")
        }
    }

    private fun importCards(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            printlog("File not found.")
            return
        }
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(MutableList::class.java, FlashCard::class.java)
        val mutableMapAdapter = moshi.adapter<MutableList<FlashCard>>(type)
        val importedCards: MutableList<FlashCard> = mutableMapAdapter.fromJson(file.readText(charset = Charset.defaultCharset()))!!
        for (card in importedCards) {
            if (card.term in cards.map { it.term }) {
                val index = cards.map { it.term }.indexOf(card.term)
                cards[index].definition = card.definition
                cards[index].mistakes = card.mistakes
            } else {
                cards.add(card)
            }
        }
        printlog("${importedCards.size} cards have been loaded.")
    }

    private fun exportCards(filePath: String) {
        val file = File(filePath)
        file.createNewFile()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(MutableList::class.java, FlashCard::class.java)
        val mutableMapAdapter = moshi.adapter<MutableList<FlashCard>>(type)
        val content = mutableMapAdapter.toJson(cards)
        file.bufferedWriter().use {
            it.write(content)
        }
        printlog("${cards.size} cards have been saved.")
    }

    private fun removeCard() {
        printlog("Which card?")
        val input = readlog()
        val terms = cards.map { it.term }
        if (terms.contains(input)) {
            cards.removeIf { it.term == input }
            printlog("The card has been removed.")
        } else {
            printlog("Can't remove \"$input\": there is no such card.")
        }
    }

    private fun askForDefinition() {
        printlog("How many times to ask?")
        val n = readlog().toInt()
        for (i in 1..n) {
            val randomTerm = cards.map { it.term }.random()
            checkAnswer(randomTerm)
        }
    }

    private fun saveLog() {
        printlog("File name:")
        val path = readlog()
        val file = File(path)
        file.createNewFile()
        file.writeText(log)
        printlog("The log has been saved.")
    }

    private fun printHardestCard() {
        val highestErrors = cards.maxOfOrNull { it.mistakes }
        if (highestErrors == null || highestErrors == 0) {
            printlog("There are no cards with errors.")
        } else {
            val termsMaxError = cards.filter { it.mistakes == highestErrors }.map { it.term }
            val isPluralOne = if (termsMaxError.size == 1) " is" else "s are"
            val isPluralTwo = if (termsMaxError.size == 1) "it" else "them"
            printlog("The hardest card$isPluralOne ${termsMaxError.joinToString(", "){"\"" + it + "\""} }." +
                    " You have $highestErrors errors answering $isPluralTwo.")
        }
    }

    private fun resetStats() {
        cards.forEach { it.mistakes = 0 }
        printlog("Card statistics have been reset.")
    }

    fun mainOperation(args: Array<String>) {
        if (args.contains("-import")) {
            val index = args.indexOf("-import") + 1
            importCards(args[index])
        }
        while (true) {
            printlog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            val input = readlog()
            when(input) {
                "add" -> createCard()
                "remove" -> removeCard()
                "import" -> {
                    printlog("File name:")
                    val filePath = readlog()
                    importCards(filePath)
                }
                "export" -> {
                    printlog("File name:")
                    val filePath = readlog()
                    exportCards(filePath)
                }
                "ask" -> askForDefinition()
                "log" -> saveLog()
                "hardest card" -> printHardestCard()
                "reset stats" -> resetStats()
                "exit" -> {
                    printlog("Bye bye!")
                    break
                }
            }
        }
        if (args.contains("-export")) {
            val index = args.indexOf("-export") + 1
            exportCards(args[index])
        }
    }

}


fun main(args: Array<String>) {
    val deck = Deck()
    deck.mainOperation(args)
}
