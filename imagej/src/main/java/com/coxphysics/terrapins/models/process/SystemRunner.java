package com.coxphysics.terrapins.models.process;

public class SystemRunner implements Runner
{
    public SystemRunner()
    {
    }

    @Override
    public int run(ProcessBuilder process_builder)
    {
        try
        {
            Process process = process_builder.start();
            return process.waitFor();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
