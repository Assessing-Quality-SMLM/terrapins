import smlm_terrapins as tp


def test_add_parameter_does_nothing_if_value_is_none():
    commands = []
    tp.add_parameter(commands, "some", None)
    assert commands == []