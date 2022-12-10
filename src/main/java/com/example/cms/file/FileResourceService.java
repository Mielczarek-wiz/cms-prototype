package com.example.cms.file;

import com.example.cms.file.projections.FileDtoSimple;
import com.example.cms.page.Page;
import com.example.cms.page.PageRepository;
import com.example.cms.user.User;
import com.example.cms.user.UserRepository;
import com.example.cms.validation.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileResourceService {
    private final FileResourceRepository fileRepository;
    private final UserRepository userRepository;
    private final PageRepository pageRepository;

    public ResponseEntity<Resource> downloadFiles(Long pageId, String filename) {

        Page page = pageRepository.findById(pageId).orElseThrow(NotFoundException::new);
        FileResource fileResource = new FileResource();

        Optional<FileResource> optionalFileResource = fileRepository.findFileResourceByFilenameAndPage(filename, page);
        if (optionalFileResource.isPresent()) {
            fileResource = optionalFileResource.get();
        }


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("File-Name", fileResource.getFilename());
        httpHeaders.setContentDisposition(ContentDisposition.parse("attachment; File-Name=\"" + fileResource.getFilename() + "\""));
        httpHeaders.setContentType(MediaType.parseMediaType(fileResource.getFileType()));
        httpHeaders.setContentLength(fileResource.getData().length);

        return ResponseEntity.ok().headers(httpHeaders).body(new ByteArrayResource(fileResource.getData()));
    }

    public ResponseEntity<FileResource> uploadFile(Long pageId, Long userId, MultipartFile multipartFile) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(NotFoundException::new);
        Page page = pageRepository.findById(pageId).orElseThrow(NotFoundException::new);


        Optional<FileResource> optionalFileResource = fileRepository.findFileResourceByFilenameAndPage(multipartFile.getOriginalFilename(), page);

        optionalFileResource.ifPresent(fileRepository::delete);

        FileResource fileResource = new FileResource();
        fileResource.setUploadDate(Timestamp.from(Instant.now()));
        fileResource.setUploadedBy(user.getUsername());
        fileResource.setPage(page);
        fileResource.setFilename(StringUtils.cleanPath(multipartFile.getOriginalFilename()));
        fileResource.setFileSize(humanReadableByteCountSI(multipartFile.getSize()));
        fileResource.setFileType(multipartFile.getContentType());
        fileResource.setData(multipartFile.getBytes());


        return ResponseEntity.ok().body(fileRepository.save(fileResource));
    }

    public List<FileDtoSimple> getAll(Long pageId) {
        List<Object[]> objects = fileRepository.findAllByPage(pageId);

        return prepareProjectionOutput(objects);
    }

    private static List<FileDtoSimple> prepareProjectionOutput(List<Object[]> objects) {
        List<FileDtoSimple> output = new ArrayList<>();
        for (Object[] object : objects) {
            output.add(new FileDtoSimple(object[0].toString(), object[1].toString(),
                    object[2].toString(), object[3].toString(), object[4].toString()));
        }
        return output;
    }

    private static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
