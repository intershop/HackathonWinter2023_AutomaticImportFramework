package com.intershop.platform.utils.internal.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.paging.PageableIterator;
import com.intershop.beehive.core.capi.paging.PagingMgr;
import com.intershop.beehive.core.capi.search.ResultMapper;
import com.intershop.beehive.core.capi.util.MappingIterator;
import com.intershop.beehive.core.capi.util.ObjectMapper;
import com.intershop.beehive.core.internal.paging.PageableIteratorImpl;
import com.intershop.beehive.core.internal.search.MappingPageable;
import com.intershop.beehive.foundation.util.Iterators;
import com.intershop.beehive.foundation.util.ResettableIterator;
import com.intershop.beehive.foundation.util.ResettableIteratorImpl;
import com.intershop.component.foundation.capi.upload.Directory;
import com.intershop.component.foundation.capi.upload.MVCFile;
import com.intershop.component.foundation.internal.iterator.ComparatorAdapter;
import com.intershop.component.foundation.internal.upload.MVCFileImpl;

/**
 * Represents a container that holds the static content or the impex directory of
 * a unit in the shared file system.
 *
 * @author Andreas Diel
 */
public class NoneDomainDirectoryImpl implements Directory
{
    private String root = null;
    private String name = null;
    private List directories = null;
    private String[] files = null;
    private NoneDomainDirectoryImpl parent = null;
    private HasSubdirectories subdirectoriesState = null;

   /**
    * Creates a partially initialized <code>Directory</code> object (without parent
    * directory).
    *
    * @param name The directory name.
    */
   public NoneDomainDirectoryImpl(String root, String name)
   {
        this(root, name, null);
   }

   /**
    * Creates an initialized <code>Directory</code> object.
    *
    * @param name The directory name.
    * @param parent The parent directory.
    */
   public NoneDomainDirectoryImpl(String root, String name, NoneDomainDirectoryImpl parent)
   {
       this.root = root;
       this.name = name;
       this.directories = new ArrayList();
       this.parent = parent;
   }

   /**
    * Returns <code>true</code> if the directory specified by the parameter
    * <code>dirPath</code> is a sub directory of this directory.
    * @param dirPath The sub directory selector.
    */
   public boolean getcontains(String dirPath)
    {
       if (dirPath == null || dirPath.length() <= 0)
       {
           return false;
       }

        // first check the ancestors
        String str = checkPath(dirPath);
        String path = getPath();
        if (!str.startsWith(path))
        {
            return false;
        }
        //
        return getDirectory(str.substring(path.length())) != null;
    }

   /**
    * Returns a sub directory of this directory.
    * The sub directory is selected by <code>dirPath</code> ("images/product" or
    * "images\product").
    * @param dirPath The sub directory selector.
    * @return A <code>Directory</code> object representing the selected sub directory
    * or <code>null</code> if the directory does not exist.
    */
   public Directory getDirectory(String dirPath)
   {
        if (dirPath == null || dirPath.trim().equals(""))
        {
            throw new IllegalArgumentException("dir path must not be null or an empty string");
        }
        String str = checkPath(dirPath);
        if (str.indexOf(File.separator) > 0)
        {
            String dirName = str.substring(0, str.indexOf(File.separator));
            for (Iterator iter = this.directories.iterator(); iter.hasNext(); )
            {
                Directory dir = (Directory)iter.next();
                if (dir.getName().equals(dirName))
                {
                    return dir.getDirectory(str.substring(str.indexOf(File.separator)));
                }
            }
        }
        // selector does not contain further path elements
        else
        {
            for (Iterator iter = this.directories.iterator(); iter.hasNext(); )
            {
                Directory dir = (Directory)iter.next();
                if (dir.getName().equals(str))
                {
                    return dir;
                }
            }
        }
        return null;
   }

    private String checkPath(String dirPath)
    {
        String str = dirPath.replace('\\', File.separatorChar).replace('/', File.separatorChar);
        // remove leading separator
        str = (str.charAt(0) == File.separatorChar ? str.substring(1) : str);
        // remove last character if it is a separator
        str = (str.charAt(str.length() - 1) == File.separatorChar) ? str.substring(0, str.length() - 1) : str;

        return str;
    }

