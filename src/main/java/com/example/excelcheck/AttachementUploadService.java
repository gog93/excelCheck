package com.example.excelcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Service
public class  AttachementUploadService {
    private final String upload = System.getProperty("user.home") + "\\upload\\";

    public String upload(MultipartFile file) {
        String picUrl="";
        if (!file.isEmpty()) {
            File fileToCreate = new File(upload);
            if (!fileToCreate.exists()) {
                fileToCreate.mkdir();
            }
            picUrl = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            try {
                file.transferTo(new File(upload + File.separator + picUrl));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
      String filePath=  upload+picUrl;

        return filePath;
    }



}
