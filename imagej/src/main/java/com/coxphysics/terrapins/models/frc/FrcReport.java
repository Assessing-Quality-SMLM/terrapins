package com.coxphysics.terrapins.models.frc;

import com.coxphysics.terrapins.models.reports.Writable;

import java.io.IOException;
import java.io.Writer;

public class FrcReport implements Writable
{
    private final FRCDialogSettings settings_;
    private final FRCResult results_;

    private FrcReport(FRCDialogSettings settings, FRCResult results)
    {

        settings_ = settings;
        results_ = results;
    }

    public static FrcReport create(FRCDialogSettings settings, FRCResult results)
    {
        return new FrcReport(settings, results);
    }

    @Override
    public void write_to(Writer writer)
    {
        try
        {
            writer.write("FRC run with ");
            settings_.write_to(writer);
            writer.write("\nResulting in ");
            results_.write_to(writer);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
