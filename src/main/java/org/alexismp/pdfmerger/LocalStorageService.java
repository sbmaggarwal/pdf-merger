package org.alexismp.pdfmerger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService implements StorageService {
    private final Path rootLocation;

    @Autowired
    public LocalStorageService() {
        // TODO: generate unique Id? Depends on concurrency
        this.rootLocation = Paths.get("./tmp");
    }

    @Override
    public void init() {
		try {
			Files.createDirectories(rootLocation);
            System.out.println("Created temporary directory...");
		}
		catch (IOException e) {
			System.out.println("Could not initialize storage - " + e);
		}        
    }

    @Override
	public void store(MultipartFile file) {
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		try {
            if (!filename.endsWith(".pdf")) {
                throw new IOException("This doesn't seem to be a PDF file: " + filename);
            }
			if (file.isEmpty()) {
				throw new IOException("Failed to store empty file: " + filename);
			}
			if (filename.contains("..")) { // This is a security check
				throw new IOException( "Cannot store file with relative path outside current directory " + filename);
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, this.rootLocation.resolve(filename),
					StandardCopyOption.REPLACE_EXISTING);
                System.out.println(filename + " successfully uploaded");
			}
		}
		catch (IOException e) {
			System.out.println("Failed to store file " + filename + " - " + e);
		}        
    }

	@Override
	public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

}