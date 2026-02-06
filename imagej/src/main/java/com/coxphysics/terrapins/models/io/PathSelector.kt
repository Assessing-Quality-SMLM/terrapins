package com.coxphysics.terrapins.models.io

import com.coxphysics.terrapins.models.PathWrapper
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.swing.JFileChooser

class PathSelector private constructor(
    private var current_path_: PathWrapper,
    private var is_both_: Boolean,
    private var is_files_: Boolean)
{
    companion object
    {
        @JvmStatic
        fun from(path: PathWrapper, is_both: Boolean, is_files: Boolean) : PathSelector
        {
            return PathSelector(path, is_both, is_files)
        }

        @JvmStatic
        fun default_with(path: PathWrapper) : PathSelector
        {
            return from(path, true, false)
        }

        @JvmStatic
        fun default() : PathSelector
        {
            val default_path = FileSystems.getDefault().getPath("")
            return default_with(PathWrapper.from(default_path))
        }

        // for calls from Java
        @JvmStatic
        fun default_() : PathSelector
        {
            return default()
        }
    }

    fun current_path(): Path?
    {
        return current_path_.path()
    }

    fun set_current_path(path: Path)
    {
        current_path_.set_path(path)
    }

    fun set_is_files_only(value: Boolean)
    {
        is_both_ = !value;
        is_files_ = value;
    }

    private fun is_files_and_directories(): Boolean
    {
        return is_both_
    }

    private fun is_files(): Boolean
    {
        return !is_files_and_directories() && is_files_
    }

    private fun is_directories(): Boolean
    {
        return !is_files_and_directories() && !is_files()
    }

    fun find()
    {
        val jfc = JFileChooser()
        if (is_files_and_directories())
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
        else if (is_files())
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY)
        else if (is_directories())
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)

        val file = current_path()?.toFile()
        if(file != null)
            jfc.setCurrentDirectory(file)
        val result = jfc.showOpenDialog(null)
        if (result != JFileChooser.APPROVE_OPTION)
            return;
        val current_path = jfc.getSelectedFile();
        set_current_path(current_path.toPath())
    }
}