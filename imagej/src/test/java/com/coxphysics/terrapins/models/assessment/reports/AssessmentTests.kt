package com.coxphysics.terrapins.models.assessment.reports

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AssessmentTests
{

    private fun working_directory_path(): Path {
        return Path("a/smlm_assessment")
    }

    @Test
    fun can_parse_score()
    {
        val lines = listOf("name,something","score,123","result,passed","message,else")
        val assessment = Assessment.from_lines(lines)
        assertNotNull(assessment)
        assertEquals(assessment!!.name(), "something")
        assertEquals(assessment.score(), 123.0)
        assertEquals(assessment.passed(), true)
        assertEquals(assessment.message(), "else")
    }

    @Test
    fun score_is_optional()
    {
        val lines = listOf("name,something","score,-","result,passed","message,else")
        val assessment = Assessment.from_lines(lines)
        assertNotNull(assessment)
        assertEquals(assessment!!.name(), "something")
        assertEquals(assessment.score(), null)
        assertEquals(assessment.passed(), true)
        assertEquals(assessment.message(), "else")
    }

    @Test
    fun fail_if_result_not_parsed()
    {
        val lines = listOf("name,something","score,-","result,junk","message,else")
        val assessment = Assessment.from_lines(lines)
        assertNull(assessment)
    }

    @Test
    fun can_parse_failure()
    {
        val lines = listOf("name,something","score,-","result,failed","message,else")
        val assessment = Assessment.from_lines(lines)
        assertNotNull(assessment)
        assertEquals(assessment!!.name(), "something")
        assertEquals(assessment.score(), null)
        assertEquals(assessment.passed(), false)
        assertEquals(assessment.failed(), true)
        assertEquals(assessment.message(), "else")
    }

    @Test
    fun can_parse_indeterminate()
    {
        val lines = listOf("name,something","score,-","result,-","message,else")
        val assessment = Assessment.from_lines(lines)
        assertNotNull(assessment)
        assertEquals(assessment!!.name(), "something")
        assertEquals(assessment.score(), null)
        assertEquals(assessment.passed(), false)
        assertEquals(assessment.failed(), false)
        assertEquals(assessment.indeterminate(), true)
        assertEquals(assessment.message(), "else")
    }

    @Test
    fun can_handle_messages_with_commas()
    {
        val lines = listOf("name,something","score,-","result,failed","message,a,nother, thing")
        val assessment = Assessment.from_lines(lines)
        assertNotNull(assessment)
        assertEquals(assessment!!.name(), "something")
        assertEquals(assessment.score(), null)
        assertEquals(assessment.passed(), false)
        assertEquals(assessment.failed(), true)
        assertEquals(assessment.message(), "a,nother, thing")
    }
}