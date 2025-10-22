package com.coxphysics.terrapins.models.reports;

import java.io.IOException;
import java.io.Writer;

public class WritableString<T> implements Writable, EqualSettings
{
    private final T data_;

    private WritableString(T data)
    {
        data_ = data;
    }

    public static <T> WritableString<T> from(T data)
    {
        return new WritableString<>(data);
    }

    @Override
    public void write_to(Writer writer)
    {
        try
        {
            writer.write(data_.toString());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean has_same_settings(Object other)
    {
        if (!Helper.is_valid_for_comparison(this, other))
            return false;
        final WritableString<T> other_ = (WritableString<T>) other;
        return data_.equals(other_.data_);
    }
}
