#[cfg(feature = "plot")]
use gnuplot::{Figure, AxesCommon};

#[cfg(feature = "plot")]
pub fn plot_x_and_y(x: &[f64], y: &[f64], title: &str) -> ()
{
	let mut figure = Figure::new();
    figure.axes2d()
          .set_title(title, &[])
          .points(x, y, &[]);
    figure.show_and_keep_running().unwrap();
}

#[cfg(not(feature = "plot"))]
pub fn plot_x_and_y(_x: &[f64], _y: &[f64], _title: &str) -> ()
{
	()
}

#[cfg(feature = "plot")]
pub fn plot_data(data: &[f64], title: &str) -> ()
{
	let x = (0..data.len()).map(|x| x as f64).collect::<Vec<f64>>();
	plot_x_and_y(&x, data, title)
}

#[cfg(not(feature = "plot"))]
pub fn plot_data(_data: &[f64], _title: &str) -> ()
{
	()
}


#[cfg(feature = "plot")]
pub fn plot_frc(qs: &[f64], frcs: &[f64], threshold_curve: &[f64]) -> ()
{
	let mut figure = Figure::new();
    figure.axes2d()
          .set_title("FRC", &[])
          .points(qs, frcs, &[])
          .points(qs, threshold_curve, &[]);
    let _ = figure.show_and_keep_running();
}

#[cfg(not(feature = "plot"))]
pub fn plot_frc(_qs: &[f64], _frcs: &[f64], _threshold_curve: &[f64]) -> ()
{
	()
}