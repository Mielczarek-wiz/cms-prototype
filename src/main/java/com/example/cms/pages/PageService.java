package com.example.cms.pages;

import com.example.cms.University.UniversityRepository;
import com.example.cms.pages.exceptions.PageException;
import com.example.cms.pages.exceptions.PageExceptionType;
import com.example.cms.pages.projections.PageDtoDetailed;
import com.example.cms.pages.projections.PageDtoSimple;
import com.example.cms.user.UserRepository;
import com.example.cms.validation.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PageService {
    private final PageRepository pageRepository;
    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;

    public PageService(PageRepository repository,
                       UniversityRepository universityRepository,
                       UserRepository userRepository) {
        this.pageRepository = repository;
        this.universityRepository = universityRepository;
        this.userRepository = userRepository;
    }

    public List<PageDtoSimple> getAll() {
        return pageRepository.findAll().stream()
                .map(PageDtoSimple::new)
                .collect(Collectors.toList());
    }

    public PageDtoDetailed get(long id) {
        return pageRepository.findById(id).map(PageDtoDetailed::new)
                .orElseThrow(NotFoundException::new);
    }

    public ResponseEntity<?> save(Page toSave) {
        validate(toSave);
        Page result = pageRepository.save(toSave);
        return ResponseEntity.created(URI.create("/" + result.getId())).body(new PageDtoSimple(result));
    }

    public ResponseEntity<?> update(long id, Page toUpdate) {
        validate(toUpdate);

        if (toUpdate.getParent() != null && id == toUpdate.getParent().getId()) {
            throw new PageException(PageExceptionType.IdEqualsParentId);
        }

        pageRepository.findById(id)
                .ifPresentOrElse(page -> {
                    page.updateFrom(toUpdate);
                    pageRepository.save(page);
                }, NotFoundException::new);

        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<?> delete(long id) {
        Page page = pageRepository.findById(id).orElseThrow(NotFoundException::new);

        if (pageRepository.existsByParent(page)) {
            throw new PageException(PageExceptionType.DeletingPageWitchChild);
        }

        pageRepository.delete(page);
        return ResponseEntity.noContent().build();
    }

    private void validate(Page page) {
        Page parent = page.getParent();
        if (parent != null) {
            checkExisting(parent.getId(), pageRepository, PageExceptionType.NotFoundParent);
        }
        if(page.getUniversity() != null) {
            checkExisting(page.getUniversity().getId(), universityRepository, PageExceptionType.NotFoundUniversity);
        }
        if(page.getCreator() != null) {
            checkExisting(page.getCreator().getId(), userRepository, PageExceptionType.NotFoundUser);
        }
    }

    private <T> void checkExisting(Long id, JpaRepository<T, Long> repository, PageExceptionType exceptionType) {
        if (id == null) {
            throw new PageException(exceptionType);
        } else {
            if (!repository.existsById(id)) {
                throw new PageException(exceptionType);
            }
        }
    }
}