mod method;
mod settings;

pub use self::method::{SplitMethod};
pub use self::settings::{Settings};

use crate::io::{ParseMethod, ThunderStormParser, collect_stream, write_item, write_headers};

use rand::{Rng};
use rand::rngs::ThreadRng;
use rand::seq::{SliceRandom};
use rand::thread_rng;

use std::fmt::{Display};
use std::fs::{File};
use std::io::{BufRead, BufReader, Write, BufWriter, Error as IoError};
use std::path::Path;

fn advance<T: Display, I: Iterator<Item=Result<T, IoError>>, W: Write>(mut localisations: I, stream: W) -> Result<bool, IoError>
{
    localisations.next()
                 .map(|l| write_item(stream, l?).and(Ok(false)))
                 .unwrap_or(Ok(true))
}

fn iterator_alternate_split<T: Display, U: Display, I: Iterator<Item=Result<T, IoError>>, W: Write, X: Write>(header_lines: &[U], mut localisations: I, mut stream_1: W, mut stream_2: X) -> Result<(), IoError>
{
    let _ = write_headers(&mut stream_1, header_lines)?;
    let _ = write_headers(&mut stream_2, header_lines)?;
    loop 
    {
        let stop = advance(&mut localisations, &mut stream_1)?;
        if stop
        {
            return Ok(())
        }
        let stop = advance(&mut localisations, &mut stream_2)?;
        if stop
        {
            return Ok(())
        }
    }
}

fn advance_block<T: Display, I: Iterator<Item=Result<T, IoError>>, W: Write, X: Write>(block_size: usize, mut localisations: I, mut stream_1: W, mut stream_2: X) -> Result<bool, IoError>
{
    let half_way = block_size / 2;
    let mut count = 0;
    while count < half_way
    {
        let stop = advance(&mut localisations, &mut stream_1)?;
        if stop
        {
            return Ok(true)
        }
        count += 1;
    }
    while count < block_size
    {
        let stop = advance(&mut localisations, &mut stream_2)?;
        if stop
        {
            return Ok(true)
        }
        count += 1;
    }
    Ok(false)
}

fn _stream_blocks<T: Display, I: Iterator<Item=Result<T, IoError>>, W: Write, X: Write>(block_size: usize, mut lines: I, mut stream_1: W, mut stream_2: X) -> Result<(), IoError>
{
    loop
    {
        let stop = advance_block(block_size, &mut lines, &mut stream_1, &mut stream_2)?;
        if stop
        {
            return Ok(())
        }
    }
}

fn stream_blocks<R: BufRead, W: Write, X: Write>(block_size: usize, n_headers: usize, input_stream: R, mut stream_1: W, mut stream_2: X) -> Result<(), IoError>
{
    let mut lines = input_stream.lines();
    let headers = (&mut lines).take(n_headers).collect::<Result<Vec<_>, _>>()?;
    let _ = write_headers(&mut stream_1, &headers)?;
    let _ = write_headers(&mut stream_2, &headers)?;    
    _stream_blocks(block_size, lines, stream_1, stream_2)
}

fn large_stream_alternate_split<R: BufRead, W: Write, X: Write>(input_stream: R, stream_1: W, stream_2: X, n_headers: usize) -> Result<(), IoError>
{
    let mut lines = input_stream.lines();
    let headers = (&mut lines).take(n_headers).collect::<Result<Vec<_>, _>>()?;
    iterator_alternate_split(&headers, lines, stream_1, stream_2)
}

fn iterator_half_split<T: Display, U: Display, I: Iterator<Item=Result<T, IoError>>, W: Write, X: Write>(half_way: usize, header_lines: &[U], mut localisations: I, mut stream_1: W, mut stream_2: X) -> Result<(), IoError>
{
    let _ = write_headers(&mut stream_1, header_lines)?;
    let _ = write_headers(&mut stream_2, header_lines)?;
    let mut count = 0;
    while count < half_way
    {
        let stop = advance(&mut localisations, &mut stream_1)?;
        if stop
        {
            return Ok(())
        }
        count += 1;
    }
    loop
    {
        let stop = advance(&mut localisations, &mut stream_2)?;
        if stop
        {
            return Ok(())
        }
    }
}

