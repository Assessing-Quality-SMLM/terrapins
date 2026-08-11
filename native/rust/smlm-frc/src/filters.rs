pub const TUKEY: &'static str = "tukey";

pub fn tukey() -> &'static str
{
	TUKEY
}

pub fn default_tukey_filter() -> (&'static str, &'static [f64])
{
    ("tukey", &[0.25])
}