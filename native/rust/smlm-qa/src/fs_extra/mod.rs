use std::fs;
use std::io::{Error as IoError};
use std::path::{Path, PathBuf};

#[allow(dead_code)]
pub fn copy_dir_all<P: AsRef<Path>, Q: AsRef<Path>>(src: P, dest: Q) -> Result<(), IoError>
{
    // println!("creating: {}", dest.as_ref().display());
    fs::create_dir_all(&dest)?;
    for entry in fs::read_dir(src)? 
    {
        let entry = entry?.path();        
        let file_name = entry.file_name().ok_or(IoError::other(format!{"{} has no file name", entry.display()}))?;
        let dest = dest.as_ref().join(file_name);
        // println!("handling: {}", dest.display());
        if entry.is_dir() 
        {
            copy_dir_all(entry, dest)?;
        } 
        else 
        {
            fs::copy(entry, dest)?;
        }
    }
    Ok(())
}

fn get_parent(p: &Path) -> Result<&Path, IoError>
{
    p.parent().ok_or_else(|| IoError::other(format!("{} has no parent", p.display())))
}

// copy file to dest creating all dicrectories necessary
pub fn copy_file<P: AsRef<Path>, Q: AsRef<Path>>(src: P, dest: Q) -> Result<u64, IoError>
{
    let dest_path = dest.as_ref();
    let dest_directory = get_parent(dest_path)?;
    if !dest_directory.exists()
    {
        let _ = fs::create_dir_all(dest_directory)?;
    }
    fs::copy(src, dest_path)
}

#[allow(dead_code)]
pub fn to_path(parts: &[&str]) -> PathBuf
{
    parts.iter().collect()
}


#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn get_parent_basic() 
    {
        let p = to_path(&["some", "thing", "image.tiff"]);
        assert_eq!(get_parent(&p).unwrap(), to_path(&["some", "thing"]))
    }

    #[test]
    fn parent_fails() 
    {
        assert_eq!(get_parent(&Path::new("")).unwrap_err().to_string(), " has no parent");
    }
}