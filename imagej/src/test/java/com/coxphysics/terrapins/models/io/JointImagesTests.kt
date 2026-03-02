package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.DiskOrImage
import com.coxphysics.terrapins.models.hawk.FakeWindowManager
import com.coxphysics.terrapins.models.macros.MacroOptions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class JointImagesTests
{
    private var executor = Executors.newSingleThreadExecutor();

    @AfterEach
    fun cleanUp()
    {
        executor.shutdown();
        while (!executor.awaitTermination(100, TimeUnit.MICROSECONDS));
    }

    @Test
    fun setting_image_1_filename_changes_filepath()
    {
        val settings = JointImages.default()
        settings.image_1().set_use_image(true)
        assertEquals(settings.image_1_filepath(Paths.get("junk")), Paths.get("junk", "image_1.tiff"))

        settings.set_image_1_filename("something")
        assertEquals(settings.image_1_filepath(Paths.get("junk")).toString(), "something")
    }

    @Test
    fun setting_image_2_filename_changes_filepath()
    {
        val settings = JointImages.default()
        settings.image_2().set_use_image(true)
        assertEquals(settings.image_2_filepath(Paths.get("junk")), Paths.get("junk", "image_2.tiff"))

        settings.set_image_2_filename("something")
        assertEquals(settings.image_2_filepath(Paths.get("junk")).toString(), "something")
    }

    // THESE TESTS CANT WORK NOW
//
//    @Test
//    fun to_disk_fails_if_image_1_fails()
//    {
//        val image_1 = DiskOrImage.default()
//        image_1.set_use_disk(true)
//        assertEquals(image_1.use_disk(), true)
//        assertEquals(image_1.filename(), null)
//
//        val settings = JointImages.new(image_1, DiskOrImage.default())
//        val result = settings.to_disk_in(Paths.get("some", "thing"))
//        assertEquals(result, false)
//    }
//
//    @Test
//    fun to_disk_fails_if_image_2_fails()
//    {
//        val image_1 = DiskOrImage.from_filename("an_image.tiff")
//        image_1.set_use_disk(true)
//        assertEquals(image_1.use_disk(), true)
//        assertEquals(image_1.filename(), "an_image.tiff")
//
//        val image_2 = DiskOrImage.default()
//        image_2.set_use_disk(true)
//        assertEquals(image_2.use_disk(), true)
//        assertEquals(image_2.filename(), null)
//
//        val settings = JointImages.new(image_1, image_2)
//        val result = settings.to_disk_in(Paths.get("some", "thing"))
//        assertEquals(result, false)
//    }

    @Test
    fun to_disk_ok_if_all_images_ok()
    {
        val image_1 = DiskOrImage.from_filename("an_image.tiff")
        image_1.set_use_disk(true)
        assertEquals(image_1.use_disk(), true)
        assertEquals(image_1.filename(), "an_image.tiff")

        val image_2 = DiskOrImage.from_filename("another_image.tiff")
        image_2.set_use_disk(true)
        assertEquals(image_2.use_disk(), true)
        assertEquals(image_2.filename(), "another_image.tiff")

        val settings = JointImages.new(image_1, image_2)
        val result = settings.to_disk_in(Paths.get("some", "thing"))
        assertEquals(result, true)
    }

    @Test
    fun macro_recording_tests()
    {
        executor.submit {
            val image_1 = DiskOrImage.from_filename("some")
            val image_2 = DiskOrImage.from_filename("thing")
            val images = JointImages.new(image_1, image_2)

            images.record_to_macro_with("a", "b")

            val options = MacroOptions.from_recorder_command_options()

            val new_images = JointImages.from_macro_options_with("a", "b", options, FakeWindowManager.empty())
            assertEquals(new_images!!.image_1().filename_nn(), "some")
            assertEquals(new_images.image_2().filename_nn(), "thing")
        }.get()
    }
}