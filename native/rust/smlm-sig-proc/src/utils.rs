pub fn get_index(row: usize, col: usize, n_cols: usize) -> usize
{
    (row * n_cols) + col
}

// pub fn get_coords(idx: usize, n_cols: usize) -> (usize, usize)
// {
//     let row = idx / n_cols;
//     let col = idx % n_cols;
//     (row, col)
// }