   /**
    * Returns an iterator of all direct sub directories.
    *
    * @return An iterator of all direct sub directories or an empty iterator if there
    * are no sub directories.
    */
   public ResettableIterator getDirectories()
   {
        Collections.sort(this.directories, new ComparatorAdapter("Name", true));
        return new ResettableIteratorImpl(this.directories.iterator());
   }

   static class FileResultMapper
       implements ResultMapper
   {
       File dir = null;
       String unitDomainName = null;
       String unitDirectory = null;
       String path = null;

       FileResultMapper(File dir, String unitDomainName, String unitDirectory, String path)
       {
           this.dir = dir;
           this.unitDomainName = unitDomainName;
           this.unitDirectory = unitDirectory;
           this.path = path;
       }

       public Object resolve(Object source)
       {
           String fileName = (String)source;
           return new MVCFileImpl(new File(dir, fileName), unitDomainName, unitDirectory, path);
       }
   }

   /**
    * Returns an pageable iterator of all files that are stored in the directory.
    *
    * @return An pageable iterator of all files stored in this directory or an empty iterator
    * if there are no files.
    */
   public PageableIterator getPageableFiles()
   {
       final String absolutePath = getAbsolutePath();
       PagingMgr pagingMgr = (PagingMgr)NamingMgr.getInstance().
           lookupManager(PagingMgr.REGISTRY_NAME);
       PageableIterator pageable = pagingMgr.lookupPageable(absolutePath);

       if (pageable != null)
       {
           return pageable;
       }

       File file = new File(getAbsolutePath());
       String[] entries = file.list(
           new FilenameFilter()
           {
               public boolean accept(File dir, String name)
               {
                   return new File(dir, name).isFile();
               }
           });

       if (entries.length == 0) return PageableIteratorImpl.getEmptyIterator();

       pageable = new PageableIteratorImpl(Arrays.asList(entries).iterator(), entries.length, 10);

       pageable = new MappingPageable(pageable,
           new FileResultMapper(file, null, null, getPath()))
       {
            public String getID()
            {
                return absolutePath;
            }
       };

       pagingMgr.registerPageable(absolutePath, pageable);

       return pageable;
   }

   static class FileObjectMapper
       implements ObjectMapper
   {
       File dir = null;
       String unitDomainName = null;
       String unitDirectory = null;
       String path = null;

       FileObjectMapper(File dir, String unitDomainName, String unitDirectory, String path)
       {
           this.dir = dir;
           this.unitDomainName = unitDomainName;
           this.unitDirectory = unitDirectory;
           this.path = path;
       }

       public Object resolve(Object source)
       {
           String fileName = (String)source;
           return new MVCFileImpl(new File(dir, fileName), unitDomainName, unitDirectory, path);
       }
   }

   public ResettableIterator getFiles()
   {
       File file = new File(getAbsolutePath());

       if (files == null)
       {
           files = file.list(
               new FilenameFilter()
               {
                   public boolean accept(File dir, String name)
                   {
                       return new File(dir, name).isFile();
                   }
               });
       }

       // using a Collator might be better, but needs locale
       Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);

