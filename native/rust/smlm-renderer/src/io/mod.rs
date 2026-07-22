use crate::{NmFrame, ImageFrame};

fn parse_value(s: &str) -> Result<usize, String>
{
    s.parse::<usize>().map_err(|e| format!("cannot parse {s} to f64: {}", e.to_string()))
}

fn parse_value_f64(s: &str) -> Result<f64, String>
{
    s.parse::<f64>().map_err(|e| format!("cannot parse {s} to f64: {}", e.to_string()))
}

pub fn parse_global_frame_px(value: &str) -> Result<ImageFrame, String>
{
    let splits = value.split(",").collect::<Vec<&str>>();
    if splits.len() < 4
    {
        return Err(format!("{value} cannot be split into row, column, height, width"));
    }
    let row_start = parse_value(splits[0])?;
    let col_start = parse_value(splits[1])?;
    let height = parse_value(splits[2])?;
    let width = parse_value(splits[3])?;
    let x_start = col_start;
    let y_start = row_start;
    Ok(ImageFrame::from(x_start, y_start, width, height))
}

pub fn parse_global_frame_nm(value: &str) -> Result<NmFrame, String>
{
    let splits = value.split(",").collect::<Vec<&str>>();
    if splits.len() < 4
    {
        return Err(format!("{value} cannot be split into row, column, height, width"));
    }
    let row_start = parse_value_f64(splits[0])?;
    let col_start = parse_value_f64(splits[1])?;
    let height = parse_value_f64(splits[2])?;
    let width = parse_value_f64(splits[3])?;
    let x_start = col_start;
    let y_start = row_start;
    Ok(NmFrame::from(x_start, y_start, width, height))
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn basic_parse_global_frame()
    {
        let description = "10,0,5,200";
        let frame = parse_global_frame_px(description).unwrap();
        assert_eq!(frame, ImageFrame::from(0, 10, 200, 5));
    }

    #[test]
    fn not_enough_arguments()
    {
        let description = "";
        let error = parse_global_frame_px(description).unwrap_err();
        assert_eq!(error, " cannot be split into row, column, height, width");
    }

    #[test]
    fn cannot_parse_row()
    {
        let description = "junk,0,5,200";
        let error = parse_global_frame_px(description).unwrap_err();
        assert_eq!(error, "cannot parse junk to f64: invalid digit found in string");
    }

    #[test]
    fn cannot_parse_col()
    {
        let description = "10,junk,5,200";
        let error = parse_global_frame_px(description).unwrap_err();
        assert_eq!(error, "cannot parse junk to f64: invalid digit found in string");
    }

    #[test]
    fn cannot_parse_height()
    {
        let description = "10,0,junk,200";
        let error = parse_global_frame_px(description).unwrap_err();
        assert_eq!(error, "cannot parse junk to f64: invalid digit found in string");
    }

    #[test]
    fn cannot_parse_width()
    {
        let description = "10,0,5,junk";
        let error = parse_global_frame_px(description).unwrap_err();
        assert_eq!(error, "cannot parse junk to f64: invalid digit found in string");
    }
}