fn large_stream_half_split<R: BufRead, W: Write, X: Write>(input_stream: R, stream_1: W, stream_2: X, n_headers: usize) -> Result<(), IoError>
{
    let lines: Vec<_> = input_stream.lines().collect();
    let half_way = lines.len() / 2;
    let mut line_iter = lines.into_iter(); 
    let headers = (&mut line_iter).take(n_headers).collect::<Result<Vec<_>, _>>()?;
    iterator_half_split(half_way, &headers, line_iter, stream_1, stream_2)
}

fn randomise_then_split_with<R: Rng, T: Display, U: Display, W: Write, X: Write>(mut rng: R, headers: &[U], localisations: &mut [T], a_stream: W, b_stream: X) -> Result<(), IoError>
{
    localisations.shuffle(&mut rng);
    iterator_alternate_split(headers, localisations.iter().map(Ok), a_stream, b_stream)
}

fn randomise_then_split<T: Display, U: Display, W: Write, X: Write>(localisations: &mut [T], headers: &[U], a_stream: W, b_stream: X) -> Result<(), IoError>
{
    let rng = thread_rng();
    randomise_then_split_with(rng, headers, localisations, a_stream, b_stream)
}

fn n_header_lines(settings: &Settings) -> usize
{
    match settings.parse_method()
    {
        ParseMethod::ThunderStorm => 
        {
            ThunderStormParser::n_header_lines()
        }
        ParseMethod::Csv(settings) => 
        {
            settings.n_header_lines()
        }
    }
}

fn advance_into<T, I: Iterator<Item = T>>(mut localisations: I, container: &mut Vec<T>) -> bool
{
    match localisations.next()
    {
        None => false,
        Some(t) =>
        {
            container.push(t);
            true
        }
    }
}

fn advance_n_into<T, I: Iterator<Item = T>>(n: usize, localisations: I, a: &mut Vec<T>) -> bool
{
    let start = a.len();
    for l in localisations.take(n)
    {
        a.push(l);
    }
    a.len() - start == n
}

fn zip_into<T, I: Iterator<Item = T>>(mut localisations: I, a: &mut Vec<T>, b: &mut Vec<T>) -> ()
{
    while advance_into(&mut localisations, a) && advance_into(&mut localisations, b)
    {
    }
}

pub fn zip<T>(localisations: &[T]) -> (Vec<&T>, Vec<&T>)
{
    let half = localisations.len() / 2;
    let mut a = Vec::with_capacity(half);
    let mut b = Vec::with_capacity(half);
    zip_into(localisations.iter(), &mut a, &mut b);
    (a, b)
}

pub fn zip_drain<T>(mut localisations: Vec<T>) -> (Vec<T>, Vec<T>)
{
    let half = localisations.len() / 2;
    let mut a = Vec::with_capacity(half);
    let mut b = Vec::with_capacity(half);
    zip_into(localisations.drain(0..), &mut a, &mut b);
    (a, b)
}

pub fn half<T>(localisations: &[T]) -> (&[T], &[T])
{
    let half = localisations.len() / 2;
    (&localisations[0..half], &localisations[half..])
}

fn add_block<T, I: Iterator<Item = T>>(half_block: usize, mut localisations: I, a: &mut Vec<T>, b: &mut Vec<T>) -> bool
{
    advance_n_into(half_block, &mut localisations, a) && advance_n_into(half_block, &mut localisations, b)
}

fn add_blocks<T, I: Iterator<Item = T>>(block_size: usize, mut localisations: I, a: &mut Vec<T>, b: &mut Vec<T>) -> ()
{
    let half = block_size / 2;
    while add_block(half, &mut localisations, a, b)
    {

    }

}

