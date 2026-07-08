pub use fftw::types::{c64};

use fftw;
use fftw::array::AlignedVec;
use fftw::plan::{R2CPlan, R2CPlan64, C2RPlan, C2RPlan64, C2CPlan, C2CPlan64};
use fftw::types::{Sign, Flag};

fn circular_shift<T: Copy>(input: &[T], 
                           output: &mut [T], 
                           n_rows: usize, 
                           row_shift: usize, 
                           n_cols: usize, 
                           col_shift: usize)
{
    for idx in 0..n_rows
    {
        let o_col = (idx + row_shift) % n_rows;
        for idy in 0..n_cols 
        {
            let o_row = (idy + col_shift) % n_cols;
            let i_idx = idx * n_cols + idy;
            let o_idx = o_col * n_cols + o_row;
            output[o_idx] = input[i_idx];
        }
    }
}

pub fn fft_shift<T: Copy>(input: &[T], output: &mut [T], n_rows: usize, n_cols: usize)
{
    circular_shift(input, output, n_rows, n_rows / 2, n_cols, n_cols / 2)
}

pub fn ifft_shift<T: Copy>(input: &[T], output: &mut [T], n_rows: usize, n_cols: usize)
{
    circular_shift(input, output, n_rows, (n_rows + 1) / 2, n_cols, (n_cols + 1) / 2)
}

fn complex_to_complex(data: &[c64], sign: Sign) -> Vec<c64>
{
    // println!("complex to complex");
    let mut plan = C2CPlan64::aligned(&[data.len()], sign, Flag::ESTIMATE).unwrap();
    let mut a = AlignedVec::new(data.len());
    // println!("a size: {}", a.len());
    a.copy_from_slice(data);
    let mut b = AlignedVec::new(data.len());
    // println!("b size: {}", b.len());
    plan.c2c(&mut a, &mut b).unwrap();
    b.to_vec()
}


pub fn ifft_complex_to_complex(data: &[c64]) -> Vec<c64>
{
    complex_to_complex(data, Sign::Backward)
}

pub fn fft_complex_to_complex(data: &[c64]) -> Vec<c64>
{
    complex_to_complex(data, Sign::Forward)
}

fn get_position(row: usize, col: usize, n_cols: usize) -> usize
{
    (row * n_cols) + col
}

fn get_coords(idx: usize, n_cols: usize) -> (usize, usize)
{
    let row = idx / n_cols;
    let col = idx % n_cols;
    (row, col)
}

pub fn hermitian_extend(data: &[c64], n_rows: usize, n_cols: usize) -> Vec<c64>
{
    let data_cols = n_cols / 2 + 1;
    let n = n_rows * n_cols;
    let mut full_data = vec![c64::new(0.0, 0.0); n];
    for (idx, value) in data.iter().enumerate()
    {
        let (row, col) = get_coords(idx, data_cols);
        let new_idx = get_position(row, col, n_cols);
        let conj_row = n_rows - (row + 1);
        let conj_col = n_cols - col;
        let conj_idx = get_position(conj_row, conj_col, n_cols);
        // let value = c64::new(value.re, value.im);
        full_data[new_idx] = *value;
        if col == 0
        {
            continue;
        }
        full_data[conj_idx] = value.conj();

    }
    full_data
}

pub fn fft_real_to_complex(data: &[f64]) -> Vec<c64>
{
    // println!("real to complex");
    let mut plan = R2CPlan64::aligned(&[data.len()], Flag::ESTIMATE).unwrap();
    let mut a = AlignedVec::new(data.len());
    // println!("a size: {}", a.len());
    a.copy_from_slice(data);
    let mut b = AlignedVec::new(data.len() / 2 + 1);
    // println!("b size: {}", b.len());
    plan.r2c(&mut a, &mut b).unwrap();
    b.to_vec()
}

pub fn fft_complex_to_real(data: &[c64]) -> Vec<f64>
{
    // println!("complex to real");
    let mut plan = C2RPlan64::aligned(&[data.len()], Flag::ESTIMATE).unwrap();
    let x = data.len() / 2 + 1;
    let mut a = AlignedVec::new(x);
    a.copy_from_slice(&data[0..x]);
    // println!("a size: {}", a.len());
    let mut b = AlignedVec::new(data.len());
    // println!("b size: {}", b.len());
    plan.c2r(&mut a, &mut b).unwrap();
    b.to_vec()
}

pub fn normalise_round_trip(data: &mut[f64]) -> ()
{
    let normalisation_factor = data.len() as f64;
    for value in data.iter_mut()
    {
        *value = *value / normalisation_factor
    }
}

