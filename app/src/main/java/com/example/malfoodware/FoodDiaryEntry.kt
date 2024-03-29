package com.example.malfoodware

import java.util.*
import kotlin.math.roundToInt

class FoodDiaryEntry(var timeMillis: Long = Calendar.getInstance().timeInMillis): Comparable<FoodDiaryEntry>
{
    var bloodSugar: Float? = null
    var insulinTaken: Int? = null
    var notes: String? = null
    var recipes: SortedMap<Recipe, Float> = sortedMapOf()
    var ingredients: SortedMap<Ingredient, Float> = sortedMapOf()
    var dateString: String
    init {
        val date = Calendar.getInstance()
        date.timeInMillis = timeMillis
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val year = date.get(Calendar.YEAR)
        dateString = "$day/$month/$year"
    }

    fun toCSV(): String
    {
        var output = ""

        // adds time/date
        val date = Calendar.getInstance()
        date.timeInMillis = timeMillis
        var timeString = "${date.get(Calendar.HOUR_OF_DAY)}:"
        var minute = date.get(Calendar.MINUTE)
        if (minute < 10)
            timeString += "0$minute"
        else
            timeString += minute
        output += "\"$dateString\n$timeString\","

        // adds blood sugar
        val bs = if (bloodSugar == null) "N/A" else bloodSugar!!.round(2).toString()
        output += "$bs,"

        // adds recipe/ingredients
        output += "\""
        var carbs = 0f
        if (recipes.size > 0 || ingredients.size > 0)
        {
            for (recipe in recipes)
            {
                output += recipe.key.recName
                output += " "
                output += "${recipe.value.roundToInt()}g "
                val carb = (recipe.key.whatNutirion() / recipe.value).carbs
                output += "(${carb.round(2)}g)\n"
                carbs += carb
            }
            for (ing in ingredients)
            {
                output += ing.key.name
                output += " "
                val carb = (ing.key.whatNutirion() / ing.value).carbs
                output += "${ing.value.roundToInt()}g "
                output += "(${carb.round(2)}g)\n"
                carbs += carb
            }
            output = output.substring(0, output.length-1)
        }
        else
            output += "N/A"
        output += "\","

        // adds carbs
        output += carbs.round(2)
        output += ","

        // adds insulin taken
        if (insulinTaken == null)
            output += "N/A,"
        else
            output += "$insulinTaken,"

        //adds notes
        if (notes == null)
            output += "N/A,,"
        else
            output += "\"$notes\",,"
        return output
    }

    fun addRecipe(rec: Recipe, qty: Float): Boolean
    {
        if (recipes.containsKey(rec)) return false
            recipes[rec] = qty
        return true
    }

    fun getNutrition(): Nutrition
    {
        var nutrition: Nutrition? = null
        for (ingredient in ingredients)
        {
            if (nutrition == null)
            {
                nutrition = ingredient.key.nut / ingredient.value
            }
            else
                nutrition.plusAssign(ingredient.key.nut / ingredient.value)
        }
        for (recipe in recipes)
        {
            if (nutrition == null)
            {
                nutrition = recipe.key.getNutrition()!! / recipe.value
            }
            else
                nutrition.plusAssign(recipe.key.getNutrition()!! / recipe.value)
        }
        if (nutrition == null)
            nutrition = Nutrition()
        return nutrition
    }

    fun addIngredient(ing: Ingredient, qty: Float): Boolean
    {
        if (ingredients.containsKey(ing)) return false
            ingredients[ing] = qty
        return true
    }
//    var ingRec: MutableMap<String, Float> // contains ingredient/recipe id as key, weight as value

    override fun equals(other: Any?): Boolean {
        return other != null && other is FoodDiaryEntry && other.timeMillis == this.timeMillis
    }

    override fun compareTo(other: FoodDiaryEntry): Int {
        if (this.timeMillis > other.timeMillis) return 1 else return -1
    }

    override fun toString(): String {
        return "DiaryEntry[timeMillis: $timeMillis, date: $dateString, bloodsugar: $bloodSugar, " +
                "insulinTaken: $insulinTaken, notes: $notes, ingredientsNo: ${ingredients.size}, " +
                "recipeNo: ${recipes.size}]"
    }

    fun toJSON(tabs: Int): String
    {
        val newTabs = tabs + 1
        var result = "${addTabs(tabs)}{\n${addTabs(newTabs)}\"bloodSugar\": $bloodSugar,\n${addTabs(newTabs)}" +
                "\"insulinTaken\": $insulinTaken,\n${addTabs(newTabs)}\"notes\": "
        result += if (notes == null) "" else "\""
        result += "$notes"
        result += if (notes == null) "" else "\""
        result += "\n${addTabs(newTabs)}\"timeMillis\": $timeMillis,\n${addTabs(newTabs)}\"ingredients\": " +
                "[\n"
        val listTabs = tabs + 2
        for (ing in ingredients)
        {
            result += "${addTabs(newTabs+1)}{\n${addTabs(listTabs)}\"ingName\": \"${ing.key.name}\"," +
                    "\n${addTabs(listTabs)}\"qty\": ${ing.value}\n${addTabs(newTabs+1)}},\n"
        }
        if (!ingredients.isEmpty())
        {
            result = result.substring(0,  result.length-2) + "\n"
        }
        result += "${addTabs(newTabs)}],\n${addTabs(newTabs)}\"recipes\": [\n"
        for (recipe in recipes)
        {
            result += "${addTabs(newTabs+1)}{\n${addTabs(listTabs)}\"recName\": \"${recipe.key.recName}," +
                    "\n${addTabs(listTabs)}\"qty\": ${recipe.value}\n${addTabs(newTabs+1)}},\n"
        }
        if (!recipes.isEmpty())
        {
            result = result.substring(0,  result.length-2) + "\n"
        }
        result += "${addTabs(newTabs)}]\n${addTabs(tabs)}}"
        return result
    }
}