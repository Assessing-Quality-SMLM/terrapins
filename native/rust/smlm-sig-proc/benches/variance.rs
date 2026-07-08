extern crate sig_proc;

use sig_proc::stats::variance::{naive, shifted, two_pass, welfords};

use criterion::{black_box, criterion_group, criterion_main, Criterion};

const DATA: [f64; 4] = [4.0, 7.0, 13.0, 16.0];

fn bench_naive_small(c: &mut Criterion) 
{
    c.bench_function("naive small", |b| b.iter(|| naive::variance(black_box(&DATA))));
}

fn bench_shifted_small(c: &mut Criterion) 
{
    c.bench_function("shifted small", |b| b.iter(|| shifted::variance(black_box(&DATA))));
}

fn bench_two_pass_small(c: &mut Criterion) 
{
    c.bench_function("two_pass small", |b| b.iter(|| two_pass::variance(black_box(&DATA))));
}

fn bench_welfords_small(c: &mut Criterion) 
{
    c.bench_function("welfords small", |b| b.iter(|| welfords::variance(black_box(&DATA))));
}

criterion_group!(benches, 
                 bench_naive_small, 
                 bench_shifted_small,
                 bench_two_pass_small,
                 bench_welfords_small,
                 );
criterion_main!(benches);