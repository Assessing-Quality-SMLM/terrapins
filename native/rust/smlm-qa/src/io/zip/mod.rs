use crate::Error;

use walkdir::{WalkDir, DirEntry};

use ::zip::{read::ZipFile, write::SimpleFileOptions, result::ZipError as InnerZipError};

use std::fs;
use std::fs::{File};
use std::io;
use std::io::{Read, Write, Seek};
use std::path::{Path, PathBuf, StripPrefixError};


const EXTENSION : &str = "smlm";

#[derive(Debug)]
pub enum ZipError
{
    Zip(InnerZipError),
    InvalidFileName(String),    
}

impl From<StripPrefixError> for ZipError
{
    fn from(error: StripPrefixError) -> ZipError
    {
        Self::InvalidFileName(error.to_string())
    }
}

impl From<InnerZipError> for Error
{
    fn from(error: InnerZipError) -> Self 
    {
        Self::from(ZipError::Zip(error))
    }
}

impl From<StripPrefixError> for Error
{
    fn from(error: StripPrefixError) -> Self 
    {
        Self::from(ZipError::from(error))
    }
}

pub fn extract_data<P: AsRef<Path>>(data_file: P) -> Result<(), Error>
{
    let data_path = data_file.as_ref();
    let ouput_dir = data_path.with_extension("");
    // println!("extracting {} to {:?}", data_path.display(), ouput_dir);
    let file = fs::File::open(data_file)?;    
    let mut archive = zip::ZipArchive::new(file)?;
    let create_error = |file: &ZipFile<'_, _>| Error::from(ZipError::InvalidFileName(format!("cannot convert {} to enclosed name", file.name())));

    for idx in 0..archive.len() 
    {
        let mut file = archive.by_index(idx).unwrap();
        // println!("{:?}", file.enclosed_name());
        let outpath = file.enclosed_name().ok_or_else(|| create_error(&file)).map(|p| ouput_dir.join(p))?;

        if file.is_dir() 
        {
            // println!("File {} extracted to \"{}\"", idx, outpath.display());
            fs::create_dir_all(&outpath).unwrap();
        } 
        else 
        {
            // println!(
            //     "File {} extracted to \"{}\" ({} bytes)",
            //     idx,
            //     outpath.display(),
            //     file.size()
            // );
            if let Some(p) = outpath.parent() 
            {
                if !p.exists() 
                {
                    let _ = fs::create_dir_all(p)?;
                }
            }
            let _ = File::create(&outpath).and_then(|mut f| io::copy(&mut file, &mut f))?;
        }
    }
    Ok(())
}

fn zip_dir<T: Write + Seek, I: Iterator<Item=DirEntry>>(entries: I, prefix: &Path, writer: T, method: zip::CompressionMethod) -> Result<(), Error>
{
    let mut zip = zip::ZipWriter::new(writer);
    let options = SimpleFileOptions::default().compression_method(method);//.unix_permissions(0o755);

    let prefix = Path::new(prefix);
    let mut buffer = Vec::new();
    for entry in entries 
    {
        let path = entry.path();
        let name = path.strip_prefix(prefix)?;
        let path_as_string = name.to_str().map(str::to_owned).ok_or_else(|| ZipError::InvalidFileName(format!("{} Is a Non UTF-8 Path", name.display())))?;

        // Write file or directory explicitly
        // Some unzip tools unzip files with directory paths correctly, some do not!
        if path.is_file() 
        {
            // println!("adding file {path:?} as {name:?} ...");
            zip.start_file(path_as_string, options)?;
            let mut f = File::open(path)?;

            f.read_to_end(&mut buffer)?;
            zip.write_all(&buffer)?;
            buffer.clear();
        } 
        else if !name.as_os_str().is_empty() 
        {
            // Only if not root! Avoids path spec / warning
            // and mapname conversion failed error on unzip
            // println!("adding dir {path_as_string:?} as {name:?} ...");
            zip.add_directory(path_as_string, options)?;
        }
    }
    zip.finish()?;
    Ok(())
}

fn create_filename(directory: &Path) -> PathBuf
{
    directory.with_extension(EXTENSION)
}

pub fn create<P: AsRef<Path>>(directory: P) -> Result<PathBuf, Error>
{
    let source_path = directory.as_ref();
    // println!("source_path: {}", source_path.display());
    let output_filename = create_filename(&source_path);
    // println!("output_filename: {}", output_filename.display());
    let output_file = File::create(output_filename.clone())?;
    let method = zip::CompressionMethod::Stored;
    let (entries, errors) : (Vec<_>, Vec<_>) = WalkDir::new(source_path).follow_links(false).into_iter().partition(Result::is_ok);
    if errors.is_empty()
    {
        zip_dir(entries.into_iter().map(Result::unwrap), &source_path, output_file, method).map(|_| output_filename)
    }
    else
    {
        let mut error_string = String::new();
        for e in errors
        {
            error_string.push_str(&format!("{}\n", e.unwrap_err().to_string()))
        }
        Err(Error::from(ZipError::InvalidFileName(error_string)))
    }    
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn create_filename_test() 
    {
        let data_dir : PathBuf = vec!["some", "thing"].iter().collect();
        let expected : PathBuf = vec!["some", "thing.smlm"].iter().collect();
        assert_eq!(create_filename(&data_dir), expected)
    }
}