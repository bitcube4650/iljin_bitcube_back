package iljin.framework.core.file;

import iljin.framework.core.config.FileStorageConfig;
import iljin.framework.core.security.user.User;
import iljin.framework.core.security.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public FileStorageService(FileRepository fileRepository,
                              FileStorageConfig fileStorageConfig,
                              UserRepository userRepository,
                              ModelMapper modelMapper
    ) {
        this.modelMapper = modelMapper;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch(Exception ex) {
            throw new FileStorageException("파일이 저장 될 디렉토리를 생성하지 못했습니다.", ex);
        }
    }

    public String getOriginalFileName(Long id) {

        Optional<UploadFile> uploadedFile = fileRepository.findById(id);
        return uploadedFile.map(uploadFile -> uploadFile.originalName)
                                    .orElse("fileNameError");
    }

    public String getOriginalFileNameByChangedFileName(String fileName) {

        Optional<UploadFile> uploadedFile = fileRepository.findByStoredName(fileName);
        return uploadedFile.map(uploadFile -> uploadFile.originalName)
                .orElse("fileNameError");
    }

    private String getDownloadFileUri(String fileName) {

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/download/")
                .path(fileName)
                .toUriString();
    }

    private UploadFile filePathSave(String fileName, String storedName) {
        UploadFile uf = new UploadFile();

        uf.setOriginalName(fileName);
        uf.setStoredName(storedName);
        uf.setDownloadUrl(getDownloadFileUri(storedName));

        fileRepository.save(uf);

        return uf;
    }

    public UploadFile storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String ext = fileName.substring(fileName.lastIndexOf("."));

        UUID uuid = UUID.randomUUID();
        String storedName = uuid.toString() + ext;

        try {
            if(fileName.contains("..")) {
                throw new FileStorageException("파일명에 허용되지 않는 문자가 포함되어 있습니다." + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(storedName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return filePathSave(fileName, storedName);

        } catch(IOException ex) {
            throw new FileStorageException("파일 " + fileName + "을 저장할 수 없습니다. 다시 시도해 보세요.", ex);
        }
    }

    public UploadFile storeFileWithDocumentId(MultipartFile file, Long id) {
        UploadFile result = storeFile(file);
        result.setDocumentHId(id);
        fileRepository.save(result);
        return result;
    }

    public UploadFile storeFileWithDocumentIdAndFileType(MultipartFile file, Long id, String fileType, Long seq) {
        UploadFile result = storeFile(file);
        result.setDocumentHId(id);
        result.setFileType(fileType);
        fileRepository.save(result);
        return result;
    }


    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
                if(resource.exists()) {
                    return resource;
                } else {
                    throw new FileNotFoundException("파일을 찾을 수 없습니다." + fileName);
            }

        } catch(MalformedURLException ex) {
            throw new FileNotFoundException("파일을 찾을 수 없습니다." + ex);
        }
    }

    public Optional<UploadFile> getUploadFileInfo(Long id) {
        return fileRepository.findById(id);
    }


    public List<File> readAllUploadedFiles(String uploadDir) {
        List fileList = new ArrayList<File>();
        try {
            Files.list(Paths.get(uploadDir))
                    .filter(Files::isRegularFile)
                    .forEach(c -> fileList.add(c.toFile()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    // 일단위로 수정된 파일 찾기
    public List<File> readDailyUploadedFiles(String uploadDir) {
        Instant lastDay = Instant.now().minus(1, ChronoUnit.DAYS);
        List fileList = new ArrayList<File>();

        try {
            Files.list(Paths.get(uploadDir))
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant().isAfter(lastDay);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }).forEach(c -> fileList.add(c.toFile()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    // Only 1GB under file support
    public void copyFile(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }

    public File getFileWithPathString(String filePathString) {
        return new File(filePathString);
    }

    public boolean updateDocumentHId(Long fileId, Long documentHId) {
        Optional<UploadFile> uploadedFile = fileRepository.findById(fileId);
        return uploadedFile.map(uploadFile -> {
            uploadFile.setDocumentHId(documentHId);
            fileRepository.save(uploadFile);
            return true;
        }).orElse(false);
    }

    public void deleteById(Long id) {
        fileRepository.deleteById(id);
    }

    public void updateRemark(Long id, String str, Long seq) {
        Optional<UploadFile> file = fileRepository.findById(id);
        file.ifPresent(c -> {
            c.setRemark(str);
            c.setSeq(seq);
            fileRepository.save(c);
        });
    }

    public Optional<String> getDownloadUrlFromOriginalFileName(final String originalName) {
        Optional<UploadFile> file = fileRepository.findFirstByOriginalNameContains(originalName);

        if (file.isPresent()) {
            return Optional.of(file.map(UploadFile::getDownloadUrl).get());
        } else return Optional.empty();
    }

    public List<UploadFileDto> findByOriginalFileName(String fileName) {
        List<UploadFile> fileList = fileRepository.findAllByOriginalNameContains(fileName);
        List<UploadFileDto> result = new ArrayList<>();
        for (UploadFile file : fileList) {
            UploadFileDto fileDto = modelMapper.map(file, UploadFileDto.class);
//            Optional<User> user =  userRepository.findByLoginId(fileDto.getCreatedBy());
//            if (user.isPresent()) {
//                fileDto.setCreator(user.get().getUserName());
//            }
            result.add(fileDto);
        }
        return result;
    }
}
