package com.coxphysics.terrapins.models.hawkman.external;

import kotlin.Pair;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResultsTests
{
    @Test
    public void can_parse_out_of_order_levels()
    {
        String data = "1,2\n5,6\n3,4";
        StringReader reader = new StringReader(data);
        Pair<List<Integer>, List<Double>> lists = Results.read_scores_from(new BufferedReader(reader));
        int[] expected_levels = new int[]{1, 3, 5};
        int[] levels = lists.component1().stream().mapToInt(Integer::intValue).toArray();
        assertArrayEquals(levels, expected_levels);
    }

    @Test
    public void null_line_no_data()
    {
        String data = "1,2\njunk\n3,4";
        StringReader reader = new StringReader(data);
        Pair<List<Integer>, List<Double>> lists = Results.read_scores_from(new BufferedReader(reader));
        assertNull(lists);
    }
}
