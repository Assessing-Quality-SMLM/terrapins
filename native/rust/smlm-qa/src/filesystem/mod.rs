use std::path::{Path, PathBuf};

pub trait FileSystem
{
    fn exists<P: AsRef<Path>>(&self, path: P) -> bool;
}

impl<T: FileSystem> FileSystem for &T
{
    fn exists<P: AsRef<Path>>(&self, path: P) -> bool
    {
        (*self).exists(path)
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct SystemFileSystem;

impl FileSystem for SystemFileSystem
{
    fn exists<P: AsRef<Path>>(&self, path: P) -> bool
    {
        path.as_ref().exists()
    }
}

#[derive(Debug, Clone, Default, PartialEq)]
pub struct FakeFileSystem 
{
    paths: Vec<PathBuf>
}

impl FakeFileSystem
{
    pub fn new(paths: Vec<PathBuf>) -> Self
    {
        Self{paths}
    }    
    
    pub fn from_iter<T, I: Iterator<Item=T>>(paths: I) -> Self
    where PathBuf: From<T>
    {
        Self::new(paths.map(PathBuf::from).collect())
    }

    // pub fn from_str(paths: Vec<&str>) -> Self
    // {
    //     Self::from_iter(paths.iter())
    // }
}

impl<T: AsRef<Path>> From<&[T]> for FakeFileSystem
{
    fn from(paths: &[T]) -> Self
    {
        Self::from_iter(paths.iter().map(|p| p.as_ref().to_path_buf()))
    }
}

impl FileSystem for FakeFileSystem
{
    fn exists<P: AsRef<Path>>(&self, path: P) -> bool
    {
        let p = path.as_ref();
        self.paths.iter().find(|pb| pb.as_path() == p).is_some()
    }
}

#[cfg(test)]
mod tests 
{
    use super::*;

    #[test]
    fn empty_fakesystem_matches_nothing() 
    {
        assert_eq!(FakeFileSystem::default().exists("junk"), false);
    }

    #[test]
    fn fakesystem_can_match_things() 
    {
        assert_eq!(FakeFileSystem::from(["something"].as_slice()).exists("something"), true);
    }
}