fn drift_iter<T, I: Iterator<Item=T>>(localisations: I, block_size: usize) -> (Vec<T>, Vec<T>)
{
    let mut a = Vec::new();
    let mut b = Vec::new();
    add_blocks(block_size, localisations, &mut a, &mut b);
    (a, b)
}

pub fn block_split<T>(localisations: &[T], block_size: usize) -> (Vec<&T>, Vec<&T>)
{
    drift_iter(localisations.iter(), block_size)    
}

pub fn block_split_copy<T: Copy>(localisations: &[T], block_size: usize) -> (Vec<T>, Vec<T>)
{
    drift_iter(localisations.iter().copied(), block_size)
}

fn randomise_with<R: Rng, T>(mut rng: R, localisations: &mut [T])
{
    localisations.shuffle(&mut rng);
}

fn default_rng() -> ThreadRng
{
    thread_rng()
}

pub fn randomise<T>(localisations: &mut [T])
{    
    randomise_with(default_rng(), localisations);
}

fn random_with<R: Rng, T>(rng: R, localisations: &mut [T]) -> (&[T], &[T])
{
    randomise_with(rng, localisations);
    half(localisations)
}

pub fn random<T>(localisations: &mut [T]) -> (&[T], &[T])
{
    random_with(default_rng(), localisations)
}

fn immutable_random_with<R: Rng, T: Clone>(rng: R, localisations: &[T]) -> (Vec<T>, Vec<T>)
{
    let mut l = localisations.to_vec();
    randomise_with(rng, &mut l);
    zip_drain(l)
}

pub fn immutable_random<T: Clone>(localisations: &[T]) -> (Vec<T>, Vec<T>)
{
    immutable_random_with(default_rng(), localisations)
}

