package iljin.framework.core.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<UploadFile, Long> {
    List<UploadFile> findAllByDocumentHIdAndFileTypeOrderBySeq(Long id, String fileType);
    Optional<UploadFile> findById(Long id);
    Optional<UploadFile> findByStoredName(String fileName);
    Optional<UploadFile> findFirstByOriginalNameContains(String storedName);
    List<UploadFile> findAllByOriginalNameContains(String originalName);
}
