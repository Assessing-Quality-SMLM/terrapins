package com.coxphysics.terrapins.models.log

class ListLog<T>() : Log<T>
{
    private val log_ = mutableListOf<T>()

    fun log(): List<T>
    {
        return log_
    }

    override fun log(item: T)
    {
        log_.addLast(item)
    }
}