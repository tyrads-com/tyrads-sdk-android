package com.tyrads.sdk.acmo.core.services

import com.tyrads.sdk.acmo.core.services.LocalizationService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

/**
 * Unit tests for [LocalizationService.translate] logic.
 * Uses reflection to inject a mock translations map so no network call is required.
 */
class LocalizationServiceTranslateTest {

    private lateinit var service: LocalizationService

    @Before
    fun setUp() {
        service = LocalizationService.getInstance()
        // Reset and inject translations via reflection
        setTranslations(emptyMap())
    }

    private fun setTranslations(map: Map<String, Any>) {
        val field: Field = LocalizationService::class.java.getDeclaredField("translations")
        field.isAccessible = true
        field.set(service, map)
    }

    // ---- Empty translations ----

    @Test
    fun translate_emptyTranslations_returnsKey() {
        setTranslations(emptyMap())
        val result = service.translate("some.key")
        assertEquals("some.key", result)
    }

    // ---- Existing keys ----

    @Test
    fun translate_existingSimpleKey_returnsValue() {
        setTranslations(mapOf("greeting" to "Hello"))
        val result = service.translate("greeting")
        assertEquals("Hello", result)
    }

    @Test
    fun translate_existingNestedKey_returnsValue() {
        setTranslations(
            mapOf(
                "ui" to mapOf(
                    "button" to mapOf(
                        "ok" to "OK"
                    )
                )
            )
        )
        val result = service.translate("ui.button.ok")
        assertEquals("OK", result)
    }

    @Test
    fun translate_deeplyNestedKey_returnsValue() {
        setTranslations(
            mapOf(
                "a" to mapOf(
                    "b" to mapOf(
                        "c" to mapOf(
                            "d" to "deep value"
                        )
                    )
                )
            )
        )
        assertEquals("deep value", service.translate("a.b.c.d"))
    }

    // ---- Missing keys ----

    @Test
    fun translate_missingSimpleKey_returnsKey() {
        setTranslations(mapOf("other.key" to "value"))
        val result = service.translate("nonexistent.key")
        assertEquals("nonexistent.key", result)
    }

    @Test
    fun translate_partialPath_returnsKey() {
        setTranslations(
            mapOf("ui" to mapOf("button" to "OK"))
        )
        // "ui.button.label" — "label" doesn't exist under "button" (it's a String, not a Map)
        val result = service.translate("ui.button.label")
        assertEquals("ui.button.label", result)
    }

    @Test
    fun translate_intermediateNodeIsNotMap_returnsKey() {
        setTranslations(mapOf("ui" to "not a map"))
        val result = service.translate("ui.button")
        assertEquals("ui.button", result)
    }

    // ---- Argument substitution ----

    @Test
    fun translate_withArgs_substitutesPlaceholder() {
        setTranslations(mapOf("welcome" to "Hello, {name}!"))
        val result = service.translate("welcome", args = mapOf("name" to "Alice"))
        assertEquals("Hello, Alice!", result)
    }

    @Test
    fun translate_withMultipleArgs_substitutesAll() {
        setTranslations(mapOf("msg" to "{greeting}, {name}! You have {count} messages."))
        val result = service.translate(
            "msg",
            args = mapOf("greeting" to "Hi", "name" to "Bob", "count" to 5)
        )
        assertEquals("Hi, Bob! You have 5 messages.", result)
    }

    @Test
    fun translate_argKeyIsCaseInsensitive() {
        setTranslations(mapOf("msg" to "Hello, {NAME}!"))
        val result = service.translate("msg", args = mapOf("name" to "Carol"))
        assertEquals("Hello, Carol!", result)
    }

    @Test
    fun translate_nullArgs_returnsTranslatedStringWithPlaceholder() {
        setTranslations(mapOf("msg" to "Hello, {name}!"))
        val result = service.translate("msg", args = null)
        assertEquals("Hello, {name}!", result)
    }

    @Test
    fun translate_extraArgs_ignoresUnusedArgs() {
        setTranslations(mapOf("msg" to "Hello!"))
        val result = service.translate("msg", args = mapOf("unused" to "value"))
        assertEquals("Hello!", result)
    }

    // ---- Non-string final value ----

    @Test
    fun translate_finalValueIsMap_returnsKey() {
        setTranslations(
            mapOf("section" to mapOf("sub" to "value"))
        )
        // Translating "section" where the value is a Map, not a String
        val result = service.translate("section")
        assertEquals("section", result)
    }
}
