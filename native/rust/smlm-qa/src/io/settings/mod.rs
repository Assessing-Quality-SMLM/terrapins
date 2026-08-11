mod version_1;

use self::version_1::{Version1};

use crate::{Error};
use crate::settings::{Settings as RealSettings};

use serde::{Deserialize, Serialize};

use std::fs::{File};
use std::io::{Read, BufReader, Write, BufWriter};
use std::path::{Path};

#[derive(Debug, Serialize, Deserialize)]
enum Settings
{
	Version1(Version1)
}

impl Settings
{
	pub fn to_settings(self) -> RealSettings
	{
		match self
		{
			Self::Version1(v) => v.to_settings()
		}
	}
}

pub fn read_settings<R: Read>(reader: R) -> Result<RealSettings, Error>
{
	serde_json::from_reader::<R, Settings>(reader).map(Settings::to_settings).map_err(Error::from)
}

pub fn read_settings_from_disk<P: AsRef<Path>>(filepath: P) -> Result<RealSettings, Error>
{
	File::open(filepath).map(BufReader::new).map_err(Error::from).and_then(read_settings)
}

pub fn write_settings<W: Write>(settings: &RealSettings, writer: W) -> Result<(), Error>
{
	let v1_settings = Version1::from(settings);
	let settings = Settings::Version1(v1_settings);
	serde_json::to_writer(writer, &settings).map_err(Error::from)
}

pub fn write_settings_to<P: AsRef<Path>>(filename: P, settings: &RealSettings) -> Result<(), Error>
{
	let write_settings = |w| write_settings(settings, w);
	File::create(filename).map_err(Error::from).map(BufWriter::new).and_then(write_settings)
}