       return new ResettableIteratorImpl(
           new MappingIterator(
               Arrays.asList(files).iterator(),
               new FileObjectMapper(file, null, null, getPath())));
   }

   /**
    * Compares this object with the specified object for order.
    *
    * @param obj The object to be compared.
    * @return a negative integer, zero, or a positive integer as this object is less
    * than, equal to, or greater than the specified object.
    */
   public int compareTo(Object obj)
   {
        if (this == obj)
        {
            return 0;
        }

        if (obj == null)
        {
            return -1;
        }

        if (obj instanceof Directory)
        {
            Directory dir = (Directory)obj;
            int cmp = this.name.compareTo(dir.getName());
            if (cmp != 0) return cmp;
            if (parent != null)
            {
                cmp = parent.compareTo(dir);
                if (cmp != 0) return cmp;
            }
            else
            {
               if (dir.getParent() != null)
               {
                    return 1;
               }
            }
        }

        return -1;
   }

   /**
    * Indicates whether some object is equal to this one.
    * Comparison is made by name and absolute path to both directories
    *
    * @param obj The reference object with which to compare.
    * @return <code>true</code> if this object is the same as the <code>obj</code>
    * argument, <code>false</code> otherwise.
    */
   public boolean equals(Object obj)
   {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (obj instanceof NoneDomainDirectoryImpl)
        {
            NoneDomainDirectoryImpl dir = (NoneDomainDirectoryImpl)obj;
            return this.name.equals(dir.getName()) &&
            this.getAbsolutePath().equals(dir.getAbsolutePath()) &&
                (this.parent == null ? dir.getParent() == null : this.parent.equals(dir.getParent()));
        }

        return false;
   }

   /**
    * Returns a hash code value for the object.
    *
    * @return A hash code value for the object.
    */
   public int hashCode()
   {
        int hc = 31;
        int multiplier = 101;
        hc = hc * multiplier + (getName() != null ? getName().hashCode() : 0);
        hc = hc * multiplier + (getParent() != null ? getParent().hashCode() : 0);
        return hc;
   }

   /**
    * Returns the name of the directory.
    *
    * @return The name of the directory.
    */
   public String getName()
   {
        return this.name;
   }

   /**
    * Sets the parent directory of this directory.
    *
    * @param The parent directory.
    * @param parent
    */
   private void setParent(NoneDomainDirectoryImpl parent)
   {
        this.parent = parent;
   }

   /**
    * Returns the parent directory of this directory.
    *
    * @return The parent directory.
    */
   public Directory getParent()
   {
        return this.parent;
   }

   /**
    * Returns the directory path relative to the static content's or impex' root
    * directory.
    *
    * @return A string containing the directory path or <code>null</code> if the
    * directory is located in the static content's root directory.
    */
   public String getPath()
   {
        if (this.parent == null || this.parent.getName().equals(""))
        {
            return this.name;
        }
        else
        {
            return parent.getPath() + File.separator + this.name;
        }
   }

    /**
     * Returns the absolute path of this directory.
     *
     * @return A string containing the absolute directory path of this directory.
     * */
    public String getAbsolutePath()
    {
       return this.root + File.separator + getPath();
    }

   /**
    * Adds a sub directory to this directory.
    *
    * @param The sub directory.
    * @param dir
    */
   public void addDirectory(NoneDomainDirectoryImpl dir)
   {
        dir.setParent(this);
        this.directories.add(dir);
   }

   /**
    * Adds a file entry to the container.
    *
    * @param The file entry.
    * @param file
    */
   public void addFile(MVCFile file)
   {
   }

   /**
    * Returns an <code>Iterator</code> object containing all directories that are
    * ancestors of this directory starting from the root folder.
    *
    * @return An <code>Iterator</code> object containing all ancestors,
    * or an empty iterator if the directory is the root.
    */
   public Iterator getAncestors()
   {
        if (getParent() == null)
        {
            return Iterators.createEmptyIterator();
        }
        List lst = new ArrayList();
        Directory dir = getParent();
        do
        {
            lst.add(dir);
            dir = dir.getParent();
        } while (dir != null);
        Collections.reverse(lst);
        return lst.iterator();
   }

   /**
    * Indicates if this directory is a root directory, i.e. it has no parent directory.
    */
    public boolean isRoot()
    {
        return this.parent == null;
    }

    /**
     * Returns a list of all files located in this directory and in all of its
     * sub directories.
     */
    public List getFileList()
    {
        List fileList = new ArrayList();
        getFileList(fileList, this);
        return fileList;
    }

    /** */
    private static void getFileList(List<MVCFile> fileList, NoneDomainDirectoryImpl dir)
    {
        Iterator files = dir.getFiles();

        while (files.hasNext()) {
            fileList.add((MVCFile)files.next());
        }

        for (int i = 0, n = dir.directories.size(); i < n; ++i)
        {
            getFileList(fileList, (NoneDomainDirectoryImpl)dir.directories.get(i));
        }
    }

    public void setHasSubdirectories(HasSubdirectories hasSubdirectories)
    {
        subdirectoriesState = hasSubdirectories;
    }

    @Override
    public HasSubdirectories hasSubdirectories()
    {
        if (subdirectoriesState != null)
        {
            return subdirectoriesState;
        }

        if (directories.isEmpty())
        {
            return HasSubdirectories.HasNoSubdirectories;
        }
        else
        {
            return HasSubdirectories.HasSubdirectories;
        }
    }
}