pub fn split<P: AsRef<Path>, Q: AsRef<Path>, R: AsRef<Path>>(localisations_filename: P, a_filename: Q, b_filename: R, settings: &Settings) -> Result<(), String>
{
    let input_stream = File::open(localisations_filename).map(BufReader::new).map_err(|e| e.to_string())?;
    let a_stream = File::create(a_filename).map(BufWriter::new).map_err(|e| e.to_string())?;
    let b_stream = File::create(b_filename).map(BufWriter::new).map_err(|e| e.to_string())?;
    match settings.method()
    {        
        SplitMethod::Zip =>
        {
            let n_headers = n_header_lines(settings);
            large_stream_alternate_split(input_stream, a_stream, b_stream, n_headers).map_err(|e| e.to_string())
        },
        SplitMethod::HalfIndex => 
        {
            let n_headers = n_header_lines(settings);
            large_stream_half_split(input_stream, a_stream, b_stream, n_headers).map_err(|e| e.to_string())
        }
        SplitMethod::Random => 
        {
            let n_headers = n_header_lines(settings);
            let stream_data = collect_stream(input_stream, n_headers)?;
            let (headers, mut data) = stream_data.take();
            randomise_then_split(&mut data, &headers, a_stream, b_stream).map_err(|e| e.to_string())
        }
        SplitMethod::Block(block_size) => 
        {
            let n_headers = n_header_lines(settings);
            stream_blocks(*block_size, n_headers, input_stream, a_stream, b_stream).map_err(|e| e.to_string())
        }
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;
    
    use crate::rand::SeedableRng;
    use rand::rngs::StdRng;

    use std::io::{ErrorKind};
    use std::iter::{empty, Empty, once, Once};
const DATA_EVEN: &str = r#"header
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696"#;

const DATA_ODD: &str = r#"header
1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696
5,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696"#;

    #[test]
    fn large_alternate_split_test_even() 
    {
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let _ = large_stream_alternate_split(DATA_EVEN.as_bytes(), &mut stream_1, &mut stream_2, 1);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        let expected_2 = "header\n2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }

    #[test]
    fn large_alternate_split_test_odd() 
    {
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let _ = large_stream_alternate_split(DATA_ODD.as_bytes(), &mut stream_1, &mut stream_2, 1);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n5,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        let expected_2 = "header\n2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }

    #[test]
    fn large_half_split_test_even()
    {
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let _ = large_stream_half_split(DATA_EVEN.as_bytes(), &mut stream_1, &mut stream_2, 1);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        let expected_2 = "header\n3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }

    #[test]
    fn large_half_split_test_odd()
    {
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let _ = large_stream_half_split(DATA_ODD.as_bytes(), &mut stream_1, &mut stream_2, 1);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        let expected_2 = "header\n4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n5,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }

    #[test]
    fn random_split_test_even() 
    {
        let rng = StdRng::seed_from_u64(2);
        let mut localisations = [1, 2, 3, 4];
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let result = randomise_then_split_with(rng, &["header"], &mut localisations, &mut stream_1, &mut stream_2);
        assert_eq!(result.is_ok(), true);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n3\n4";
        let expected_2 = "header\n2\n1";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }

    #[test]
    fn random_split_test_odd() 
    {
        let rng = StdRng::seed_from_u64(2);
        let mut localisations = [1, 2, 3, 4, 5];
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let result = randomise_then_split_with(rng, &["header"], &mut localisations, &mut stream_1, &mut stream_2);
        assert_eq!(result.is_ok(), true);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n3\n5\n1";
        let expected_2 = "header\n4\n2";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }

    #[test]
    fn advance_propogates_error()
    {
        let iter: Once<Result<u8, IoError>> = once(Err(IoError::new(ErrorKind::Other, "uh oh")));
        let mut stream = Vec::new();
        let result = advance(iter, &mut stream);
        assert_eq!(result.unwrap_err().to_string(), "uh oh")
    }

    #[test]
    fn advance_returns_true_at_stream_end()
    {
        let iter : Empty<Result<u8, IoError>> = empty();
        let mut stream = Vec::new();
        let result = advance(iter, &mut stream);
        assert_eq!(result.unwrap(), true)
    }

    #[test]
    fn advance_returns_false_if_more_items()
    {
        let items = [1, 2];
        let mut stream = Vec::new();
        let result = advance(items.iter().map(Ok), &mut stream);
        assert_eq!(result.unwrap(), false)
    }

    #[test]
    fn alternate_stream_split()
    {
        let iter = (0..10).map(Ok);
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let result = iterator_alternate_split(&["header"], iter, &mut stream_1, &mut stream_2);
        assert_eq!(result.unwrap(), ());
        let stream_1_text = std::str::from_utf8(&stream_1).unwrap();
        let stream_2_text = std::str::from_utf8(&stream_2).unwrap();
        assert_eq!(stream_1_text, "header\n0\n2\n4\n6\n8");
        assert_eq!(stream_2_text, "header\n1\n3\n5\n7\n9");
    }

    #[test]
    fn zip_test()
    {
        let locs = [1, 2, 3, 4, 5];
        let expected = (vec![&1, &3, &5], vec![&2, &4]);
        assert_eq!(zip(&locs), expected)
    }

    #[test]
    fn zip_drain_test()
    {
        let locs = vec![1, 2, 3, 4, 5];
        let expected = (vec![1, 3, 5], vec![2, 4]);
        assert_eq!(zip_drain(locs), expected)
    }

    #[test]
    fn half_test()
    {
        let locs = [1, 2, 3, 4, 5];
        let result = half(&locs);
        assert_eq!(result.0, [1, 2]);
        assert_eq!(result.1, [3, 4, 5]);
    }

    #[test]
    fn random_test()
    {
        let rng = StdRng::seed_from_u64(2);
        let mut locs = [1, 2, 3, 4, 5];
        randomise_with(rng, &mut locs);
        assert_eq!(locs, [3, 4, 5, 2, 1])
    }

    #[test]
    fn random_split_test()
    {
        let rng = StdRng::seed_from_u64(2);
        let mut locs = [1, 2, 3, 4, 5];        
        {
            let (a, b) = random_with(rng, &mut locs);
            assert_eq!(a, [3, 4]);
            assert_eq!(b, [5, 2, 1]);
        }
        assert_eq!(locs, [3, 4, 5, 2, 1]);
    }

    #[test]
    fn immutable_random_test()
    {
        let rng = StdRng::seed_from_u64(2);
        let locs = [1, 2, 3, 4, 5];
        let (a, b) = immutable_random_with(rng, &locs);
        assert_eq!(locs, [1, 2, 3, 4, 5]);
        assert_eq!(a, [3, 5, 1]);
        assert_eq!(b, [4, 2]);
    }

    #[test]
    fn advance_n_into_ok()
    {
        let data = (0..10).collect::<Vec<usize>>();
        let mut buffer = Vec::new();
        assert_eq!(advance_n_into(10, data.clone().into_iter(), &mut buffer), true);
        assert_eq!(data, buffer)
    }

    #[test]
    fn advance_n_into_not_enough_data()
    {
        let data = (0..5).collect::<Vec<usize>>();
        let mut buffer = Vec::new();
        assert_eq!(advance_n_into(10, data.clone().into_iter(), &mut buffer), false);
        assert_eq!(data, buffer)
    }

    #[test]
    fn advance_n_into_data_reamining()
    {
        let data = (0..5).collect::<Vec<usize>>();
        let mut buffer = Vec::new();
        assert_eq!(advance_n_into(5, data.clone().into_iter(), &mut buffer), true);
        assert_eq!(data[0..5], buffer)
    }

    #[test]
    fn block_split_basic()
    {
        let block_size = 10;
        let data = (0..20).collect::<Vec<usize>>();
        let (a, b) = block_split(&data, block_size);
        assert_eq!(a, vec![&0, &1, &2, &3, &4, &10, &11, &12, &13, &14]);
        assert_eq!(b, vec![&5, &6, &7, &8, &9, &15, &16, &17, &18, &19]);
    }

    #[test]
    fn block_split_uneven()
    {
        let block_size = 10;
        let data = (0..19).collect::<Vec<usize>>();
        let (a, b) = block_split(&data, block_size);
        assert_eq!(a, vec![&0, &1, &2, &3, &4, &10, &11, &12, &13, &14]);
        assert_eq!(b, vec![&5, &6, &7, &8, &9, &15, &16, &17, &18]);
    }

    #[test]
    fn block_split_too_small()
    {
        let block_size = 10;
        let data = (0..4).collect::<Vec<usize>>();
        let (a, b) = block_split(&data, block_size);
        assert_eq!(a, vec![&0, &1, &2, &3]);
        assert_eq!(b, Vec::<&usize>::new());
    }

    #[test]
    fn block_split_stream_even() 
    {
        let block_size = 4;
        let n_headers = 1;
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let _ = stream_blocks(block_size, n_headers, DATA_EVEN.as_bytes(), &mut stream_1, &mut stream_2);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        let expected_2 = "header\n3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }

    #[test]
    fn block_split_stream_odd() 
    {
        let block_size = 4;
        let n_headers = 1;
        let mut stream_1 = Vec::new();
        let mut stream_2 = Vec::new();
        let _ = stream_blocks(block_size, n_headers, DATA_ODD.as_bytes(), &mut stream_1, &mut stream_2);
        let s_1 = std::str::from_utf8(&stream_1).unwrap();
        let s_2 = std::str::from_utf8(&stream_2).unwrap();
        let expected_1 = "header\n1,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n2,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n5,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        let expected_2 = "header\n3,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696\n4,685.44715,2145.33857,104.22976,1496790.54948,0,20572.23365,136811.47493,9.81696";
        assert_eq!(s_1, expected_1);
        assert_eq!(s_2, expected_2);
    }
}