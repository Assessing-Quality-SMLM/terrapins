package com.coxphysics.terrapins.models.process;

public class FakeRunner implements Runner
{
    private final int exit_code_;

    private FakeRunner(int exitCode)
    {
        exit_code_ = exitCode;
    }

    public static FakeRunner with_exit_code(int code)
    {
        return new FakeRunner(code);
    }

    @Override
    public int run(ProcessBuilder process_builder)
    {
        return exit_code_;
    }
}
