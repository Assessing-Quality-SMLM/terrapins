package com.coxphysics.terrapins.models.frc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FRCTests
{
    @Test
    public void parse_one_seventh_threshold()
    {
        assertEquals(FRC.to_command(ThresholdMethod.ONE_SEVENTH), "17");
    }

    @Test
    public void parse_half_bit_threshold()
    {
        assertEquals(FRC.to_command(ThresholdMethod.HALF_BIT), "half");
    }

    @Test
    public void parse_one_sigma_threshold()
    {
        assertEquals(FRC.to_command(ThresholdMethod.ONE_SIGMA), "sigma_1");
    }

    @Test
    public void parse_two_sigma_threshold()
    {
        assertEquals(FRC.to_command(ThresholdMethod.TWO_SIGMA), "sigma_2");
    }

    @Test
    public void parse_three_sigma_threshold()
    {
        assertEquals(FRC.to_command(ThresholdMethod.THREE_SIGMA), "sigma_3");
    }

    @Test
    public void parse_four_sigma_threshold()
    {
        assertEquals(FRC.to_command(ThresholdMethod.FOUR_SIGMA), "sigma_4");
    }
}
