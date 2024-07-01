package com.ecommerce.sbecom.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
//        File names of current/original file
//        This method returns the original filename in the user's filesystem as it was provided when the file was uploaded. It can be used to retrieve the name of the file as it was on the client's machine.
//        If a user uploads a file named example.txt, the file.getOriginalFilename returns example.txt
        String originalFileName = file.getOriginalFilename();

//        Generate a unique file name
//        mat.jpg -> 1234.jpg
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));
        String filePath = path + File.separator + fileName;
//       images/whdiwjdiwjddw.jpg

//        Check if path exist and create
        File folder = new File(path);
        //The File class in Java is versatile and can represent both files and directories
        //new File(path): This line creates a new File object named folder that represents the directory specified by path.
        // It does not create the actual directory on the file system; it just creates a Java object that represents the directory path.
        if(!folder.exists()){
            folder.mkdir();
        }
//        upload to server
//      Files.copy(InputStream in, Path target)
//      This static method from the java.nio.file.Files class copies all bytes from the input stream in to the file specified by target
//      The method reads the input stream until the end of the stream is reached and writes the bytes to the target file.
//      Paths.get(filePath) converts the string path into a Path object which is used by the NIO file operations.
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }
}
