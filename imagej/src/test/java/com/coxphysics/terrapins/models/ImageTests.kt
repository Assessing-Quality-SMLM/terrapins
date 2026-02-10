package com.coxphysics.terrapins.models

import com.coxphysics.terrapins.models.utils.StringUtils
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImageTests
{

    @Test
    fun empty_image_title_nn()
    {
        assertEquals(Image.empty().title_nn(), StringUtils.EMPTY_STRING)
    }

}