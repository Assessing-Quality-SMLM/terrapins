pub fn get_index(row: usize, col: usize, n_cols: usize) -> usize
{
    (row * n_cols) + col
}

pub fn get_coords(idx: usize, n_cols: usize) -> (usize, usize)
{
    let row = idx / n_cols;
    let col = idx % n_cols;
    (row, col)
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn index_test() 
    {
        assert_eq!(get_index(0, 0, 0), 0);
        assert_eq!(get_index(0, 1, 2), 1);
        assert_eq!(get_index(1, 1, 2), 3);

        // pathologic specification
        assert_eq!(get_index(0, 1, 0), 1); //columns over specified
        assert_eq!(get_index(0, 1, 1), 1); //columns over specified
        assert_eq!(get_index(0, 2, 1), 2); //columns over specified
    }

    #[test]
    fn coords_test()
    {
        assert_eq!(get_coords(0, 1), (0, 0));
        assert_eq!(get_coords(1, 1), (1, 0));
        assert_eq!(get_coords(1, 2), (0, 1));
        assert_eq!(get_coords(3, 2), (1, 1));
    }

    #[test]
    #[should_panic]
    fn misspecified_coords_panic_test()
    {
        // pathologic specification
        assert_eq!(get_coords(0, 0), (0, 0)) //n columns under specified
    }
}