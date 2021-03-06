package ro.ieugen.demo.cxf.services;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.ieugen.demo.cxf.model.Directory;
import ro.ieugen.demo.cxf.model.DirectoryResult;
import ro.ieugen.demo.cxf.model.MyFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Simple file system service.
 * <p/>
 * GET      /path/to/folder             - Directory listing
 * GET      /path/to/file.txt           - Info and contents of file at path
 * POST     /path/to/newfolder          - Create a new folder
 * POST     /path/to/folder (multipart/form-data) - Upload file at folder path
 * PUT      /path/to/file.txt           - Replace contents of file at path
 * DELETE   /path/to/folder             - Delete folder at path
 */
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class FileSystemService {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemService.class);
    private final Path ROOT;

    public FileSystemService() {
        this("target");
    }

    public FileSystemService(String rootDirectory) {
        try {
            this.ROOT = Paths.get(rootDirectory).toRealPath(LinkOption.NOFOLLOW_LINKS);
            File root = ROOT.toFile();
            if (!root.exists()) {
                LOG.info("Creating root dir {}", ROOT.toString());
                Files.createDirectories(ROOT);
            } else if (!root.isDirectory()) {
                LOG.error("Supplied path is a file {}", rootDirectory);
                throw new IllegalArgumentException("Supplied path is a file " + rootDirectory);
            }
        } catch (IOException ioe) {
            LOG.error("Exception resolving root directory {}", "target");
            throw Throwables.propagate(ioe);
        }
    }

    @GET
    @javax.ws.rs.Path("/home/")
    public DirectoryResult getRoot() {
        Path path = getAndValidatePath("");
        return getDirectoryResult(path);
    }

    private DirectoryResult getDirectoryResult(Path path) {
        try (DirectoryStream<Path> filesAndDirs = Files.newDirectoryStream(path)) {

            final List<MyFile> myFiles = Lists.newArrayList();
            final List<Directory> directories = Lists.newArrayList();

            for (Path filePath : filesAndDirs) {
                File associatedFile = filePath.toFile();
                if (associatedFile.isDirectory()) {
                    directories.add(Directory.fromFile(associatedFile));
                } else {
                    myFiles.add(MyFile.fromFile(associatedFile));
                }
            }
            return new DirectoryResult(directories, myFiles);
        } catch (IOException e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @javax.ws.rs.Path("/home/{path}")
    public Response getFile(@javax.ws.rs.PathParam("path") String path) {
        LOG.info("--- invoke getFile with {}", path);
        Path pathOnDisk = getAndValidatePath(path);
        if (pathOnDisk.toFile().isDirectory()) {
            return Response.ok(getDirectoryResult(pathOnDisk)).build();
        } else {
            return Response.ok(pathOnDisk.toString()).build();
        }
    }

    private Path getAndValidatePath(String path) {
        Path localPath;
        try {
            localPath = Paths.get(ROOT.toString(), path).toRealPath(LinkOption.NOFOLLOW_LINKS);
            LOG.info("Path is {}", localPath.toString());
            return localPath;
        } catch (IOException e) {
            LOG.info("Path not found for {}", path);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @POST
    @javax.ws.rs.Path("/home/{path}")
    public Response createPath(@javax.ws.rs.PathParam("path") String path) {
        LOG.info("--- invoke create path with {}", path);
        return Response.ok().build();
    }

    @POST
    @javax.ws.rs.Path("/home/{path}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@javax.ws.rs.PathParam("path") String path) {
        LOG.info("--- invoke upload file with {}", path);
        return Response.ok().build();
    }

    @PUT
    @javax.ws.rs.Path("/home/{path}")
    public Response createOrReplace(@javax.ws.rs.PathParam("path") String path) {
        LOG.info("--- invoke create or replace with {}", path);

        return Response.ok().build();
    }

}
