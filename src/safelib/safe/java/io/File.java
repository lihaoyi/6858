

package safe.java.io;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.String;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

import java.nio.file.Path;


public class File implements Serializable, Comparable<File>{

    java.io.File f;

    public static final char separatorChar = java.io.File.separatorChar;

    public static final String separator = java.io.File.separator;

    public static final char pathSeparatorChar = java.io.File.pathSeparatorChar;

    public static final String pathSeparator = java.io.File.pathSeparator;

    public File(String pathname) {
        f = new java.io.File(pathname);
    }



    private File(java.io.File f){
        this.f = f;
    }
    public File(String parent, String child) {
        f = new java.io.File(parent, child);
    }


    public File(URI uri) {
        f = new java.io.File(uri);
    }





    public String getName() { return f.getName(); }


    public String getParent() { return f.getParent(); }


    public File getParentFile() { return new File(f.getParentFile()); }


    public String getPath() { return f.getPath(); }





    public boolean isAbsolute() { return f.isAbsolute(); }


    public String getAbsolutePath() { return f.getAbsolutePath(); }


    public java.io.File getAbsoluteFile() { return f.getAbsoluteFile(); }


    public String getCanonicalPath() throws IOException { return f.getCanonicalPath(); }


    public java.io.File getCanonicalFile() throws IOException { return f.getCanonicalFile(); }



    @Deprecated
    public URL toURL() throws MalformedURLException { return f.toURL(); }


    public URI toURI() { return f.toURI(); }

    public boolean canRead() { return f.canRead(); }


    public boolean canWrite() { return f.canWrite(); }


    public boolean exists() { return f.exists(); }


    public boolean isDirectory() { return f.isDirectory(); }


    public boolean isFile() { return f.isFile(); }


    public boolean isHidden() { return f.isHidden(); }


    public long lastModified() { return f.lastModified(); }


    public long length() { return f.length(); }

    public boolean createNewFile() throws IOException { return f.createNewFile(); }


    public boolean delete() { return f.delete(); }


    public void deleteOnExit() { f.deleteOnExit(); }


    public String[] list() { return f.list(); }


    public String[] list(FilenameFilter filter) { return f.list(filter); }


    public File[] listFiles() {
        java.io.File[] fs = f.listFiles();
        File[] sfs = new File[fs.length];
        for(int i = 0; i < fs.length; i++) sfs[i] = new File(fs[i]);
        return sfs;
    }


    public File[] listFiles(FilenameFilter filter) {
        java.io.File[] fs = f.listFiles(filter);
        File[] sfs = new File[fs.length];
        for(int i = 0; i < fs.length; i++) sfs[i] = new File(fs[i]);
        return sfs;
    }


    public File[] listFiles(FileFilter filter) {
        java.io.File[] fs = f.listFiles(filter);
        File[] sfs = new File[fs.length];
        for(int i = 0; i < fs.length; i++) sfs[i] = new File(fs[i]);
        return sfs;
    }


    public boolean mkdir() { return f.mkdir(); }


    public boolean mkdirs() { return mkdirs(); }


    public boolean renameTo(File dest) { return f.renameTo(dest.f); }


    public boolean setLastModified(long time) {return f.setLastModified(time); }


    public boolean setReadOnly() { return f.setReadOnly(); }


    public boolean setWritable(boolean writable, boolean ownerOnly) { return f.setWritable(writable, ownerOnly); }


    public boolean setWritable(boolean writable) { return f.setWritable(writable); }


    public boolean setReadable(boolean readable, boolean ownerOnly) { return f.setReadable(readable, ownerOnly); }


    public boolean setReadable(boolean readable) { return f.setReadable(readable); }


    public boolean setExecutable(boolean executable, boolean ownerOnly) { return f.setExecutable(executable, ownerOnly); }


    public boolean setExecutable(boolean executable) { return f.setExecutable(executable); }


    public boolean canExecute() { return f.canExecute(); }





    public static File[] listRoots() {
        java.io.File[] fs = java.io.File.listRoots();
        File[] sfs = new File[fs.length];
        for(int i = 0; i < fs.length; i++) sfs[i] = new File(fs[i]);
        return sfs;
    }





    public long getTotalSpace() { return f.getTotalSpace(); }


    public long getFreeSpace() { return f.getFreeSpace(); }


    public long getUsableSpace() { return f.getUsableSpace(); }

    public static File createTempFile(String prefix, String suffix, File directory) throws IOException{
        return new File(java.io.File.createTempFile(prefix, suffix, directory.f));
    }



    public static File createTempFile(String prefix, String suffix) throws IOException{
        return new File(java.io.File.createTempFile(prefix, suffix));
    }





    public int compareTo(File pathname) { return f.compareTo(pathname.f); }


    public boolean equals(Object obj) { return f.equals(obj); }


    public int hashCode() { return f.hashCode(); }


    public String toString() { return f.toString(); }

    public Path toPath() { return f.toPath(); }
}