pub fn fft_2d(data: &[f64], _n_rows: usize, _n_cols: usize) -> Vec<c64>
{
    let c_data = data.iter().map(|x| c64::new(*x, 0.0)).collect::<Vec<_>>();
    fft_complex_to_complex(&c_data)
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn fft_shift_test_1d() 
    {
        let data = [0.0,  0.25, -0.5, -0.25];
        let mut output = [0.0; 4];
        fft_shift(&data, &mut output, 1, 4);
        assert_eq!(output, [-0.5, -0.25, 0.0, 0.25]);
    }

    #[test]
    fn fft_shift_test_2d() 
    {
        let data = [0.0,  1.0,  2.0, -2.0, -1.0,
                    0.1,  1.1,  2.1, -1.9, -0.9, 
                    0.2,  1.2,  2.2, -1.8, -0.8,
                    -0.2,  0.8,  1.8, -2.2, -1.2,
                    -0.1,  0.9,  1.9, -2.1, -1.1];
        let mut output = [0.0;25];
        let expected = [-2.2, -1.2, -0.2,  0.8,  1.8,
                        -2.1, -1.1, -0.1,  0.9,  1.9,
                        -2.0, -1.0,  0.0,  1.0,  2.0,
                        -1.9, -0.9,  0.1,  1.1,  2.1,
                        -1.8, -0.8,  0.2,  1.2,  2.2];
        fft_shift(&data, &mut output, 5, 5);
        assert_eq!(output, expected);
    }

    #[test]
    fn fft_shift_test_2d_rectangular() 
    {
        let data = [0.0,  1.0,  2.0, -2.0,
                    0.1,  1.1,  2.1, -1.9, 
                    0.2,  1.2,  2.2, -1.8,
                    -0.2,  0.8,  1.8, -2.2,
                    -0.1,  0.9,  1.9, -2.1];
        let mut output = [0.0; 20];
        let expected = [1.8, -2.2, -0.2, 0.8, 
                        1.9, -2.1, -0.1, 0.9, 
                        2.0, -2.0, 0.0, 1.0, 
                        2.1, -1.9, 0.1, 1.1, 
                        2.2, -1.8, 0.2, 1.2];
        fft_shift(&data, &mut output, 5, 4);
        assert_eq!(output, expected);
    }
    

    #[test]
    fn ifft_shift_test_1d() 
    {
        let data = [-0.5, -0.25, 0.0, 0.25];
        let mut output = [0.0;4];
        ifft_shift(&data, &mut output, 1, 4);
        assert_eq!(output, [0.0,  0.25, -0.5, -0.25]);
    }

    #[test]
    fn ifft_shift_test_2d() 
    {        
        let data = [-2.2, -1.2, -0.2,  0.8,  1.8,
                    -2.1, -1.1, -0.1,  0.9,  1.9,
                    -2.0, -1.0,  0.0,  1.0,  2.0,
                    -1.9, -0.9,  0.1,  1.1,  2.1,
                    -1.8, -0.8,  0.2,  1.2,  2.2];
        let mut output = [0.0;25];
        ifft_shift(&data, &mut output, 5, 5);

        let expected = [0.0,  1.0,  2.0, -2.0, -1.0,
                        0.1,  1.1,  2.1, -1.9, -0.9, 
                        0.2,  1.2,  2.2, -1.8, -0.8,
                        -0.2,  0.8,  1.8, -2.2, -1.2,
                        -0.1,  0.9,  1.9, -2.1, -1.1];
        assert_eq!(output, expected);
    }

    #[test]
    fn shift_unity() 
    {
        let data = [0.0,  1.0,  2.0, -2.0, -1.0,
                    0.1,  1.1,  2.1, -1.9, -0.9, 
                    0.2,  1.2,  2.2, -1.8, -0.8,
                    -0.2,  0.8,  1.8, -2.2, -1.2,
                    -0.1,  0.9,  1.9, -2.1, -1.1];
        let mut output = [0.0;25];        
        fft_shift(&data, &mut output, 5, 5);
        let mut output2 = [0.0;25];
        ifft_shift(&output, &mut output2, 5, 5);
        assert_eq!(output2, data);
    }

    #[test]
    fn round_trip_r2c_c2r()
    {
        let data = (0..10).map(|x| x as f64).collect::<Vec<f64>>();
        let ft = fft_real_to_complex(&data.clone());
        let e_ft = hermitian_extend(&ft, 1, 10);
        let mut ift = fft_complex_to_real(&e_ft); // unnormalised
        normalise_round_trip(&mut ift);
        #[cfg(target_os = "macos")]
        let expected = [0.0, 1.0, 1.9999999999999996, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
        #[cfg(not(target_os = "macos"))]
        let expected = [0.0, 1.0000000000000007, 2.0, 2.9999999999999996, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
        assert_eq!(ift, expected);
    }

    #[test]
    fn round_trip_c2c_c2c()
    {
        let data = (0..10).map(|x| c64::new(x as f64, 0.0)).collect::<Vec<c64>>();
        let ft = fft_complex_to_complex(&data.clone());
        let ift = ifft_complex_to_complex(&ft); // unnormalised        
        let mut real_ift = ift.iter().map(|x| x.re).collect::<Vec<f64>>();
        normalise_round_trip(&mut real_ift);
        #[cfg(target_os = "macos")]
        let expected = [0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
        #[cfg(not(target_os = "macos"))]
        let expected = [0.0, 1.0000000000000007, 2.0, 2.9999999999999996, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0];
        assert_eq!(real_ift, expected);
    }

    // #[test]
    // fn r2c_vs_c2c()
    // {
    //     let limit = 1000;
    //     let data = (0..limit).map(|x| x as f64).collect::<Vec<f64>>();
    //     let c_data = data.iter().map(|x| c64::new(*x, 0.0)).collect::<Vec<c64>>();
    //     let cft = fft_complex_to_complex(&c_data);
    //     let rft = fft_real_to_complex(&data);
    //     let extended_real = hermitian_extend(&rft, 1, limit);
    //     for (a, b) in extended_real.iter().zip(cft.iter())
    //     {
    //         println!("{}", a - b);
    //     }
    //     assert_eq!(extended_real, cft)
    // }
}